package io.legends.plol.application.domain.blog.author;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

/**
 * A basic Author entity implemented only the Spring Data persistable interface.
 * The latter is required to be able to work with Spring Data repositories.
 * <p/>
 * The id uses a table sequence generator from the AcrossHibernateJpaModule.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "ax_author")
@Builder(toBuilder = true)
public class Author implements Persistable<Long>
{
	@Id
	@GeneratedValue(generator = "seq_ax_author_id")
	@GenericGenerator(
			name = "seq_ax_author_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_ax_author_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@Column
	@NotBlank
	@Length(max = 255)
	private String name;

	@Column
	@Length(max = 255)
	@Email
	private String email;

	@Override
	public boolean isNew() {
		return id == null;
	}
}
