package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.UserMapper;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统计分析服务实现。
 *
 * 改造说明：
 *   历史版本读取 Spark 离线跑批写入的 stat_* 统计表。移除 Spark 链路后，
 *   改为注入业务 Mapper 对业务表实时 COUNT 聚合，字段与响应结构保持不变：
 *     - userCount：用户总数（sys_user 全表）；
 *     - activeUserCount：正常状态用户数（sys_user.status = 1，原为 Spark
 *       统计的活跃用户数，口径弱化已在迁移方案附录 B 记录）；
 *     - enabledBoardCount：启用状态板块数（forum_board.status = 1）；
 *     - postCount / commentCount：对应业务表有效记录数（status = 1）；
 *     - likeCount：forum_like 全部点赞记录数；
 *     - updateTime：当前时间，表示"实时统计"（原为跑批时间）。
 *   数据量为课程演示规模（千级），COUNT 聚合性能无压力。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserMapper userMapper;
    private final ForumBoardMapper boardMapper;
    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final ForumLikeMapper likeMapper;

    public StatisticsServiceImpl(UserMapper userMapper,
                                 ForumBoardMapper boardMapper,
                                 ForumPostMapper postMapper,
                                 ForumCommentMapper commentMapper,
                                 ForumLikeMapper likeMapper) {
        this.userMapper = userMapper;
        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.likeMapper = likeMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("userCount", safe(userMapper.countUsers()));
        overview.put("activeUserCount", safe(userMapper.countActiveUsers()));
        overview.put("enabledBoardCount", safe(boardMapper.countEnabledBoards()));
        overview.put("postCount", safe(postMapper.countPosts()));
        overview.put("commentCount", safe(commentMapper.countComments()));
        overview.put("likeCount", safe(likeMapper.countLikes()));
        overview.put("updateTime", LocalDateTime.now().format(TIME_FORMATTER));
        return overview;
    }

    private Integer safe(Integer value) {
        return value == null ? 0 : value;
    }
}
