package cn.edu.zut.softwareengineering.mapper;
import java.util.List;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.User;

public interface UserMapper {
    public User login(User user);

    public User register(User user);
    
    public void logout(Long userId);

    public User getUserById(Long userId);

    public void updateProfile(User user);

    public void updatePassword(Long userId, String oldPassword, String newPassword);

    public List<ForumPost> getUserPosts(Long userId);

    public List<ForumCommentMapper> getUserComments(Long userId);

    public List<User> getAllUsers();

    public void updateUserStatus(Long userId, Integer status);
}