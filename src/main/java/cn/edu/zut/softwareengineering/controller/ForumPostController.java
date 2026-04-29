package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.service.ForumPostService;
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
@RequestMapping("/posts")
@Tag(name = "帖子接口", description = "帖子发布、列表、详情、编辑和删除")
public class ForumPostController {

    private final ForumPostService forumPostService;

    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @PostMapping("/create")
    @Operation(summary = "发布帖子", description = "使用当前登录用户作为帖子作者")
    public ResultUtil.Result<ForumPost> createPost(@RequestBody ForumPost post) {
        post.setAuthorId(SecurityUtil.getCurrentUserId());
        return ResultUtil.success(forumPostService.createPost(post));
    }

    @PostMapping("/detail")
    @Operation(summary = "获取帖子详情")
    public ResultUtil.Result<ForumPost> getPostDetail(@RequestParam Integer postId) {
        return ResultUtil.success(forumPostService.getPostDetail(postId));
    }

    @PostMapping("/list")
    @Operation(summary = "获取帖子列表", description = "支持按板块、关键词和分页参数查询")
    public ResultUtil.Result<List<ForumPost>> getPostList(@RequestParam(required = false) Integer boardId,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer size) {
        return ResultUtil.success(forumPostService.getPostList(boardId, keyword, page, size));
    }

    @PostMapping("/update")
    @Operation(summary = "编辑自己的帖子")
    public ResultUtil.Result<Void> updatePost(@RequestBody ForumPost post) {
        forumPostService.updateOwnPost(post, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除自己的帖子")
    public ResultUtil.Result<Void> deletePost(@RequestParam Integer postId) {
        forumPostService.deleteOwnPost(postId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }
}
