package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.JwtUtil;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")// 认证相关接口
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")// 用户登录
    public ResultUtil.Result<Map<String, Object>> login(@RequestBody User user) {
        User result = userService.login(user);
        String token = jwtUtil.generateToken(result.getUserId(), result.getUsername(), result.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", result);
        return ResultUtil.success(data);
    }

    @PostMapping("/register")// 用户注册
    public ResultUtil.Result<User> register(@RequestBody User user) {
        User result = userService.register(user);
        return ResultUtil.success(result);
    }

    @PostMapping("/logout")// 用户注销
    public ResultUtil.Result<Void> logout() {
        return ResultUtil.success();
    }

}
