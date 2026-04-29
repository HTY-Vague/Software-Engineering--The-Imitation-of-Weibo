package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
@Tag(name = "评论接口", description = "评论发布、评论树查询和评论删除")
public class ForumCommentController {

    private final ForumCommentService forumCommentService;

    public ForumCommentController(ForumCommentService forumCommentService) {
        this.forumCommentService = forumCommentService;
    }

    @PostMapping("/create")
    @Operation(summary = "发表评论", description = "使用当前登录用户作为评论作者")
    public ResultUtil.Result<ForumComment> createComment(@RequestBody ForumComment comment) {
        comment.setUserId(SecurityUtil.getCurrentUserId());
        return ResultUtil.success(forumCommentService.createComment(comment));
    }

    @PostMapping("/list")
    @Operation(summary = "获取帖子评论树")
    public ResultUtil.Result<List<ForumComment>> getComments(@RequestParam Integer postId) {
        return ResultUtil.success(forumCommentService.getComments(postId));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除自己的评论")
    public ResultUtil.Result<Void> deleteComment(@RequestParam Integer commentId) {
        forumCommentService.deleteOwnComment(commentId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }
}
