package cn.edu.zut.softwareengineering.service;

public interface ForumLikeService {
    void like(Integer targetType, Integer targetId, Integer userId);

    void unlike(Integer targetType, Integer targetId, Integer userId);

    boolean hasLiked(Integer targetType, Integer targetId, Integer userId);

    Integer countLikes(Integer targetType, Integer targetId);
}
