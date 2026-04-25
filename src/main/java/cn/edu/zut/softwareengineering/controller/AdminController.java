package cn.edu.zut.softwareengineering.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/admin")// 管理员相关接口
public class AdminController {

    private final ForumBoardService boardService;
    private final UserService userService;

    public AdminController(UserService userService, ForumBoardService boardService) {
        this.userService = userService;
        this.boardService = boardService;
    }

    @GetMapping("/users")// 获取所有用户
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/status")// 启用/禁用用户
    public void updateUserStatus(@PathVariable Integer userId, @RequestBody Integer status) {
        userService.updateUserStatus(userId, status);
    }

    @GetMapping("/forums")// 获取所有论坛板块
    public ResponseEntity<List<ForumBoard>> getBoards() {
        return ResponseEntity.ok(boardService.getBoards());
    }

    @PostMapping("/forums")// 添加论坛板块
    public ResponseEntity<ForumBoard> addBoard(@RequestBody ForumBoard forumBoard) {
        boardService.addBoard(forumBoard);
        return ResponseEntity.ok(forumBoard);
    }

    @PutMapping("/forums/{forumId}")// 更新论坛板块
    public void updateForum(@PathVariable Integer forumId, @RequestBody ForumBoard forumBoard) {
        boardService.updateBoard(forumBoard);
    }

    @PutMapping("/forums/{forumId}/status")// 启用/禁用论坛板块
    public void updateForumStatus(@PathVariable Integer forumId, @RequestBody Integer status) {
        boardService.updateBoardStatus(forumId, status);
    }

    @PutMapping("/forums/sort")// 排序论坛板块
    public void sortForums() {
        boardService.sortBoards(null);
    }

    @DeleteMapping("/forums/{forumId}")// 删除论坛板块
    public void deleteForum(@PathVariable Integer forumId) {
        boardService.deleteBoard(forumId);
    }
}