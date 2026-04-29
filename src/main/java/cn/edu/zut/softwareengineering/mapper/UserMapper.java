package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User login(User user);
    void register(User user);
    void logout(Integer userId);
    User getUserById(Integer userId);
    User getUserByUsername(String username);
    void updateProfile(User user);
    void updatePassword(@Param("userId") Integer userId, @Param("newPassword") String newPassword);
    List<ForumPost> getUserPosts(Integer userId);
    List<ForumComment> getUserComments(Integer userId);
    List<User> getAllUsers();
    void updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);
    Integer countUsers();
    Integer countActiveUsers();
}
