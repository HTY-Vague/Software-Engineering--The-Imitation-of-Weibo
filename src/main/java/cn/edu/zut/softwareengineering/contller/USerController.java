package cn.edu.zut.softwareengineering.contller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;



@RestController
@RequestMapping("/user")// 用户相关接口
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping("/profile")// 获取用户信息
    public ResponseEntity<User> getProfile(@RequestBody User user) {
        Long userId = user != null && user.getUserId() != null ? user.getUserId().longValue() : null;
        return ResponseEntity.ok(userService.getProfile(userId));

    }


    @PostMapping("/update/profile")// 更新用户信息
    public void updateProfile(@RequestBody User user) {

        userService.updateProfile(user);
    }

    @PostMapping("/update/password")// 更新密码
    public void updatePassword(@RequestBody User user,
                               @RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        Long userId = user != null && user.getUserId() != null ? user.getUserId().longValue() : null;
        userService.updatePassword(userId, oldPassword, newPassword);

    }

    @PostMapping("/posts")// 获取用户帖子
    public ResponseEntity<?> getPosts(@RequestBody User user) {
        Long userId = user != null && user.getUserId() != null ? user.getUserId().longValue() : null;
        return ResponseEntity.ok(userService.getPosts(userId));

    }

    @PostMapping("/delete/post")// 删除用户帖子
    public void deletePost(@RequestParam Long postId) {
        userService.deletePost(postId);

    }

    @PostMapping("/comments")// 获取用户评论
    public ResponseEntity<?> getComments(@RequestBody User user) {
        Long userId = user != null && user.getUserId() != null ? user.getUserId().longValue() : null;
        return ResponseEntity.ok(userService.getComments(userId));

    }
}
