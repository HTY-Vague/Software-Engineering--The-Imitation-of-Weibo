package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    private final ForumPostMapper forumPostMapper;
    private final ForumLikeMapper forumLikeMapper;

    public ForumPostServiceImpl(ForumPostMapper forumPostMapper, ForumLikeMapper forumLikeMapper) {
        this.forumPostMapper = forumPostMapper;
        this.forumLikeMapper = forumLikeMapper;
    }

    @Override
    public ForumPost createPost(ForumPost post) {
        if (post == null || post.getBoardId() == null || post.getAuthorId() == null) {
            throw new IllegalArgumentException("boardId 和 authorId 不能为空");
        }
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (post.getContent() == null || post.getContent().isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        forumPostMapper.insertPost(post);
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost getPostDetail(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("postId 不能为空");
        }
        forumPostMapper.incrementViewCount(postId);
        return forumPostMapper.getPostById(postId);
    }

    @Override
    public List<ForumPost> getPostList(Integer boardId, String keyword, Integer page, Integer size) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;
        int offset = (currentPage - 1) * pageSize;
        return forumPostMapper.getPosts(boardId, keyword, offset, pageSize);
    }

    @Override
    public void updatePost(ForumPost post) {
        if (post == null || post.getPostId() == null) {
            throw new IllegalArgumentException("postId 不能为空");
        }
        forumPostMapper.updatePost(post);
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
    public void updateOwnPost(ForumPost post, Integer operatorId) {
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        ensurePostOwner(post == null ? null : post.getPostId(), operatorId);
        updatePost(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwnPost(Integer postId, Integer operatorId) {
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        ensurePostOwner(postId, operatorId);
        deletePost(postId);
    }

    @Override
    public void toggleTop(Integer postId, Integer isTop) {
        if (postId == null || isTop == null) {
            throw new IllegalArgumentException("postId 和 isTop 不能为空");
        }
        forumPostMapper.updateTop(postId, isTop);
    }

    private void ensurePostOwner(Integer postId, Integer operatorId) {
        if (postId == null) {
            throw new IllegalArgumentException("postId 不能为空");
        }
        ForumPost post = forumPostMapper.getPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (!operatorId.equals(post.getAuthorId())) {
            throw new IllegalStateException("只能操作自己的帖子");
        }
    }
}
