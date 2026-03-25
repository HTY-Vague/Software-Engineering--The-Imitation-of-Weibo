package cn.edu.zut.softwareengineering.contller;

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
@RequestMapping("/admin")
public class AdminController {

    private final ForumBoardService boardService;
    private final UserService userService;

    public AdminController(UserService userService, ForumBoardService boardService) {
        this.userService = userService;
        this.boardService = boardService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/status")
    public void updateUserStatus(@PathVariable Long userId, @RequestBody Integer status) {
        userService.updateUserStatus(userId, status);
    }

    @GetMapping("/forums")
    public ResponseEntity<List<ForumBoard>> getBoards() {
        return ResponseEntity.ok(boardService.getBoards());
    }

    @PostMapping("/forums")
    public ResponseEntity<ForumBoard> addBoard(@RequestBody ForumBoard forumBoard) {
        boardService.addBoard(forumBoard);
        return ResponseEntity.ok(forumBoard);
    }

    @PutMapping("/forums/{forumId}")
    public void updateForum(@PathVariable Integer forumId, @RequestBody ForumBoard forumBoard) {
        boardService.updateBoard(forumBoard);
    }

    @PutMapping("/forums/{forumId}/status")
    public void updateForumStatus(@PathVariable Integer forumId, @RequestBody Integer status) {
        boardService.updateBoardStatus(forumId, status);
    }

    @PutMapping("/forums/sort")
    public void sortForums() {
        boardService.sortBoards(null);
    }

    @DeleteMapping("/forums/{forumId}")
    public void deleteForum(@PathVariable Integer forumId) {
        boardService.deleteBoard(forumId);
    }
}