package io.ilot.plol.controller;

import io.ilot.plol.repos.BoardRepository;
import io.ilot.plol.model.board.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardController {

    Logger logger = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    private BoardRepository boardRepository;

    @RequestMapping("/boards/{boardId}")
    public ResponseEntity<Board> board(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardRepository.findOne(boardId));
    }


}
