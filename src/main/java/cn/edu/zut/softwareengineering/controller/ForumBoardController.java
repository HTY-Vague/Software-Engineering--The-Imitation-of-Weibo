package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
@RestController
@RequestMapping("/forum")// 论坛板块相关接口
@Tag(name = "板块接口", description = "论坛板块浏览和板块下帖子查询")
public class ForumBoardController {
    private final ForumBoardService forumBoardService;

    public ForumBoardController(ForumBoardService forumBoardService) {
        this.forumBoardService = forumBoardService;
    }

    @PostMapping("/boards")// 获取所有论坛板块
    @Operation(summary = "获取论坛板块列表")
    public ResultUtil.Result<List<ForumBoard>> getBoards() {

        return ResultUtil.success(forumBoardService.getBoards());

    }


    @PostMapping("/board/posts")// 获取论坛板块下的帖子
    @Operation(summary = "获取指定板块下的帖子")
    public ResultUtil.Result<List<ForumPost>> getBoardPosts(@RequestBody ForumBoard forumBoard) {
        
        return ResultUtil.success(forumBoardService.getBoardPosts(forumBoard));
        
    }

}
