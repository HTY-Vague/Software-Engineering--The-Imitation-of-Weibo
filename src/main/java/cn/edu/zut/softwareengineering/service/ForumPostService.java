package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.ForumPost;

import java.util.List;

public interface ForumPostService {
    ForumPost createPost(ForumPost post);

    ForumPost getPostDetail(Integer postId);

    List<ForumPost> getPostList(Integer boardId, String keyword, Integer page, Integer size);

    void updatePost(ForumPost post);

    void deletePost(Integer postId);

    void updateOwnPost(ForumPost post, Integer operatorId);

    void deleteOwnPost(Integer postId, Integer operatorId);

    void toggleTop(Integer postId, Integer isTop);
}
