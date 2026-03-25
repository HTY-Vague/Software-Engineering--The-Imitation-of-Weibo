package cn.edu.zut.softwareengineering.mapper;
import java.util.List;

import cn.edu.zut.softwareengineering.entity.User;

public interface UserMapper {
    public User login(User user);

    public User register(User user);

    public User logout(User user);

    public User getProfile(User user);

    public void updateProfile(User user);

    public void updatePassword(User user);

    public List<ForumPost> getPosts(User user);
    
    public List<ForumComment> getComments(User user);

    public List<User> getUsers();

    public void updateUserStatus(Long userId, Integer status);
}