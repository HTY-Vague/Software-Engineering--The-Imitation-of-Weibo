package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.ForumPost;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getOverview();

    List<ForumPost> getHotPosts(Integer limit);
}
