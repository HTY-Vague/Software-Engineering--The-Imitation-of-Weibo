package cn.edu.zut.softwareengineering.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String principalText) || !principalText.contains(":")) {
            throw new IllegalStateException("用户身份信息无效");
        }
        return Integer.valueOf(principalText.substring(0, principalText.indexOf(':')));
    }
}
