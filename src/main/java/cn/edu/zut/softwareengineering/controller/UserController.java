package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/user")// 用户相关接口
public class UserController {

    private final UserService userService;
    private final ForumPostService forumPostService;

    public UserController(UserService userService, ForumPostService forumPostService) {

        this.userService = userService;
        this.forumPostService = forumPostService;
    }

    @PostMapping("/profile")// 获取用户信息
    public ResultUtil.Result<User> getProfile() {
        return ResultUtil.success(userService.getProfile(SecurityUtil.getCurrentUserId()));

    }


    @PostMapping("/update/profile")// 更新用户信息
    public ResultUtil.Result<Void> updateProfile(@RequestBody User user) {
        user.setUserId(SecurityUtil.getCurrentUserId());
        userService.updateProfile(user);
        return ResultUtil.success();
    }

    @PostMapping("/update/password")// 更新密码
    public ResultUtil.Result<Void> updatePassword(@RequestParam String oldPassword,
                                                  @RequestParam String newPassword) {
        userService.updatePassword(SecurityUtil.getCurrentUserId(), oldPassword, newPassword);
        return ResultUtil.success();

    }

    @PostMapping("/posts")// 获取用户帖子
    public ResultUtil.Result<?> getPosts() {
        return ResultUtil.success(userService.getPosts(SecurityUtil.getCurrentUserId()));

    }

    @PostMapping("/delete/post")// 删除用户帖子
    public ResultUtil.Result<Void> deletePost(@RequestParam Integer postId) {
        forumPostService.deleteOwnPost(postId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();

    }

    @PostMapping("/comments")// 获取用户评论
    public ResultUtil.Result<?> getComments() {
        return ResultUtil.success(userService.getComments(SecurityUtil.getCurrentUserId()));

    }
}
