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
import cn.edu.zut.softwareengineering.entity.ForumLike;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.service.ForumLikeService;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ForumBoardService boardService;
    private final ForumCommentService commentService;
    private final ForumLikeService likeService;
    private final ForumPostService postService;
    private final UserService userService;
    
    public AdminController(UserService userService , ForumBoardService forumService, ForumCommentService commentService, ForumLikeService likeService, ForumPostService postService) {
        this.userService = userService;

        this.boardService = forumService;

        this.commentService = commentService;

        this.likeService = likeService;

        this.postService = postService;
    }

    @GetMapping("/users")//获取用户列表
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PutMapping("/users/{userId}/status")//启用/禁用用户
    public void updateUserStatus(@PathVariable Long userId, @RequestBody Integer status) {
        
        userService.updateUserStatus(userId, status);
    
    }

    @GetMapping("/forums")//获取论坛列表
    public ResponseEntity<List<ForumBoard>> getBoards() {

        return ResponseEntity.ok(boardService.getBoards());

    }

    @PostMapping("/forums")//添加论坛
    public ResponseEntity<ForumBoard> addBoard(@RequestBody ForumBoard forumBoard) {
        
        boardService.addBoard(forumBoard);
        return ResponseEntity.ok(forumBoard);

    }
    
    @PutMapping("/forums/{forumId}")//更新论坛
    public void updateForum(@PathVariable Integer forumId, @RequestBody ForumBoard forumBoard) {
        boardService.updateBoard(forumBoard);
    }

    @PutMapping("/forums/{forumId}/status")//启用/禁用论坛
    public void updateForumStatus(@PathVariable Integer forumId, @RequestBody Integer status) {

        boardService.updateBoardStatus(forumId, status);

    }

    @PutMapping("/forums/sort")//排序论坛
    public void sortForums() {

        boardService.sortBoards(null);
    
    }

    @DeleteMapping("/forums/{forumId}")//删除论坛
    public void deleteForum(@PathVariable Integer forumId) {

        boardService.deleteBoard(forumId);
    
    }
}