package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.service.SparkStatsService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spark 跑批触发服务实现。
 *
 * 通过 ProcessBuilder 调用服务器上的 spark-submit 运行 ForumSparkAnalysis，
 * 异步执行避免 HTTP 超时，状态保存在内存中（单机部署足够）。
 *
 * 线程安全：使用 AtomicReference 保存最近一次任务状态，triggerRun 使用 CAS
 * 保证同一时刻只有一个跑批在运行，避免并发触发多个 spark-submit 进程。
 *
 * 配置项（application.properties）：
 *   spark.enabled         是否启用（默认 false）
 *   spark.submit-path     spark-submit 路径
 *   spark.jar-path        分析程序 jar 路径
 *   spark.jdbc-url        目标 MySQL JDBC URL
 *   spark.db-user         数据库用户名
 *   spark.db-pass         数据库密码
 */
@Service
public class SparkStatsServiceImpl implements SparkStatsService {

    private static final Logger log = LoggerFactory.getLogger(SparkStatsServiceImpl.class);

    @Value("${spark.enabled:false}")
    private boolean enabled;

    @Value("${spark.submit-path:/opt/spark/bin/spark-submit}")
    private String submitPath;

    @Value("${spark.jar-path:/opt/forum-stats.jar}")
    private String jarPath;

    @Value("${spark.jdbc-url:}")
    private String jdbcUrl;

    @Value("${spark.db-user:}")
    private String dbUser;

    @Value("${spark.db-pass:}")
    private String dbPass;

    private final AtomicReference<RunState> lastRun = new AtomicReference<>(RunState.initial());

    @PostConstruct
    void init() {
        log.info("SparkStatsService 初始化：enabled={}, submitPath={}, jarPath={}", enabled, submitPath, jarPath);
    }

    @Override
    public Map<String, Object> triggerRun() {
        Map<String, Object> resp = new LinkedHashMap<>();

        if (!enabled) {
            resp.put("state", "DISABLED");
            resp.put("message", "Spark 跑批触发未启用，请设置环境变量 SPARK_ENABLED=true 并配置 SPARK_DB_USER/SPARK_DB_PASS");
            return resp;
        }

        if (dbUser == null || dbUser.isBlank() || dbPass == null || dbPass.isBlank()) {
            resp.put("state", "DISABLED");
            resp.put("message", "Spark 跑批缺少数据库凭据，请配置 SPARK_DB_USER 和 SPARK_DB_PASS 环境变量");
            return resp;
        }

        RunState current = lastRun.get();
        if (current.isRunning()) {
            resp.put("state", "RUNNING");
            resp.put("message", "已有跑批任务正在执行，请等待完成后再触发");
            resp.put("startTime", current.startTime != null ? current.startTime.toString() : null);
            resp.put("taskId", current.taskId);
            return resp;
        }

        String taskId = "spark-" + System.currentTimeMillis();
        RunState running = RunState.running(taskId);
        if (!lastRun.compareAndSet(current, running)) {
            resp.put("state", "RUNNING");
            resp.put("message", "并发触发冲突，已有跑批任务正在执行");
            return resp;
        }

        Thread worker = new Thread(this::executeSpark, "spark-stats-runner-" + taskId);
        worker.setDaemon(true);
        worker.start();

        resp.put("state", "RUNNING");
        resp.put("message", "Spark 跑批已触发，正在异步执行");
        resp.put("taskId", taskId);
        resp.put("startTime", running.startTime != null ? running.startTime.toString() : null);
        return resp;
    }

    @Override
    public Map<String, Object> getStatus() {
        RunState s = lastRun.get();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("state", s.state);
        resp.put("message", s.message);
        resp.put("taskId", s.taskId);
        resp.put("startTime", s.startTime != null ? s.startTime.toString() : null);
        resp.put("endTime", s.endTime != null ? s.endTime.toString() : null);
        resp.put("durationMillis", s.durationMillis());
        return resp;
    }

    private void executeSpark() {
        RunState state = lastRun.get();
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    submitPath,
                    "--class", "cn.edu.zut.sparkstats.ForumSparkAnalysis",
                    "--master", "local[2]",
                    "--driver-memory", "1g",
                    jarPath,
                    jdbcUrl,
                    dbUser,
                    dbPass
            );
            pb.redirectErrorStream(true);
            log.info("启动 Spark 跑批：taskId={}, cmd={}", state.taskId, pb.command());

            process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                    log.info("[spark-stats] {}", line);
                }
            }

            int code = process.waitFor();
            String tail = tail(output, 500);

            if (code == 0) {
                RunState done = RunState.success(state.taskId, state.startTime, tail);
                lastRun.set(done);
                log.info("Spark 跑批成功：taskId={}, 耗时={}ms", state.taskId, done.durationMillis());
            } else {
                RunState failed = RunState.failed(state.taskId, state.startTime, "退出码=" + code + "，输出末尾：\n" + tail);
                lastRun.set(failed);
                log.error("Spark 跑批失败：taskId={}, 退出码={}, 输出:\n{}", state.taskId, code, tail);
            }
        } catch (Exception e) {
            RunState failed = RunState.failed(state.taskId, state.startTime, "异常：" + e.getClass().getSimpleName() + ": " + e.getMessage());
            lastRun.set(failed);
            log.error("Spark 跑批异常：taskId={}", state.taskId, e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private static String tail(CharSequence sb, int max) {
        String s = sb.toString();
        if (s.length() <= max) {
            return s;
        }
        return s.substring(s.length() - max);
    }

    /**
     * 跑批状态快照（不可变）。
     */
    private static final class RunState {
        final String state;
        final String taskId;
        final String message;
        final Instant startTime;
        final Instant endTime;

        private RunState(String state, String taskId, String message, Instant startTime, Instant endTime) {
            this.state = state;
            this.taskId = taskId;
            this.message = message;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        static RunState initial() {
            return new RunState("READY", null, "尚未触发跑批", null, null);
        }

        static RunState running(String taskId) {
            return new RunState("RUNNING", taskId, "跑批执行中", Instant.now(), null);
        }

        static RunState success(String taskId, Instant startTime, String output) {
            return new RunState("SUCCESS", taskId, "跑批成功\n" + output, startTime, Instant.now());
        }

        static RunState failed(String taskId, Instant startTime, String reason) {
            return new RunState("FAILED", taskId, reason, startTime, Instant.now());
        }

        boolean isRunning() {
            return "RUNNING".equals(state);
        }

        long durationMillis() {
            if (startTime == null || endTime == null) {
                return 0L;
            }
            return Duration.between(startTime, endTime).toMillis();
        }
    }
}
