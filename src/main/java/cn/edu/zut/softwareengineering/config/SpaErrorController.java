package cn.edu.zut.softwareengineering.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA 前端路由兜底。
 *
 * Vue Router 使用 history 模式，浏览器直接访问或刷新 /auth/login、/post/1、/admin/users
 * 等前端路由路径时，后端没有对应的 GET 映射会触发 404 或 405。
 * 这里把 GET 请求的 404/405 转发到 /index.html，由 Vue Router 在前端接管页面渲染。
 *
 * 后端 API 绝大多数是 POST/PUT/DELETE，浏览器地址栏导航只会发 GET，
 * 因此 GET 的 404/405 几乎都是前端路由，安全地返回 index.html。
 */
@Controller
public class SpaErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object statusObj = request.getAttribute("jakarta.servlet.error.status_code");
        String method = request.getMethod();

        if ("GET".equals(method) && statusObj != null) {
            int status = (Integer) statusObj;
            // 404 = 路径无映射；405 = 有映射但方法不支持（如 POST 接口被 GET 访问）
            // 两种情况都视为前端路由，返回 index.html
            if (status == HttpStatus.NOT_FOUND.value() || status == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                return "forward:/index.html";
            }
        }

        // 其余错误（500 等）走默认错误页
        return "error";
    }
}
