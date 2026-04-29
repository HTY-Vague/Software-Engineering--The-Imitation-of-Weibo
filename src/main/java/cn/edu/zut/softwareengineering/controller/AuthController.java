package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.JwtUtil;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")// 认证相关接口
@Tag(name = "认证接口", description = "用户注册、登录和退出")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")// 用户登录
    @Operation(summary = "用户登录", description = "校验账号密码，登录成功后返回 JWT 和用户信息")
    public ResultUtil.Result<Map<String, Object>> login(@RequestBody User user) {
        User result = userService.login(user);
        String token = jwtUtil.generateToken(result.getUserId(), result.getUsername(), result.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", result);
        return ResultUtil.success(data);
    }

    @PostMapping("/register")// 用户注册
    @Operation(summary = "用户注册", description = "创建普通用户账号")
    public ResultUtil.Result<User> register(@RequestBody User user) {
        User result = userService.register(user);
        return ResultUtil.success(result);
    }

    @PostMapping("/logout")// 用户注销
    @Operation(summary = "用户退出", description = "无状态 JWT 退出接口，客户端清理 token 即可")
    public ResultUtil.Result<Void> logout() {
        return ResultUtil.success();
    }

}
