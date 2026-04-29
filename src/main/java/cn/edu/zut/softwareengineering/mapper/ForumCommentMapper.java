package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface ForumCommentMapper {
    void insertComment(ForumComment comment);

    ForumComment getCommentById(Integer commentId);

    List<ForumComment> getCommentsByPostId(Integer postId);

    void deleteComment(Integer commentId);

    void deleteCommentsByPostId(Integer postId);

    void incrementLikeCount(Integer commentId);

    void decrementLikeCount(Integer commentId);

    Integer countComments();
}
