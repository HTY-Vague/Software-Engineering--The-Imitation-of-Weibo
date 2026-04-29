package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.service.ForumLikeService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
@Tag(name = "点赞接口", description = "帖子和评论的通用点赞能力")
public class ForumLikeController {

    private final ForumLikeService forumLikeService;

    public ForumLikeController(ForumLikeService forumLikeService) {
        this.forumLikeService = forumLikeService;
    }

    @PostMapping("/like")
    @Operation(summary = "点赞", description = "targetType: 1=帖子，2=评论")
    public ResultUtil.Result<Void> like(@RequestParam Integer targetType,
                                        @RequestParam Integer targetId) {
        forumLikeService.like(targetType, targetId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞", description = "targetType: 1=帖子，2=评论")
    public ResultUtil.Result<Void> unlike(@RequestParam Integer targetType,
                                          @RequestParam Integer targetId) {
        forumLikeService.unlike(targetType, targetId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/status")
    @Operation(summary = "查询当前用户点赞状态")
    public ResultUtil.Result<Boolean> hasLiked(@RequestParam Integer targetType,
                                               @RequestParam Integer targetId) {
        return ResultUtil.success(forumLikeService.hasLiked(targetType, targetId, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/count")
    @Operation(summary = "查询点赞数量")
    public ResultUtil.Result<Integer> countLikes(@RequestParam Integer targetType,
                                                 @RequestParam Integer targetId) {
        return ResultUtil.success(forumLikeService.countLikes(targetType, targetId));
    }
}
