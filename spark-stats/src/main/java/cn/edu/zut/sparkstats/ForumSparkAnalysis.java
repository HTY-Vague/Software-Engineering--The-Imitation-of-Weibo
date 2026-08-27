package cn.edu.zut.sparkstats;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import java.util.Properties;

import static org.apache.spark.sql.functions.*;


public class ForumSparkAnalysis {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("用法: ForumSparkAnalysis <jdbcUrl> <user> <password>");
            System.err.println("示例: ForumSparkAnalysis jdbc:mysql://127.0.0.1:3306/software_engineering_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true <用户> <密码>");
            System.exit(1);
        }

        String jdbcUrl = args[0];
        String user = args[1];
        String password = args[2];

        SparkSession spark = SparkSession.builder()
                .appName("forum-spark-stats")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        Properties dbProps = new Properties();
        dbProps.setProperty("user", user);
        dbProps.setProperty("password", password);
        dbProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");

        try {
            // 读取业务表
            Dataset<Row> users = readTable(spark, jdbcUrl, dbProps, "sys_user");
            Dataset<Row> boards = readTable(spark, jdbcUrl, dbProps, "forum_board");
            Dataset<Row> posts = readTable(spark, jdbcUrl, dbProps, "forum_post");
            Dataset<Row> comments = readTable(spark, jdbcUrl, dbProps, "forum_comment");
            Dataset<Row> likes = readTable(spark, jdbcUrl, dbProps, "forum_like");

            // 只分析正常状态数据
            Dataset<Row> postsActive = posts.filter(col("status").equalTo(1));
            Dataset<Row> commentsActive = comments.filter(col("status").equalTo(1));

            runOverview(spark, users, boards, postsActive, commentsActive, likes, jdbcUrl, dbProps);
            runHotPosts(postsActive, jdbcUrl, dbProps);
            runBoardActive(spark, boards, postsActive, jdbcUrl, dbProps);
            runActiveUser(spark, users, postsActive, commentsActive, likes, jdbcUrl, dbProps);
            runContentQuality(postsActive, jdbcUrl, dbProps);
            runDailyTrend(spark, users, postsActive, commentsActive, likes, jdbcUrl, dbProps);

            System.out.println("==== Spark 统计分析跑批完成 ====");
        } catch (Exception e) {
            System.err.println("跑批失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } finally {
            spark.stop();
        }
    }

    private static Dataset<Row> readTable(SparkSession spark, String url, Properties props, String table) {
        return spark.read().jdbc(url, table, props).cache();
    }

    private static void writeOverwrite(Dataset<Row> df, String url, Properties props, String table) {
        df.write().mode(SaveMode.Overwrite).jdbc(url, table, props);
    }

    /**
     * 1. 统计概览快照（覆盖写 stat_overview 的 id=1 行）。
     * Spark 的 Overwrite 模式会整表清空再写，为了保留 id=1 语义，
     * 这里只写一行并保证 id=1。
     */
    private static void runOverview(SparkSession spark,
                                    Dataset<Row> users, Dataset<Row> boards,
                                    Dataset<Row> posts, Dataset<Row> comments,
                                    Dataset<Row> likes,
                                    String url, Properties props) {
        long userCount = users.count();
        long activeUserCount = users.filter(col("status").equalTo(1)).count();
        long enabledBoardCount = boards.filter(col("status").equalTo(1)).count();
        long postCount = posts.count();
        long commentCount = comments.count();
        long likeCount = likes.count();

        Dataset<Row> row = spark.createDataFrame(
                java.util.Collections.singletonList(
                        org.apache.spark.sql.RowFactory.create(
                                1L,
                                (int) userCount,
                                (int) activeUserCount,
                                (int) enabledBoardCount,
                                (int) postCount,
                                (int) commentCount,
                                (int) likeCount,
                                new java.sql.Timestamp(System.currentTimeMillis())
                        )
                ),
                org.apache.spark.sql.types.DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                        org.apache.spark.sql.types.DataTypes.createStructField("id", org.apache.spark.sql.types.DataTypes.LongType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("user_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("active_user_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("enabled_board_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("post_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("comment_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("like_count", org.apache.spark.sql.types.DataTypes.IntegerType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("update_time", org.apache.spark.sql.types.DataTypes.TimestampType, false)
                })
        );
        writeOverwrite(row, url, props, "stat_overview");
        System.out.println("[概览] 用户=" + userCount + " 启用用户=" + activeUserCount
                + " 板块=" + enabledBoardCount + " 帖子=" + postCount
                + " 评论=" + commentCount + " 点赞=" + likeCount);
    }

    /**
     * 2. 热门帖子榜（Top 50，覆盖写 stat_hot_post）。
     * 热度公式：like_count*2 + comment_count*3 + view_count*0.1
     */
    private static void runHotPosts(Dataset<Row> posts, String url, Properties props) {
        WindowSpec rankWindow = Window.orderBy(col("hot_score").desc());
        Dataset<Row> hot = posts
                .withColumn("hot_score",
                        col("like_count").multiply(2)
                                .plus(col("comment_count").multiply(3))
                                .plus(col("view_count").multiply(0.1)))
                .orderBy(col("hot_score").desc())
                .limit(50)
                .withColumn("rank_num", row_number().over(rankWindow))
                .select(
                        col("rank_num"),
                        col("post_id"),
                        col("board_id"),
                        col("author_id"),
                        col("title"),
                        col("view_count"),
                        col("like_count"),
                        col("comment_count"),
                        col("hot_score"),
                        current_timestamp().as("update_time")
                );
        writeOverwrite(hot, url, props, "stat_hot_post");
        System.out.println("[热门帖子] 写入 " + hot.count() + " 条");
    }

    /**
     * 3. 板块活跃度（覆盖写 stat_board_active）。
     * 活跃度：post_count*5 + like_sum*2 + comment_sum*3 + view_sum*0.1
     */
    private static void runBoardActive(SparkSession spark,
                                       Dataset<Row> boards, Dataset<Row> posts,
                                       String url, Properties props) {
        Dataset<Row> boardStats = posts.groupBy(col("board_id"))
                .agg(
                        count("*").as("post_count"),
                        sum("view_count").as("view_sum"),
                        sum("like_count").as("like_sum"),
                        sum("comment_count").as("comment_sum")
                );

        Dataset<Row> result = boards.join(boardStats, boards.col("board_id").equalTo(boardStats.col("board_id")), "left")
                .select(
                        boards.col("board_id"),
                        boards.col("board_name"),
                        coalesce(col("post_count"), lit(0)).as("post_count"),
                        coalesce(col("view_sum"), lit(0)).cast("bigint").as("view_sum"),
                        coalesce(col("like_sum"), lit(0)).as("like_sum"),
                        coalesce(col("comment_sum"), lit(0)).as("comment_sum")
                )
                .withColumn("active_score",
                        col("post_count").multiply(5)
                                .plus(col("like_sum").multiply(2))
                                .plus(col("comment_sum").multiply(3))
                                .plus(col("view_sum").multiply(0.1)))
                .withColumn("update_time", current_timestamp())
                .orderBy(col("active_score").desc());

        writeOverwrite(result, url, props, "stat_board_active");
        System.out.println("[板块活跃度] 写入 " + result.count() + " 条");
    }

    /**
     * 4. 用户活跃度排行（Top 50，覆盖写 stat_active_user）。
     * 活跃度：post_count*5 + comment_count*2 + like_received*1
     * like_received = 帖子被点赞数 + 评论被点赞数（通过 forum_like 关联）
     */
    private static void runActiveUser(SparkSession spark,
                                      Dataset<Row> users, Dataset<Row> posts,
                                      Dataset<Row> comments, Dataset<Row> likes,
                                      String url, Properties props) {
        // 每用户发帖数
        Dataset<Row> postCnt = posts.groupBy(col("author_id").as("uid"))
                .agg(count("*").as("post_count"));

        // 每用户评论数
        Dataset<Row> cmtCnt = comments.groupBy(col("user_id").as("uid"))
                .agg(count("*").as("comment_count"));

        // 用户帖子被点赞数：likes where target_type=1 join posts.on target_id=post_id -> author_id
        Dataset<Row> postLikeReceived = likes.filter(col("target_type").equalTo(1))
                .join(posts, likes.col("target_id").equalTo(posts.col("post_id")), "inner")
                .groupBy(posts.col("author_id").as("uid"))
                .agg(count("*").as("post_like_received"));

        // 用户评论被点赞数：likes where target_type=2 join comments.on target_id=comment_id -> user_id
        Dataset<Row> cmtLikeReceived = likes.filter(col("target_type").equalTo(2))
                .join(comments, likes.col("target_id").equalTo(comments.col("comment_id")), "inner")
                .groupBy(comments.col("user_id").as("uid"))
                .agg(count("*").as("cmt_like_received"));

        Dataset<Row> result = users
                .join(postCnt, users.col("user_id").equalTo(postCnt.col("uid")), "left")
                .join(cmtCnt, users.col("user_id").equalTo(cmtCnt.col("uid")), "left")
                .join(postLikeReceived, users.col("user_id").equalTo(postLikeReceived.col("uid")), "left")
                .join(cmtLikeReceived, users.col("user_id").equalTo(cmtLikeReceived.col("uid")), "left")
                .select(
                        users.col("user_id"),
                        users.col("username"),
                        users.col("nickname"),
                        coalesce(col("post_count"), lit(0)).as("post_count"),
                        coalesce(col("comment_count"), lit(0)).as("comment_count"),
                        (coalesce(col("post_like_received"), lit(0))
                                .plus(coalesce(col("cmt_like_received"), lit(0)))).as("like_count")
                )
                .withColumn("active_score",
                        col("post_count").multiply(5)
                                .plus(col("comment_count").multiply(2))
                                .plus(col("like_count").multiply(1)))
                .orderBy(col("active_score").desc())
                .limit(50);

        WindowSpec rankWindow = Window.orderBy(col("active_score").desc());
        result = result.withColumn("rank_num", row_number().over(rankWindow))
                .select(
                        col("rank_num"),
                        col("user_id"),
                        col("username"),
                        col("nickname"),
                        col("post_count"),
                        col("comment_count"),
                        col("like_count"),
                        col("active_score"),
                        current_timestamp().as("update_time")
                );

        writeOverwrite(result, url, props, "stat_active_user");
        System.out.println("[用户活跃度] 写入 " + result.count() + " 条");
    }

    /**
     * 5. 内容质量分析（覆盖写 stat_content_quality）。
     *   HIGH_LIKE_LOW_COMMENT：点赞>=10 且 评论<=5（优质干货）
     *   HIGH_COMMENT_LOW_LIKE：评论>=8 且 点赞<=3（争议/讨论帖）
     *   HOT：热度>=40（综合热门，热度公式同热门榜）
     * 一个帖子可同时归属多个类别（每个类别一行）。
     */
    private static void runContentQuality(Dataset<Row> posts, String url, Properties props) {
        Dataset<Row> withScore = posts.withColumn("hot_score",
                col("like_count").multiply(2)
                        .plus(col("comment_count").multiply(3))
                        .plus(col("view_count").multiply(0.1)));

        Dataset<Row> highLike = withScore
                .filter(col("like_count").geq(10).and(col("comment_count").leq(5)))
                .withColumn("quality_type", lit("HIGH_LIKE_LOW_COMMENT"))
                .select(col("post_id"), col("title"), col("like_count"), col("comment_count"), col("view_count"), col("quality_type"));

        Dataset<Row> highComment = withScore
                .filter(col("comment_count").geq(8).and(col("like_count").leq(3)))
                .withColumn("quality_type", lit("HIGH_COMMENT_LOW_LIKE"))
                .select(col("post_id"), col("title"), col("like_count"), col("comment_count"), col("view_count"), col("quality_type"));

        Dataset<Row> hot = withScore
                .filter(col("hot_score").geq(40))
                .withColumn("quality_type", lit("HOT"))
                .select(col("post_id"), col("title"), col("like_count"), col("comment_count"), col("view_count"), col("quality_type"));

        Dataset<Row> quality = highLike.union(highComment).union(hot)
                .withColumn("update_time", current_timestamp());

        writeOverwrite(quality, url, props, "stat_content_quality");
        System.out.println("[内容质量] 写入 " + quality.count() + " 条");
    }

    /**
     * 6. 每日趋势（按 create_time 日期分组，追加写 stat_daily_trend，主键 stat_date 去重）。
     * 用 Overwrite 模式按当天数据重写对应日期行；简单起见：全量按天聚合后 Overwrite。
     */
    private static void runDailyTrend(SparkSession spark,
                                      Dataset<Row> users, Dataset<Row> posts,
                                      Dataset<Row> comments, Dataset<Row> likes,
                                      String url, Properties props) {
        Dataset<Row> userTrend = users
                .withColumn("stat_date", to_date(col("create_time")))
                .groupBy(col("stat_date")).agg(count("*").as("new_user_count"));

        Dataset<Row> postTrend = posts
                .withColumn("stat_date", to_date(col("create_time")))
                .groupBy(col("stat_date")).agg(count("*").as("new_post_count"));

        Dataset<Row> cmtTrend = comments
                .withColumn("stat_date", to_date(col("create_time")))
                .groupBy(col("stat_date")).agg(count("*").as("new_comment_count"));

        Dataset<Row> likeTrend = likes
                .withColumn("stat_date", to_date(col("create_time")))
                .groupBy(col("stat_date")).agg(count("*").as("new_like_count"));

        Dataset<Row> result = userTrend
                .join(postTrend, new String[]{"stat_date"}, "full_outer")
                .join(cmtTrend, new String[]{"stat_date"}, "full_outer")
                .join(likeTrend, new String[]{"stat_date"}, "full_outer")
                .select(
                        col("stat_date"),
                        coalesce(col("new_user_count"), lit(0)).cast("int").as("new_user_count"),
                        coalesce(col("new_post_count"), lit(0)).cast("int").as("new_post_count"),
                        coalesce(col("new_comment_count"), lit(0)).cast("int").as("new_comment_count"),
                        coalesce(col("new_like_count"), lit(0)).cast("int").as("new_like_count")
                )
                .withColumn("update_time", current_timestamp())
                .orderBy(col("stat_date"));

        writeOverwrite(result, url, props, "stat_daily_trend");
        System.out.println("[每日趋势] 写入 " + result.count() + " 天");
    }
}
