package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.mapper.UserMapper;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.ForumComment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
	private final UserMapper userMapper;

	public UserServiceImpl(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	@Override
	public User login(User user) {
		return userMapper.login(user);
	}

	@Override
	public User register(User user) {
		return userMapper.register(user);
	}

	@Override
	public void logout(Long userId) {
		if (userId == null) {
			return;
		}
		userMapper.logout(userId);
	}

	@Override
	public User getProfile(Long userId) {
		if (userId == null) {
			return null;
		}
		return userMapper.getUserById(userId);
	}

	@Override
	public void updateProfile(User user) {
		userMapper.updateProfile(user);
	}

	@Override
	public void updatePassword(Long userId, String oldPassword, String newPassword) {
		if (userId == null || oldPassword == null || newPassword == null) {
			return;
		}
		userMapper.updatePassword(userId, oldPassword, newPassword);
	}

	@Override
	public List<ForumPost> getPosts(Long userId) {
		if (userId == null) {
			return List.of();
		}
		return userMapper.getUserPosts(userId);
	}

	@Override
	public void deletePost(Long postId) {
		throw new UnsupportedOperationException("deletePost is not supported by UserMapper");
	}

	@Override
	public List<ForumComment> getComments(Long userId) {
		if (userId == null) {
			return List.of();
		}
		return userMapper.getUserComments(userId);
	}

	@Override
	public List<User> getAllUsers() {
		return userMapper.getAllUsers();
	}

	@Override
	public void updateUserStatus(Long userId, Integer status) {
		userMapper.updateUserStatus(userId, status);
	}

}
