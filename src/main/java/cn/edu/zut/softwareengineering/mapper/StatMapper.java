package cn.edu.zut.softwareengineering.mapper;

import cn.edu.zut.softwareengineering.entity.StatActiveUser;
import cn.edu.zut.softwareengineering.entity.StatBoardActive;
import cn.edu.zut.softwareengineering.entity.StatContentQuality;
import cn.edu.zut.softwareengineering.entity.StatDailyTrend;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.StatOverview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计数据 Mapper，读取 Spark 跑批写入的 stat_* 表。
 * 只读，不提供写入方法（写入由 Spark 程序负责）。
 */
@Mapper
public interface StatMapper {

    StatOverview getOverview();

    List<StatHotPost> getHotPosts(@Param("limit") Integer limit);

    List<StatBoardActive> getBoardActive();

    List<StatActiveUser> getActiveUsers(@Param("limit") Integer limit);

    List<StatContentQuality> getContentQuality(@Param("qualityType") String qualityType);

    List<StatDailyTrend> getDailyTrend(@Param("days") Integer days);

    StatDailyTrend getLatestDailyTrend();
}
