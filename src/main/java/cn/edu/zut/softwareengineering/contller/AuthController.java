package cn.edu.zut.softwareengineering.contller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.UserService;




@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }



    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        User result = userService.login(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User result = userService.register(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        return ResponseEntity.ok().build();

    }

}
