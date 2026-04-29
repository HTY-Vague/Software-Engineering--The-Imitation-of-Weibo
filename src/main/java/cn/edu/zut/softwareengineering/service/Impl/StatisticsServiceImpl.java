package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.UserMapper;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final ForumBoardMapper forumBoardMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumLikeMapper forumLikeMapper;

    public StatisticsServiceImpl(UserMapper userMapper,
                                 ForumBoardMapper forumBoardMapper,
                                 ForumPostMapper forumPostMapper,
                                 ForumCommentMapper forumCommentMapper,
                                 ForumLikeMapper forumLikeMapper) {
        this.userMapper = userMapper;
        this.forumBoardMapper = forumBoardMapper;
        this.forumPostMapper = forumPostMapper;
        this.forumCommentMapper = forumCommentMapper;
        this.forumLikeMapper = forumLikeMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("userCount", safeCount(userMapper.countUsers()));
        overview.put("activeUserCount", safeCount(userMapper.countActiveUsers()));
        overview.put("enabledBoardCount", safeCount(forumBoardMapper.countEnabledBoards()));
        overview.put("postCount", safeCount(forumPostMapper.countPosts()));
        overview.put("commentCount", safeCount(forumCommentMapper.countComments()));
        overview.put("likeCount", safeCount(forumLikeMapper.countLikes()));
        return overview;
    }

    @Override
    public List<ForumPost> getHotPosts(Integer limit) {
        int queryLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        return forumPostMapper.getHotPosts(queryLimit);
    }

    private Integer safeCount(Integer value) {
        return value == null ? 0 : value;
    }
}
