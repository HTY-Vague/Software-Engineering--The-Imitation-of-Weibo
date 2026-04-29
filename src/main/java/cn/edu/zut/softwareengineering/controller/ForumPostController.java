package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @PostMapping("/create")
    public ResultUtil.Result<ForumPost> createPost(@RequestBody ForumPost post) {
        post.setAuthorId(SecurityUtil.getCurrentUserId());
        return ResultUtil.success(forumPostService.createPost(post));
    }

    @PostMapping("/detail")
    public ResultUtil.Result<ForumPost> getPostDetail(@RequestParam Integer postId) {
        return ResultUtil.success(forumPostService.getPostDetail(postId));
    }

    @PostMapping("/list")
    public ResultUtil.Result<List<ForumPost>> getPostList(@RequestParam(required = false) Integer boardId,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer size) {
        return ResultUtil.success(forumPostService.getPostList(boardId, keyword, page, size));
    }

    @PostMapping("/update")
    public ResultUtil.Result<Void> updatePost(@RequestBody ForumPost post) {
        forumPostService.updateOwnPost(post, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/delete")
    public ResultUtil.Result<Void> deletePost(@RequestParam Integer postId) {
        forumPostService.deleteOwnPost(postId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }
}
