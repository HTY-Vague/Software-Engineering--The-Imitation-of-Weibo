package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumLike;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.service.ForumLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForumLikeServiceImpl implements ForumLikeService {

    private final ForumLikeMapper forumLikeMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;

    public ForumLikeServiceImpl(ForumLikeMapper forumLikeMapper,
                                ForumPostMapper forumPostMapper,
                                ForumCommentMapper forumCommentMapper) {
        this.forumLikeMapper = forumLikeMapper;
        this.forumPostMapper = forumPostMapper;
        this.forumCommentMapper = forumCommentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Integer targetType, Integer targetId, Integer userId) {
        validateTarget(targetType, targetId, userId);
        if (forumLikeMapper.getUserLike(targetType, targetId, userId) != null) {
            throw new IllegalStateException("不能重复点赞");
        }
        ForumLike like = new ForumLike();
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setUserId(userId);
        forumLikeMapper.insertLike(like);
        updateCount(targetType, targetId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Integer targetType, Integer targetId, Integer userId) {
        validateTarget(targetType, targetId, userId);
        if (forumLikeMapper.getUserLike(targetType, targetId, userId) == null) {
            return;
        }
        forumLikeMapper.deleteLike(targetType, targetId, userId);
        updateCount(targetType, targetId, false);
    }

    @Override
    public boolean hasLiked(Integer targetType, Integer targetId, Integer userId) {
        validateTarget(targetType, targetId, userId);
        return forumLikeMapper.getUserLike(targetType, targetId, userId) != null;
    }

    @Override
    public Integer countLikes(Integer targetType, Integer targetId) {
        if (targetType == null || targetId == null) {
            throw new IllegalArgumentException("targetType 和 targetId 不能为空");
        }
        return forumLikeMapper.countByTarget(targetType, targetId);
    }

    private void validateTarget(Integer targetType, Integer targetId, Integer userId) {
        if (targetType == null || targetId == null || userId == null) {
            throw new IllegalArgumentException("targetType、targetId 和 userId 不能为空");
        }
        if (targetType != 1 && targetType != 2) {
            throw new IllegalArgumentException("targetType 只能为 1 或 2");
        }
    }

    private void updateCount(Integer targetType, Integer targetId, boolean increment) {
        if (targetType == 1) {
            if (increment) {
                forumPostMapper.incrementLikeCount(targetId);
            } else {
                forumPostMapper.decrementLikeCount(targetId);
            }
        } else if (increment) {
            forumCommentMapper.incrementLikeCount(targetId);
        } else {
            forumCommentMapper.decrementLikeCount(targetId);
        }
    }
}
