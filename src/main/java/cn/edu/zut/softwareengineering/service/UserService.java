package cn.edu.zut.softwareengineering.service;



import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface UserService {

    User login(User user);

    User register(User user);

    User logout(User user);

    User getProfile();

    void updateProfile(User user);
    
    void updatePassword(User user);

    List<ForumPost> getPosts(User user);
    
    List<ForumComment> getComments(User user);

    List<User> getUsers();

    void updateUserStatus(Long userId, Integer status);

}
