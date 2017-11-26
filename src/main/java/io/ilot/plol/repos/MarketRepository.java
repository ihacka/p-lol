package io.ilot.plol.repos;

import io.ilot.plol.model.Incident;
import io.ilot.plol.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by marini on 26/11/2017.
 */
public interface MarketRepository extends JpaRepository<Market, Long> {
}
