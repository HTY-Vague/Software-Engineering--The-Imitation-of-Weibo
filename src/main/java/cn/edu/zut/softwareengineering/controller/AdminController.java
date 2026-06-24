package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.entity.StatActiveUser;
import cn.edu.zut.softwareengineering.entity.StatBoardActive;
import cn.edu.zut.softwareengineering.entity.StatContentQuality;
import cn.edu.zut.softwareengineering.entity.StatDailyTrend;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.SparkStatsService;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin")// 管理员相关接口
@Tag(name = "后台管理接口", description = "用户、板块、内容和统计管理")
public class AdminController {

    private final ForumBoardService boardService;
    private final UserService userService;
    private final ForumPostService postService;
    private final ForumCommentService commentService;
    private final StatisticsService statisticsService;
    private final SparkStatsService sparkStatsService;

    public AdminController(UserService userService,
                           ForumBoardService boardService,
                           ForumPostService postService,
                           ForumCommentService commentService,
                           StatisticsService statisticsService,
                           SparkStatsService sparkStatsService) {
        this.userService = userService;
        this.boardService = boardService;
        this.postService = postService;
        this.commentService = commentService;
        this.statisticsService = statisticsService;
        this.sparkStatsService = sparkStatsService;
    }

    @GetMapping("/users")// 获取所有用户
    @Operation(summary = "获取所有用户")
    public ResultUtil.Result<List<User>> getAllUsers() {
        return ResultUtil.success(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/status")// 启用/禁用用户
    @Operation(summary = "更新用户状态")
    public ResultUtil.Result<Void> updateUserStatus(@PathVariable Integer userId, @RequestBody Integer status) {
        userService.updateUserStatus(userId, status);
        return ResultUtil.success();
    }

    @GetMapping("/forums")// 获取所有论坛板块
    @Operation(summary = "获取所有板块")
    public ResultUtil.Result<List<ForumBoard>> getBoards() {
        return ResultUtil.success(boardService.getBoards());
    }

    @PostMapping("/forums")// 添加论坛板块
    @Operation(summary = "新增板块")
    public ResultUtil.Result<ForumBoard> addBoard(@RequestBody ForumBoard forumBoard) {
        boardService.addBoard(forumBoard);
        return ResultUtil.success(forumBoard);
    }

    @PutMapping("/forums/{forumId}")// 更新论坛板块
    @Operation(summary = "更新板块")
    public ResultUtil.Result<Void> updateForum(@PathVariable Integer forumId, @RequestBody ForumBoard forumBoard) {
        forumBoard.setBoardId(forumId);
        boardService.updateBoard(forumBoard);
        return ResultUtil.success();
    }

    @PutMapping("/forums/{forumId}/status")// 启用/禁用论坛板块
    @Operation(summary = "更新板块状态")
    public ResultUtil.Result<Void> updateForumStatus(@PathVariable Integer forumId, @RequestBody Integer status) {
        boardService.updateBoardStatus(forumId, status);
        return ResultUtil.success();
    }

    @PutMapping("/forums/sort")// 排序论坛板块
    @Operation(summary = "板块排序")
    public ResultUtil.Result<Void> sortForums(@RequestBody List<Long> boardIds) {
        boardService.sortBoards(boardIds);
        return ResultUtil.success();
    }

    @DeleteMapping("/forums/{forumId}")// 删除论坛板块
    @Operation(summary = "删除板块")
    public ResultUtil.Result<Void> deleteForum(@PathVariable Integer forumId) {
        boardService.deleteBoard(forumId);
        return ResultUtil.success();
    }

    @PutMapping("/posts/{postId}/top")
    @Operation(summary = "更新帖子置顶状态")
    public ResultUtil.Result<Void> updatePostTop(@PathVariable Integer postId, @RequestBody Integer isTop) {
        postService.toggleTop(postId, isTop);
        return ResultUtil.success();
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "管理员删除帖子")
    public ResultUtil.Result<Void> deletePost(@PathVariable Integer postId) {
        postService.deletePost(postId);
        return ResultUtil.success();
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "管理员删除评论")
    public ResultUtil.Result<Void> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResultUtil.success();
    }

    @GetMapping("/comments")
    @Operation(summary = "获取所有评论")
    public ResultUtil.Result<List<ForumComment>> getAllComments() {
        return ResultUtil.success(commentService.getAllComments());
    }

    @GetMapping("/statistics/overview")
    @Operation(summary = "获取后台统计概览")
    public ResultUtil.Result<Map<String, Object>> getStatisticsOverview() {
        return ResultUtil.success(statisticsService.getOverview());
    }

    @GetMapping("/statistics/hot-posts")
    @Operation(summary = "获取热门帖子", description = "数据来源：Spark 跑批写入的 stat_hot_post 表，按热度分排序")
    public ResultUtil.Result<List<StatHotPost>> getHotPosts(@RequestParam(required = false) Integer limit) {
        return ResultUtil.success(statisticsService.getHotPosts(limit));
    }

    @GetMapping("/statistics/board-active")
    @Operation(summary = "获取板块活跃度排行", description = "数据来源：Spark 跑批写入的 stat_board_active 表，按活跃度排序")
    public ResultUtil.Result<List<StatBoardActive>> getBoardActive() {
        return ResultUtil.success(statisticsService.getBoardActive());
    }

    @GetMapping("/statistics/active-users")
    @Operation(summary = "获取用户活跃度排行", description = "数据来源：Spark 跑批写入的 stat_active_user 表，按活跃度排序")
    public ResultUtil.Result<List<StatActiveUser>> getActiveUsers(@RequestParam(required = false) Integer limit) {
        return ResultUtil.success(statisticsService.getActiveUsers(limit));
    }

    @GetMapping("/statistics/content-quality")
    @Operation(summary = "获取内容质量分析", description = "数据来源：Spark 跑批写入的 stat_content_quality 表。qualityType 可选值：HIGH_LIKE_LOW_COMMENT（高赞低评论）、HIGH_COMMENT_LOW_LIKE（高评论低赞）、HOT（综合热门），不传则返回全部")
    public ResultUtil.Result<List<StatContentQuality>> getContentQuality(@RequestParam(required = false) String qualityType) {
        return ResultUtil.success(statisticsService.getContentQuality(qualityType));
    }

    @GetMapping("/statistics/daily-trend")
    @Operation(summary = "获取每日趋势", description = "数据来源：Spark 跑批写入的 stat_daily_trend 表。days 为查询最近天数，默认 7，最大 90")
    public ResultUtil.Result<List<StatDailyTrend>> getDailyTrend(@RequestParam(required = false) Integer days) {
        return ResultUtil.success(statisticsService.getDailyTrend(days));
    }

    @PostMapping("/statistics/trigger-spark")
    @Operation(summary = "手动触发 Spark 跑批", description = "异步调用服务器上的 spark-submit 运行 ForumSparkAnalysis，覆盖刷新 stat_* 统计表。课程演示时无需 SSH 即可刷新统计数据。需配置 SPARK_ENABLED=true 及数据库凭据环境变量。")
    public ResultUtil.Result<Map<String, Object>> triggerSparkRun() {
        return ResultUtil.success(sparkStatsService.triggerRun());
    }

    @GetMapping("/statistics/spark-status")
    @Operation(summary = "查询 Spark 跑批状态", description = "返回最近一次跑批任务的状态（READY/RUNNING/SUCCESS/FAILED）、起止时间和耗时。")
    public ResultUtil.Result<Map<String, Object>> getSparkStatus() {
        return ResultUtil.success(sparkStatsService.getStatus());
    }
}
