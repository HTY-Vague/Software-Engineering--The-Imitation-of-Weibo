package cn.edu.zut.softwareengineering.contller;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@RestController
@RequestMapping("/forum")// 论坛板块相关接口
public class ForumBoardController {
    private final ForumBoardService forumBoardService;

    public ForumBoardController(ForumBoardService forumBoardService) {
        this.forumBoardService = forumBoardService;
    }

    @PostMapping("/boards")// 获取所有论坛板块
    public ResponseEntity<List<ForumBoard>> getBoards() {

        return ResponseEntity.ok(forumBoardService.getBoards());

    }


    @PostMapping("/board/posts")// 获取论坛板块下的帖子
    public ResponseEntity<List<ForumPost>> getBoardPosts(@RequestBody ForumBoard forumBoard) {
        
        return ResponseEntity.ok(forumBoardService.getBoardPosts(forumBoard));
        
    }

}
