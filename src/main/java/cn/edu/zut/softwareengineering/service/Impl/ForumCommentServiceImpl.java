package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForumCommentServiceImpl implements ForumCommentService {

    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumLikeMapper forumLikeMapper;

    public ForumCommentServiceImpl(ForumCommentMapper forumCommentMapper,
                                   ForumPostMapper forumPostMapper,
                                   ForumLikeMapper forumLikeMapper) {
        this.forumCommentMapper = forumCommentMapper;
        this.forumPostMapper = forumPostMapper;
        this.forumLikeMapper = forumLikeMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumComment createComment(ForumComment comment) {
        if (comment == null || comment.getPostId() == null || comment.getUserId() == null) {
            throw new IllegalArgumentException("postId 和 userId 不能为空");
        }
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        forumCommentMapper.insertComment(comment);
        forumPostMapper.incrementCommentCount(comment.getPostId());
        return comment;
    }

    @Override
    public List<ForumComment> getComments(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("postId 不能为空");
        }
        List<ForumComment> comments = forumCommentMapper.getCommentsByPostId(postId);
        Map<Integer, ForumComment> rootMap = new LinkedHashMap<>();
        List<ForumComment> roots = new ArrayList<>();
        for (ForumComment comment : comments) {
            comment.setChildren(new ArrayList<>());
            if (comment.getParentCommentId() == null) {
                rootMap.put(comment.getCommentId(), comment);
                roots.add(comment);
            }
        }
        for (ForumComment comment : comments) {
            if (comment.getParentCommentId() != null) {
                ForumComment parent = rootMap.get(comment.getParentCommentId());
                if (parent != null) {
                    parent.getChildren().add(comment);
                } else {
                    roots.add(comment);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Integer commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("commentId 不能为空");
        }
        ForumComment comment = forumCommentMapper.getCommentById(commentId);
        if (comment == null) {
            return;
        }
        forumCommentMapper.deleteComment(commentId);
        forumLikeMapper.deleteByTarget(2, commentId);
        forumPostMapper.decrementCommentCount(comment.getPostId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwnComment(Integer commentId, Integer operatorId) {
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        ForumComment comment = forumCommentMapper.getCommentById(commentId);
        if (comment == null) {
            return;
        }
        if (!operatorId.equals(comment.getUserId())) {
            throw new IllegalStateException("只能删除自己的评论");
        }
        deleteComment(commentId);
    }
}
