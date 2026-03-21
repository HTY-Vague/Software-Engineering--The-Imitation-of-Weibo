package cn.edu.zut.softwareengineering.contller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;



@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestBody User user) {

        return ResponseEntity.ok(userService.getProfile());

    }


    @PostMapping("/update/profile")

    public void updateProfile(@RequestBody User user) {

        userService.updateProfile(user);
    }

    @PostMapping("/update/password")
    public void updatePassword(@RequestBody User user) {

        userService.updatePassword(user);

    }

    @PostMapping("/posts")
    public ResponseEntity<?> getPosts(@RequestBody User user) {

        return ResponseEntity.ok(userService.getPosts(user));

    }

    @PostMapping("/comments")
    public ResponseEntity<?> getComments(@RequestBody User user) {

        return ResponseEntity.ok(userService.getComments(user));

    }
}
