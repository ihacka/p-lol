package io.ilot.plol.repos;

import io.ilot.plol.model.board.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
