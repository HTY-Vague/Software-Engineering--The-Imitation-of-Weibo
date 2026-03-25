package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface UserService {
    /**
     * 用户登录
     * @param user
     * @return
     */
    User login(User user);
    /**
     * 用户注册
     * @param user
     * @return
     */
    User register(User user);
    /**
     * 用户注销
        * @param userId
     */
        void logout(Long userId);
    /**
     * 获取用户信息
        * @param userId
     * @return
     */
        User getProfile(Long userId);
    /**
     * 更新用户信息
     * @param user
     */
    void updateProfile(User user);
    /**
     * 更新用户密码
        * @param userId
        * @param oldPassword
        * @param newPassword
     */
        void updatePassword(Long userId, String oldPassword, String newPassword);
    /**
     * 获取用户帖子
        * @param userId
     * @return
     */
        List<ForumPost> getPosts(Long userId);
    /**
     * 删除用户帖子
        * @param postId
     */
        void deletePost(Long postId);
    /**
     * 获取用户评论
        * @param userId
     * @return
     */
        List<ForumComment> getComments(Long userId);
    /**
     * 获取用户列表(Admin)
     * @return
     */
    List<User> getAllUsers();
    /**
     * 启用/禁用用户(Admin)
     * @param userId
     * @param status
     */
    void updateUserStatus(Long userId, Integer status);

}
