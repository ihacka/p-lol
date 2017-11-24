package io.legends.plol.application.domain.blog.post;

import io.legends.plol.application.domain.blog.author.Author;
import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * A custom BlogPost entity implemented using one of the base classes from AcrossHibernateJpaModule.
 * The {@link SettableIdAuditableEntity} adds auditing properties (createdDate, createdBy, lastModifiedDate, lastModifiedBy),
 * implements the id as a Long and implements equality based on the id value.  It also supplies a {@link #toDto()} method
 * that is expected to provide a safe DTO (detached) object for the entity.
 * <p/>
 * In combination with the table sequence generator used for the id, entities of this base class can be created either
 * with an automatic id, or with a manually determined id (set using {@link #setNewEntityId(Long)}.  Care should be taken not
 * to use manual ids conflicting with the table generated ones.  But for example test data could be inserted with manually set
 * negative ids, as the table generator only generates positive numbers.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "ax_blog_post")
@Builder(toBuilder = true)
public class BlogPost extends SettableIdAuditableEntity<BlogPost>
{
	@Id
	@GeneratedValue(generator = "seq_ax_blog_post_id")
	@GenericGenerator(
			name = "seq_ax_blog_post_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_ax_blog_post_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "author_id")
	private Author author;

	@Column
	@NotBlank
	@Length(max = 255)
	private String title;

	@Column(name = "sub_title")
	@Length(max = 255)
	private String subTitle;

	@Column(name = "image_url")
	@Length(max = 255)
	private String imageUrl;

	@Column
	private String body;

	@Embedded
	@Builder.Default
	private PublicationSettings publicationSettings = new PublicationSettings();
}
