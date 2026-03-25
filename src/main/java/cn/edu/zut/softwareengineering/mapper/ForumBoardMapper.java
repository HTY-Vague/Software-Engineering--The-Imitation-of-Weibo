package cn.edu.zut.softwareengineering.mapper;

import java.util.List;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;

public interface ForumBoardMapper {

    public List<ForumBoard> getBoards();

    public void addBoard(ForumBoard forumBoard);

    public void updateBoard(ForumBoard forumBoard);

    public void updateBoardStatus(Integer forumId, Integer status);

    public void sortBoards(List<Long> boardIds);

    public void deleteBoard(Integer boardId);

    public List<ForumPost> getBoardPosts(Integer boardId);
}
