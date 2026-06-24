package cn.edu.zut.softwareengineering.service;

import java.util.Map;

/**
 * Spark 离线分析跑批触发服务。
 *
 * 提供手动触发 spark-submit 跑批和查询跑批状态的能力，
 * 方便课程演示时无需 SSH 到服务器即可刷新统计数据。
 *
 * 数据流：调用 spark-submit 运行 ForumSparkAnalysis →
 *        Spark 通过 JDBC 读写 MySQL → 覆盖写入 stat_* 表 →
 *        Spring Boot 后续统计接口读取最新结果。
 *
 * 仅当配置 spark.enabled=true 时可用，本地开发默认禁用。
 */
public interface SparkStatsService {

    /**
     * 触发一次 Spark 跑批（异步执行）。
     *
     * @return 包含 taskId 和当前状态的描述
     */
    Map<String, Object> triggerRun();

    /**
     * 查询最近一次跑批状态。
     *
     * @return 状态信息：state（READY/RUNNING/SUCCESS/FAILED）、message、startTime、endTime、durationMillis
     */
    Map<String, Object> getStatus();
}
