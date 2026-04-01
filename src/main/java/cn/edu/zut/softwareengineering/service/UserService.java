package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface UserService {
    User login(User user);
    User register(User user);
    void logout(Integer userId);
    User getProfile(Integer userId);
    void updateProfile(User user);
    void updatePassword(Integer userId, String oldPassword, String newPassword);
    List<ForumPost> getPosts(Integer userId);
    void deletePost(Integer postId);
    List<ForumComment> getComments(Integer userId);
    List<User> getAllUsers();
    void updateUserStatus(Integer userId, Integer status);
}
