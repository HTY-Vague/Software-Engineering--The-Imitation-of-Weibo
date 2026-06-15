package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface ForumCommentService {
    ForumComment createComment(ForumComment comment);

    List<ForumComment> getComments(Integer postId);

    List<ForumComment> getAllComments();

    void deleteComment(Integer commentId);

    void deleteOwnComment(Integer commentId, Integer operatorId);
}
