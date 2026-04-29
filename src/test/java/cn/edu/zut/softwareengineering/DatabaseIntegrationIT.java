package cn.edu.zut.softwareengineering;

import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
@SpringBootTest
class DatabaseIntegrationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ForumBoardMapper forumBoardMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Autowired
    private ForumLikeMapper forumLikeMapper;

    @Test
    void databaseConnectionShouldRespond() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertEquals(1, result);
    }

    @Test
    void coreMapperReadQueriesShouldMatchSchema() {
        assertNonNegative(userMapper.countUsers());
        assertNonNegative(userMapper.countActiveUsers());
        assertNonNegative(forumBoardMapper.countEnabledBoards());
        assertNonNegative(forumPostMapper.countPosts());
        assertNonNegative(forumCommentMapper.countComments());
        assertNonNegative(forumLikeMapper.countLikes());
        assertNotNull(forumPostMapper.getHotPosts(5));
    }

    private void assertNonNegative(Integer count) {
        assertNotNull(count);
        assertTrue(count >= 0);
    }
}
