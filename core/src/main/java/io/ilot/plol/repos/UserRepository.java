package io.ilot.plol.repos;

import io.ilot.plol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;


public interface UserRepository extends JpaRepository<User, Long> {

    //@Query("from users where name = :name")
    Collection<User> findByName(String name);

}
