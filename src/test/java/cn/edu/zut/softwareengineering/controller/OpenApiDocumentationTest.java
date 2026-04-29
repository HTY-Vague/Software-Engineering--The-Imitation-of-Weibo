package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.service.ForumLikeService;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import cn.edu.zut.softwareengineering.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ForumPostService forumPostService;

    @MockBean
    private ForumBoardService forumBoardService;

    @MockBean
    private ForumCommentService forumCommentService;

    @MockBean
    private ForumLikeService forumLikeService;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("OpenAPI文档可生成并包含核心接口")
    void openApiDocsContainCoreEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("仿微博论坛系统后端接口"))
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/posts/create']").exists())
                .andExpect(jsonPath("$.paths['/admin/statistics/overview']").exists());
    }
}
