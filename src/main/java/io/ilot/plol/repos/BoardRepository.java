package io.ilot.plol.repos;

import io.ilot.plol.model.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
