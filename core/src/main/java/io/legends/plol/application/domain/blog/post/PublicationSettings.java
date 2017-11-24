package io.legends.plol.application.domain.blog.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * A simple Embeddable class to combine publication settings.
 * Only added as an example of embeddable support (for example in EntityModule).
 */
@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicationSettings
{
	@Column
	private boolean published;

	@Temporal(TemporalType.DATE)
	@Column(name = "publication_date")
	private Date publicationDate;
}
