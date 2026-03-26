package cn.edu.zut.softwareengineering.service.Impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.service.ForumBoardService;

@Service
public class ForumBoardServiceImpl implements ForumBoardService{
    private final ForumBoardMapper forumBoardMapper;

    public ForumBoardServiceImpl(ForumBoardMapper forumBoardMapper) {
        this.forumBoardMapper = forumBoardMapper;
    }


    @Override
    public List<ForumBoard> getBoards() {

        return forumBoardMapper.getBoards();
        
    }

    @Override
    public void addBoard(ForumBoard forumBoard) {

        forumBoardMapper.addBoard(forumBoard);

    }

    @Override
    public void updateBoard(ForumBoard forumBoard){

        forumBoardMapper.updateBoard(forumBoard);

    }

    @Override
    public void updateBoardStatus(Integer boardId, Integer status) {

        forumBoardMapper.updateBoardStatus(boardId, status);

    }

    @Override
    public void sortBoards(List<Long> boardIds){

        if (boardIds == null || boardIds.isEmpty()) {
            return;
        }

        if (boardIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("boardIds 中不能包含空值");
        }

        forumBoardMapper.sortBoards(boardIds);

    }

    @Override
    public void deleteBoard(Integer boardId){

        forumBoardMapper.deleteBoard(boardId);

    }

    @Override
    public List<ForumPost> getBoardPosts(ForumBoard forumBoard){

        if (forumBoard == null || forumBoard.getBoardId() == null) {
            throw new IllegalArgumentException("boardId 不能为空");
        }

        return forumBoardMapper.getBoardPosts(forumBoard.getBoardId());

    }


}
