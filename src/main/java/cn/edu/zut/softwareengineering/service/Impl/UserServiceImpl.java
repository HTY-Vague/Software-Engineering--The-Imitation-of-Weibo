package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.mapper.UserMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.utils.PasswordEncoderUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
	private final UserMapper userMapper;
	private final ForumPostMapper forumPostMapper;
	private final ForumLikeMapper forumLikeMapper;

	public UserServiceImpl(UserMapper userMapper, ForumPostMapper forumPostMapper, ForumLikeMapper forumLikeMapper) {
		this.userMapper = userMapper;
		this.forumPostMapper = forumPostMapper;
		this.forumLikeMapper = forumLikeMapper;
	}

	@Override
	public User login(User user) {
		if (user == null || user.getUsername() == null || user.getUsername().isBlank() || user.getPassword() == null || user.getPassword().isBlank()) {
			throw new IllegalArgumentException("用户名和密码不能为空");
		}
		User dbUser = userMapper.login(user);
		if (dbUser == null || !passwordMatches(user.getPassword(), dbUser.getPassword())) {
			throw new IllegalArgumentException("用户名或密码错误");
		}
		dbUser.setPassword(null);
		return dbUser;
	}

	@Override
	public User register(User user) {
		if (user == null || user.getUsername() == null || user.getUsername().isBlank() || user.getPassword() == null || user.getPassword().isBlank()) {
			throw new IllegalArgumentException("用户名和密码不能为空");
		}
		if (userMapper.getUserByUsername(user.getUsername()) != null) {
			throw new IllegalArgumentException("用户名已存在");
		}
		if (user.getNickname() == null || user.getNickname().isBlank()) {
			user.setNickname(user.getUsername());
		}
		user.setRole("USER");
		user.setPassword(PasswordEncoderUtil.encode(user.getPassword()));
		userMapper.register(user);
		user.setPassword(null);
		return user;
	}

	@Override
	public void logout(Integer userId) {
		if (userId == null) {
			return;
		}
		userMapper.logout(userId);
	}

	@Override
	public User getProfile(Integer userId) {
		if (userId == null) {
			return null;
		}
		User user = userMapper.getUserById(userId);
		if (user != null) {
			user.setPassword(null);
		}
		return user;
	}

	@Override
	public void updateProfile(User user) {
		if (user == null || user.getUserId() == null) {
			throw new IllegalArgumentException("userId 不能为空");
		}
		userMapper.updateProfile(user);
	}

	@Override
	public void updatePassword(Integer userId, String oldPassword, String newPassword) {
		if (userId == null || oldPassword == null || newPassword == null) {
			throw new IllegalArgumentException("密码参数不能为空");
		}
		User user = userMapper.getUserById(userId);
		if (user == null || !passwordMatches(oldPassword, user.getPassword())) {
			throw new IllegalArgumentException("旧密码错误");
		}
		userMapper.updatePassword(userId, PasswordEncoderUtil.encode(newPassword));
	}

	@Override
	public List<ForumPost> getPosts(Integer userId) {
		if (userId == null) {
			return List.of();
		}
		return userMapper.getUserPosts(userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePost(Integer postId) {
		if (postId == null) {
			throw new IllegalArgumentException("postId 不能为空");
		}
		forumPostMapper.deletePost(postId);
		forumLikeMapper.deleteByTarget(1, postId);
	}

	@Override
	public List<ForumComment> getComments(Integer userId) {
		if (userId == null) {
			return List.of();
		}
		return userMapper.getUserComments(userId);
	}

	@Override
	public List<User> getAllUsers() {
		List<User> users = userMapper.getAllUsers();
		users.forEach(user -> user.setPassword(null));
		return users;
	}

	@Override
	public void updateUserStatus(Integer userId, Integer status) {
		userMapper.updateUserStatus(userId, status);
	}

	private boolean passwordMatches(String rawPassword, String storedPassword) {
		return PasswordEncoderUtil.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
	}

}
