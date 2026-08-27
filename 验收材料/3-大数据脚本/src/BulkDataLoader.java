package cn.edu.zut.sparkstats;

import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;


public class BulkDataLoader {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("用法: BulkDataLoader <jdbcUrl> <user> <password> <csvDir>");
            System.err.println("示例: BulkDataLoader jdbc:mysql://127.0.0.1:3306/software_engineering_forum?... <用户> <密码> /opt/forum/csv");
            System.exit(1);
        }

        String jdbcUrl = args[0];
        String user = args[1];
        String password = args[2];
        String csvDir = args[3];

        // 0. 检查 CSV 目录
        String[] csvs = {"sys_user.csv", "forum_board.csv", "forum_post.csv",
                "forum_comment.csv", "forum_like.csv"};
        for (String c : csvs) {
            Path p = Paths.get(csvDir, c);
            if (!Files.exists(p)) {
                System.err.println("缺少 CSV 文件: " + p);
                System.exit(2);
            }
        }

        // 1. 先用原生 JDBC 清空业务表（关闭外键检查，按依赖反序 TRUNCATE）
        System.out.println("==== 步骤1: 清空业务表 ====");
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS=0");
            // 按外键依赖反序清空：点赞 -> 评论 -> 帖子 -> 板块 -> 用户
            for (String table : new String[]{"forum_like", "forum_comment", "forum_post",
                    "forum_board", "sys_user"}) {
                st.execute("TRUNCATE TABLE " + table);
                System.out.println("  TRUNCATE " + table + " OK");
            }
            st.execute("SET FOREIGN_KEY_CHECKS=1");
            System.out.println("业务表清空完成");
        } catch (Exception e) {
            System.err.println("清空业务表失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }

        // 2. 启动 Spark，读 CSV 写 MySQL
        SparkSession spark = SparkSession.builder()
                .appName("forum-bulk-loader")
                .getOrCreate();
        spark.sparkContext().setLogLevel("WARN");

        try {
            // 2.1 sys_user
            System.out.println("==== 步骤2.1: 写 sys_user ====");
            StructType userSchema = DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                    DataTypes.createStructField("user_id", DataTypes.LongType, false),
                    DataTypes.createStructField("username", DataTypes.StringType, false),
                    DataTypes.createStructField("password", DataTypes.StringType, false),
                    DataTypes.createStructField("nickname", DataTypes.StringType, false),
                    DataTypes.createStructField("avatar", DataTypes.StringType, true),
                    DataTypes.createStructField("signature", DataTypes.StringType, true),
                    DataTypes.createStructField("role", DataTypes.StringType, false),
                    DataTypes.createStructField("status", DataTypes.IntegerType, false),
                    DataTypes.createStructField("create_time", DataTypes.TimestampType, false),
                    DataTypes.createStructField("update_time", DataTypes.TimestampType, false)
            });
            Dataset<Row> userDf = readCsv(spark, csvDir + "/sys_user.csv", userSchema);
            writeTable(userDf, jdbcUrl, user, password, "sys_user",
                    "user_id", "username", "password", "nickname", "avatar", "signature",
                    "role", "status", "create_time", "update_time");

            // 2.2 forum_board
            System.out.println("==== 步骤2.2: 写 forum_board ====");
            StructType boardSchema = DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                    DataTypes.createStructField("board_id", DataTypes.LongType, false),
                    DataTypes.createStructField("board_name", DataTypes.StringType, false),
                    DataTypes.createStructField("description", DataTypes.StringType, true),
                    DataTypes.createStructField("sort_num", DataTypes.IntegerType, false),
                    DataTypes.createStructField("status", DataTypes.IntegerType, false),
                    DataTypes.createStructField("create_time", DataTypes.TimestampType, false),
                    DataTypes.createStructField("update_time", DataTypes.TimestampType, false)
            });
            Dataset<Row> boardDf = readCsv(spark, csvDir + "/forum_board.csv", boardSchema);
            writeTable(boardDf, jdbcUrl, user, password, "forum_board",
                    "board_id", "board_name", "description", "sort_num", "status",
                    "create_time", "update_time");

            // 2.3 forum_post
            System.out.println("==== 步骤2.3: 写 forum_post ====");
            StructType postSchema = DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                    DataTypes.createStructField("post_id", DataTypes.LongType, false),
                    DataTypes.createStructField("board_id", DataTypes.LongType, false),
                    DataTypes.createStructField("author_id", DataTypes.LongType, false),
                    DataTypes.createStructField("title", DataTypes.StringType, false),
                    DataTypes.createStructField("content", DataTypes.StringType, false),
                    DataTypes.createStructField("view_count", DataTypes.IntegerType, false),
                    DataTypes.createStructField("like_count", DataTypes.IntegerType, false),
                    DataTypes.createStructField("comment_count", DataTypes.IntegerType, false),
                    DataTypes.createStructField("is_top", DataTypes.IntegerType, false),
                    DataTypes.createStructField("status", DataTypes.IntegerType, false),
                    DataTypes.createStructField("create_time", DataTypes.TimestampType, false),
                    DataTypes.createStructField("update_time", DataTypes.TimestampType, false)
            });
            Dataset<Row> postDf = readCsv(spark, csvDir + "/forum_post.csv", postSchema);
            writeTable(postDf, jdbcUrl, user, password, "forum_post",
                    "post_id", "board_id", "author_id", "title", "content",
                    "view_count", "like_count", "comment_count", "is_top", "status",
                    "create_time", "update_time");

            // 2.4 forum_comment
            System.out.println("==== 步骤2.4: 写 forum_comment ====");
            StructType commentSchema = DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                    DataTypes.createStructField("comment_id", DataTypes.LongType, false),
                    DataTypes.createStructField("post_id", DataTypes.LongType, false),
                    DataTypes.createStructField("user_id", DataTypes.LongType, false),
                    DataTypes.createStructField("parent_comment_id", DataTypes.LongType, true),
                    DataTypes.createStructField("reply_user_id", DataTypes.LongType, true),
                    DataTypes.createStructField("content", DataTypes.StringType, false),
                    DataTypes.createStructField("like_count", DataTypes.IntegerType, false),
                    DataTypes.createStructField("status", DataTypes.IntegerType, false),
                    DataTypes.createStructField("create_time", DataTypes.TimestampType, false),
                    DataTypes.createStructField("update_time", DataTypes.TimestampType, false)
            });
            Dataset<Row> commentDf = readCsv(spark, csvDir + "/forum_comment.csv", commentSchema);
            writeTable(commentDf, jdbcUrl, user, password, "forum_comment",
                    "comment_id", "post_id", "user_id", "parent_comment_id", "reply_user_id",
                    "content", "like_count", "status", "create_time", "update_time");

            // 2.5 forum_like
            System.out.println("==== 步骤2.5: 写 forum_like ====");
            StructType likeSchema = DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                    DataTypes.createStructField("like_id", DataTypes.LongType, false),
                    DataTypes.createStructField("target_type", DataTypes.IntegerType, false),
                    DataTypes.createStructField("target_id", DataTypes.LongType, false),
                    DataTypes.createStructField("user_id", DataTypes.LongType, false),
                    DataTypes.createStructField("create_time", DataTypes.TimestampType, false)
            });
            Dataset<Row> likeDf = readCsv(spark, csvDir + "/forum_like.csv", likeSchema);
            writeTable(likeDf, jdbcUrl, user, password, "forum_like",
                    "like_id", "target_type", "target_id", "user_id", "create_time");

            System.out.println("==== 批量数据导入完成 ====");
            // 3. 回读验证（对应成员1流程"进入数据库查询数据是否存在"）
            System.out.println("==== 步骤3: 回读验证 ====");
            Properties verifyProps = new Properties();
            verifyProps.setProperty("user", user);
            verifyProps.setProperty("password", password);
            verifyProps.setProperty("driver", "com.mysql.cj.jdbc.Driver");
            for (String table : new String[]{"sys_user", "forum_board", "forum_post",
                    "forum_comment", "forum_like"}) {
                long cnt = spark.read().jdbc(jdbcUrl, table, verifyProps).count();
                System.out.println("  " + table + " -> " + cnt + " 行");
            }
        } catch (Exception e) {
            System.err.println("导入失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        } finally {
            spark.stop();
        }
    }

    private static Dataset<Row> readCsv(SparkSession spark, String path, StructType schema) {
        return spark.read()
                .option("header", "true")
                .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
                .option("nullValue", "")
                .option("quote", "\"")
                .schema(schema)
                .csv(path)
                .cache();
    }

    private static void writeTable(Dataset<Row> df, String jdbcUrl, String dbUser,
                                    String dbPass, String table, String... cols) {
        long n = df.count();
        String colList = String.join(", ", cols);
        String ph = String.join(", ", Collections.nCopies(cols.length, "?"));
        String sql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + ph + ")";
        System.out.println("  SQL: " + sql);

        df.foreachPartition((ForeachPartitionFunction<Row>) rows -> {
            try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                int batch = 0;
                while (rows.hasNext()) {
                    Row row = rows.next();
                    for (int i = 0; i < cols.length; i++) {
                        ps.setObject(i + 1, row.get(i));
                    }
                    ps.addBatch();
                    if (++batch % 1000 == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
                conn.commit();
            }
        });

        System.out.println("  写入 " + table + " -> " + n + " 行");
    }
}