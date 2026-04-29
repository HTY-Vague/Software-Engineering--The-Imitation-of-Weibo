package cn.edu.zut.softwareengineering.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "仿微博论坛系统后端接口",
                version = "0.0.1",
                description = "课程设计论坛系统后端 API，包含认证、用户、板块、帖子、评论、点赞和后台管理接口。",
                license = @License(name = "Learning Project")
        )
)
public class OpenApiConfig {
}
