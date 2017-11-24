package io.legends.plol.application.domain.blog.post;

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.legends.plol.application.domain.blog.author.Author;

import java.util.List;

/**
 * A sample Spring Data JPA repository using one of the custom repository interfaces from AcrossHibernateJpaModule.
 * AcrossHibernateJpaModule provides base classes and interfaces for common configurations that ensure support with other modules.
 * <p/>
 * There is however no real requirement to use any of them.
 */
public interface BlogPostRepository extends IdBasedEntityJpaRepository<BlogPost>
{
	List<BlogPost> findAllByAuthor( Author author );

	Page<BlogPost> findByPublicationSettings_PublishedIsTrueOrderByPublicationSettings_PublicationDateDesc( Pageable pageable );
}
