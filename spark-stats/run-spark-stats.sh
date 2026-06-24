#!/bin/bash
# =============================================================
# 手动触发一次 Spark 跑批（课程演示用，不用等到凌晨 3 点）
# 用法： bash run-spark-stats.sh
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

SPARK_SUBMIT="/opt/spark/bin/spark-submit"
JAR_REMOTE="/opt/forum-stats.jar"
DB_URL="jdbc:mysql://127.0.0.1:3306/software_engineering_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
LOG_DIR="/var/log/forum-spark"
LOG_FILE="$LOG_DIR/manual-$(date +%Y%m%d-%H%M%S).log"

sudo mkdir -p "$LOG_DIR"

echo "==== 手动跑批开始，日志：$LOG_FILE ===="
$SPARK_SUBMIT \
  --class cn.edu.zut.sparkstats.ForumSparkAnalysis \
  --master local[2] \
  --driver-memory 1g \
  "$JAR_REMOTE" \
  "$DB_URL" "$DB_USER" "$DB_PASS" 2>&1 | tee "$LOG_FILE"

echo "==== 跑批完成 ===="
echo "可执行验证查询："
echo "  mysql -u$DB_USER -p'$DB_PASS' -e 'SELECT * FROM software_engineering_forum.stat_overview;'"
echo "  mysql -u$DB_USER -p'$DB_PASS' -e 'SELECT rank_num,post_id,title,hot_score FROM software_engineering_forum.stat_hot_post ORDER BY rank_num;'"
