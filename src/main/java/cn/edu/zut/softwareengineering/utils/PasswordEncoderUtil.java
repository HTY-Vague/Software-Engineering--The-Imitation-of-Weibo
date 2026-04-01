package cn.edu.zut.softwareengineering.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
public class PasswordEncoderUtil {

    /**
     * BCrypt编码器实例
     * 强度设置为10,表示加密时会进行2^10次哈希计算
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private PasswordEncoderUtil() {
    }

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return true-密码匹配, false-密码不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 获取BCrypt编码器实例
     *
     * @return BCryptPasswordEncoder实例
     */
    public static BCryptPasswordEncoder getEncoder() {
        return encoder;
    }
}
