package cn.edu.zut.softwareengineering.service;

import java.util.List;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;

public interface ForumBoardService {
    /**
    * 获取所有论坛版块
    * @return
    */
    List<ForumBoard> getBoards();
    /**
     * 添加论坛版块(Admin)
     * @param forumBoard
     */
    void addBoard(ForumBoard forumBoard);
    /**
     * 更新论坛版块(Admin)
     * @param forumBoard
     */
    void updateBoard(ForumBoard forumBoard);
    /**
     * 启用/禁用论坛版块(Admin)
     * @param forumId
     * @param status
     */
    void updateBoardStatus(Integer forumId, Integer status);
    /**
     * 排序论坛版块(Admin)
     * @param boardIds
     */
    void sortBoards(List<Long> boardIds);
    /**
     * 删除论坛版块(Admin)
     * @param boardId
     */
    void deleteBoard(Integer boardId);
    /**
     * 获取论坛版块下的帖子
     * @param forumBoard
     * @return
     */
    List<ForumPost> getBoardPosts(ForumBoard forumBoard);

}
