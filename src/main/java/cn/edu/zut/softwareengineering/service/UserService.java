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
     * @param user
     * @return
     */
    User logout(User user);
    /**
     * 获取用户信息
     * @return
     */
    User getProfile();
    /**
     * 更新用户信息
     * @param user
     */
    void updateProfile(User user);
    /**
     * 更新用户密码
     * @param user
     */
    void updatePassword(User user);
    /**
     * 获取用户帖子
     * @param user
     * @return
     */
    List<ForumPost> getPosts(User user);
    /**
     * 删除用户帖子
     * @param user
     * @return
     */
    void deletePost(User user);
    /**
     * 获取用户评论
     * @param user
     * @return
     */
    List<ForumComment> getComments(User user);
    /**
     * 获取用户列表(Admin)
     * @return
     */
    List<User> getUsers();
    /**
     * 启用/禁用用户(Admin)
     * @param userId
     * @param status
     */
    void updateUserStatus(Long userId, Integer status);

}
