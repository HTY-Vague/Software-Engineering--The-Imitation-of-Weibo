package cn.edu.zut.softwareengineering.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.IOException;

/**
 * 前端静态资源配置。
 *
 * 后端 jar 内嵌前端 Vue 构建产物（src/main/resources/static/），
 * 需要让 Vue Router history 模式的路由（如 /auth/login、/post/1）
 * 都能正确返回 index.html，由前端路由接管页面渲染。
 *
 * 策略：除已知的后端 API 路径和静态资源路径外，
 *       未匹配的 GET 请求转发到 index.html。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径映射到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
