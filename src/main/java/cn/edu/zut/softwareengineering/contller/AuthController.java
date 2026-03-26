package cn.edu.zut.softwareengineering.contller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;




@RestController
@RequestMapping("/auth")// 认证相关接口
public class AuthController {
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }



    @PostMapping("/login")// 用户登录
    public ResponseEntity<User> login(@RequestBody User user) {
        User result = userService.login(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")// 用户注册
    public ResponseEntity<User> register(@RequestBody User user) {
        User result = userService.register(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")// 用户注销
    public ResponseEntity<Void> logout() {

        return ResponseEntity.ok().build();

    }

}
