package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.service.ForumLikeService;
import cn.edu.zut.softwareengineering.utils.ResultUtil;
import cn.edu.zut.softwareengineering.utils.SecurityUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
public class ForumLikeController {

    private final ForumLikeService forumLikeService;

    public ForumLikeController(ForumLikeService forumLikeService) {
        this.forumLikeService = forumLikeService;
    }

    @PostMapping("/like")
    public ResultUtil.Result<Void> like(@RequestParam Integer targetType,
                                        @RequestParam Integer targetId) {
        forumLikeService.like(targetType, targetId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/unlike")
    public ResultUtil.Result<Void> unlike(@RequestParam Integer targetType,
                                          @RequestParam Integer targetId) {
        forumLikeService.unlike(targetType, targetId, SecurityUtil.getCurrentUserId());
        return ResultUtil.success();
    }

    @PostMapping("/status")
    public ResultUtil.Result<Boolean> hasLiked(@RequestParam Integer targetType,
                                               @RequestParam Integer targetId) {
        return ResultUtil.success(forumLikeService.hasLiked(targetType, targetId, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/count")
    public ResultUtil.Result<Integer> countLikes(@RequestParam Integer targetType,
                                                 @RequestParam Integer targetId) {
        return ResultUtil.success(forumLikeService.countLikes(targetType, targetId));
    }
}
