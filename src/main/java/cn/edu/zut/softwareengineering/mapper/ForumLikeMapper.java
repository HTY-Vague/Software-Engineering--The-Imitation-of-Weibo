package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.ForumLike;
import org.apache.ibatis.annotations.Param;

public interface ForumLikeMapper {
    void insertLike(ForumLike like);

    void deleteLike(@Param("targetType") Integer targetType,
                    @Param("targetId") Integer targetId,
                    @Param("userId") Integer userId);

    ForumLike getUserLike(@Param("targetType") Integer targetType,
                          @Param("targetId") Integer targetId,
                          @Param("userId") Integer userId);

    Integer countByTarget(@Param("targetType") Integer targetType,
                          @Param("targetId") Integer targetId);

    void deleteByTarget(@Param("targetType") Integer targetType,
                        @Param("targetId") Integer targetId);

    Integer countLikes();
}
