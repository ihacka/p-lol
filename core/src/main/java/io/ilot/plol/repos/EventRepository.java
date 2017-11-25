package io.ilot.plol.repos;

import io.ilot.plol.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by marini on 25/11/2017.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

}
