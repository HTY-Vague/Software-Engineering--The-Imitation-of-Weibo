package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.ForumComment;

import java.util.List;

public interface ForumCommentService {
    ForumComment createComment(ForumComment comment);

    List<ForumComment> getComments(Integer postId);

    void deleteComment(Integer commentId);
}
