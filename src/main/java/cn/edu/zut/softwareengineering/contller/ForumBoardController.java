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
@RequestMapping("/forum")
public class ForumBoardController {
    private final ForumBoardService forumBoardService;

    public ForumBoardController(ForumBoardService forumBoardService) {
        this.forumBoardService = forumBoardService;
    }

    @PostMapping("/boards")
    public ResponseEntity<List<ForumBoard>> getBoards() {

        return ResponseEntity.ok(forumBoardService.getBoards());

    }


    @PostMapping("/board/posts")
    public ResponseEntity<List<ForumPost>> getBoardPosts(@RequestBody ForumBoard forumBoard) {
        
        return ResponseEntity.ok(forumBoardService.getBoardPosts(forumBoard));
    }

}
