package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class ForumCommentController {

    private final ForumCommentService forumCommentService;

    public ForumCommentController(ForumCommentService forumCommentService) {
        this.forumCommentService = forumCommentService;
    }

    @PostMapping("/create")
    public ResultUtil.Result<ForumComment> createComment(@RequestBody ForumComment comment) {
        comment.setUserId(SecurityUtil.getCurrentUserId());
        return ResultUtil.success(forumCommentService.createComment(comment));
    }

    @PostMapping("/list")
    public ResultUtil.Result<List<ForumComment>> getComments(@RequestParam Integer postId) {
        return ResultUtil.success(forumCommentService.getComments(postId));
    }

    @PostMapping("/delete")
    public ResultUtil.Result<Void> deleteComment(@RequestParam Integer commentId) {
        forumCommentService.deleteComment(commentId);
        return ResultUtil.success();
    }
}
