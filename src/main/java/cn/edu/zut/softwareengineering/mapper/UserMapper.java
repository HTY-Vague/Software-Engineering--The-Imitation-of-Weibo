package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User login(User user);
    User register(User user);
    void logout(Long userId);
    User getUserById(Long userId);
    void updateProfile(User user);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    List<ForumPost> getUserPosts(Long userId);
    List<ForumComment> getUserComments(Long userId);
    List<User> getAllUsers();
    void updateUserStatus(Long userId, Integer status);
}