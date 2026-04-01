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
    void logout(Integer userId);
    User getUserById(Integer userId);
    void updateProfile(User user);
    void updatePassword(Integer userId, String oldPassword, String newPassword);
    List<ForumPost> getUserPosts(Integer userId);
    List<ForumComment> getUserComments(Integer userId);
    List<User> getAllUsers();
    void updateUserStatus(Integer userId, Integer status);
}