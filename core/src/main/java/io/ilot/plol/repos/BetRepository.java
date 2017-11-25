package io.ilot.plol.repos;

import io.ilot.plol.model.Bet;
import io.ilot.plol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

        List<Bet> findByUser(User userId);
}
