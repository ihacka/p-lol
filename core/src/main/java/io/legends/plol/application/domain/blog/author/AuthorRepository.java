package io.legends.plol.application.domain.blog.author;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * A sample Spring Data JPA repository using only the basic Spring Data interfaces.
 */
public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author>
{
}
