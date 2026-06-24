#!/bin/bash
# =============================================================
# 论坛 Spark 统计分析部署脚本
# 在服务器上执行，完成：部署 jar + 首次跑批 + 安装每日 cron
# 用法： bash deploy-spark-stats.sh
#
# 环境变量（必须显式提供，不提交真实凭据到仓库）：
#   DB_USER  数据库用户名
#   DB_PASS  数据库密码
# =============================================================

set -e

if [ -z "$DB_USER" ] || [ -z "$DB_PASS" ]; then
  echo "错误：请先设置 DB_USER 和 DB_PASS 环境变量"
  echo "示例： export DB_USER='your_user'; export DB_PASS='your_pass'"
  exit 1
fi

# --- 配置 ---
JAR_LOCAL="${JAR_LOCAL:-spark-stats/target/spark-stats-1.0.0.jar}"
JAR_REMOTE="/opt/forum-stats.jar"
SPARK_SUBMIT="/opt/spark/bin/spark-submit"
DB_URL="jdbc:mysql://127.0.0.1:3306/software_engineering_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
LOG_DIR="/var/log/forum-spark"
LOG_FILE="$LOG_DIR/stats-$(date +%Y%m%d-%H%M%S).log"
CRON_MARKER="forum-spark-stats"

echo "==== 1. 部署 jar 到 $JAR_REMOTE ===="
sudo mkdir -p /opt
sudo cp "$JAR_LOCAL" "$JAR_REMOTE"
sudo chmod 644 "$JAR_REMOTE"
echo "jar 部署完成"

echo "==== 2. 创建日志目录 $LOG_DIR ===="
sudo mkdir -p "$LOG_DIR"
sudo chown "$(whoami):$(whoami)" "$LOG_DIR"
sudo chmod 755 "$LOG_DIR"
echo "日志目录就绪"

echo "==== 3. 首次跑批验证 ===="
$SPARK_SUBMIT \
  --class cn.edu.zut.sparkstats.ForumSparkAnalysis \
  --master local[2] \
  --driver-memory 1g \
  "$JAR_REMOTE" \
  "$DB_URL" "$DB_USER" "$DB_PASS" 2>&1 | tee "$LOG_FILE"

echo "首次跑批完成，日志：$LOG_FILE"

echo "==== 4. 安装每日 cron（凌晨 3:00 执行） ===="
# 移除旧的同类条目，避免重复
( crontab -l 2>/dev/null | grep -v "$CRON_MARKER" ; \
  echo "0 3 * * * $SPARK_SUBMIT --class cn.edu.zut.sparkstats.ForumSparkAnalysis --master local[2] --driver-memory 1g $JAR_REMOTE \"$DB_URL\" $DB_USER $DB_PASS >> $LOG_DIR/cron-\%Y\%m\%d.log 2>&1 # $CRON_MARKER" \
) | crontab -

echo "cron 已安装，每日 03:00 自动跑批"
crontab -l | grep "$CRON_MARKER"
echo "==== 部署完成 ===="
