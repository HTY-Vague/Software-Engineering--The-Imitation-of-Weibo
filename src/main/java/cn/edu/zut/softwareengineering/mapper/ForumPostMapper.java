package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ForumPostMapper {
    void insertPost(ForumPost post);

    ForumPost getPostById(Integer postId);

    List<ForumPost> getPosts(@Param("boardId") Integer boardId,
                             @Param("keyword") String keyword,
                             @Param("offset") Integer offset,
                             @Param("size") Integer size);

    void incrementViewCount(Integer postId);

    void updatePost(ForumPost post);

    void deletePost(Integer postId);

    void updateTop(@Param("postId") Integer postId, @Param("isTop") Integer isTop);

    void incrementCommentCount(Integer postId);

    void decrementCommentCount(Integer postId);

    void incrementLikeCount(Integer postId);

    void decrementLikeCount(Integer postId);
}
