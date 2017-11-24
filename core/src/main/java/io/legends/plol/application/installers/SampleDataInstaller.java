package io.legends.plol.application.installers;

import io.legends.plol.application.domain.blog.author.Author;
import io.legends.plol.application.domain.blog.author.AuthorRepository;
import io.legends.plol.application.domain.blog.post.BlogPost;
import io.legends.plol.application.domain.blog.post.BlogPostRepository;
import io.legends.plol.application.domain.blog.post.PublicationSettings;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Installs sample blog posts using the actual repositories.  Like the other installers this installer will only execute
 * if the version is different.  Unlike the other installers, it only runs after all modules have bootstrapped.  This means
 * all components like services, repositories can be used to perform its tasks.
 * <p/>
 * NOTE: this installer can only be executed once, there is no code supporting updates of any kind, running it more than
 * once will simply insert all data a second time.
 */
@Installer(name = "sample-data", description = "Adds sample blog posts", phase = InstallerPhase.AfterContextBootstrap)
@RequiredArgsConstructor
class SampleDataInstaller
{
	private final AuthorRepository authorRepository;
	private final BlogPostRepository blogPostRepository;

	@Transactional
	@InstallerMethod
	public void createAuthorsAndBlogPosts() {
		createAcrossBlogPost();
		createDummyBlogPosts();
	}

	private void createAcrossBlogPost() {
		Author author = authorRepository.save( Author.builder().name( "The Across Team" ).build() );
		blogPostRepository.save(
				BlogPost.builder().author( author )
				        .title( "Congratulations with your Across application" )
				        .subTitle( "This is a very simple but functional blog system" )
				        .body( "<p>The Across Initializr generated this project for you based on the selected modules. " +
						               "A sample domain model (simple entities) for Blog post and Author has been added. " +
						               "If you have added AdminWebModule and EntityModule to your application, you can manage these entities. " +
						               "If you selected to add the sample frontend controllers you can browse your blogs on the frontend.</p>" +
						               "<p>The included example code can be a good starting point for building your own application. " +
						               "We hope you enjoy working with Across!</p>" )
				        .publicationSettings( PublicationSettings.builder().publicationDate( date( 2017, 11, 4 ) ).published( true ).build() )
				        .build()
		);
	}

	private void createDummyBlogPosts() {
		Author author = authorRepository.save( Author.builder().name( "Start Bootstrap" ).build() );

		Stream.of(
				BlogPost.builder()
				        .author( author ).imageUrl( IMAGE_URL ).body( POST_BODY )
				        .title( "Man must explore, and this is exploration at its greatest" )
				        .subTitle( "Problems look mighty small from 150 miles up" )
				        .publicationSettings( PublicationSettings.builder().publicationDate( date( 2017, 11, 1 ) ).published( true ).build() )
				        .build(),
				BlogPost.builder()
				        .author( author ).imageUrl( IMAGE_URL ).body( POST_BODY )
				        .title( "I believe every human has a finite number of heartbeats. I don't intend to waste any of mine." )
				        .publicationSettings( PublicationSettings.builder().publicationDate( date( 2017, 8, 13 ) ).published( true ).build() )
				        .build(),
				BlogPost.builder()
				        .author( author ).imageUrl( IMAGE_URL ).body( POST_BODY )
				        .title( "Science has not yet mastered prophecy" )
				        .subTitle( "We predict too much for the next year and yet far too little for the next ten" )
				        .publicationSettings( PublicationSettings.builder().publicationDate( date( 2016, 12, 31 ) ).published( true ).build() )
				        .build(),
				BlogPost.builder()
				        .author( author ).imageUrl( IMAGE_URL ).body( POST_BODY )
				        .title( "Failure is not an option" )
				        .subTitle( "Many say exploration is part of our destiny, but it's actually our duty to future generations." )
				        .build()
		)
		      .forEach( blogPostRepository::save );
	}

	private Date date( int year, int month, int dayOfMonth ) {
		return Date.from( LocalDate.of( year, month, dayOfMonth ).atStartOfDay( ZoneId.systemDefault() ).toInstant() );
	}

	private static final String IMAGE_URL = "https://blackrockdigital.github.io/startbootstrap-clean-blog/img/post-bg.jpg";
	private static final String POST_BODY =
			"<p>Never in all their history have men been able truly to conceive of the world as one: a single sphere, a globe, having the qualities of a globe, a round earth in which all the directions eventually meet, in which there is no center because every point, or none, is center — an equal earth which all men occupy as equals. The airman's earth, if free men make it, will be truly round: a globe in practice, not in theory.</p>\n" +
					"<p>Science cuts two ways, of course; its products can be used for both good and evil. But there's no turning back from science. The early warnings about technological dangers also come from science.</p>\n" +
					"<p>What was most significant about the lunar voyage was not that man set foot on the Moon but that they set eye on the earth.</p>\n" +
					"<p>A Chinese tale tells of some men sent to harm a young girl who, upon seeing her beauty, become her protectors rather than her violators. That's how I felt seeing the Earth for the first time. I could not help but love and cherish her.</p>\n" +
					"<p>For those who have seen the Earth from space, and for the hundreds and perhaps thousands more who will, the experience most certainly changes your perspective. The things that we share in our world are far more valuable than those which divide us.</p>\n" +
					"<h2 class=\"section-heading\">The Final Frontier</h2>\n" +
					"<p>There can be no thought of finishing for ‘aiming for the stars.’ Both figuratively and literally, it is a task to occupy the generations. And no matter how much progress one makes, there is always the thrill of just beginning.</p>\n" +
					"<p>There can be no thought of finishing for ‘aiming for the stars.’ Both figuratively and literally, it is a task to occupy the generations. And no matter how much progress one makes, there is always the thrill of just beginning.</p>\n" +
					"<blockquote class=\"blockquote\">The dreams of yesterday are the hopes of today and the reality of tomorrow. Science has not yet mastered prophecy. We predict too much for the next year and yet far too little for the next ten.</blockquote>\n" +
					"<p>Spaceflights cannot be stopped. This is not the work of any one man or even a group of men. It is a historical process which mankind is carrying out in accordance with the natural laws of human development.</p>\n" +
					"<h2 class=\"section-heading\">Reaching for the Stars</h2>\n" +
					"<p>As we got further and further away, it [the Earth] diminished in size. Finally it shrank to the size of a marble, the most beautiful you can imagine. That beautiful, warm, living object looked so fragile, so delicate, that if you touched it with a finger it would crumble and fall apart. Seeing this has to change a man.</p>\n" +
					"<a href=\"#\">\n" +
					"<img class=\"img-fluid\" src=\"https://blackrockdigital.github.io/startbootstrap-clean-blog/img/post-sample-image.jpg\" alt=\"\">\n" +
					"</a>\n" +
					"<span class=\"caption text-muted\">To go places and do things that have never been done before – that’s what living is all about.</span>\n" +
					"<p>Space, the final frontier. These are the voyages of the Starship Enterprise. Its five-year mission: to explore strange new worlds, to seek out new life and new civilizations, to boldly go where no man has gone before.</p>\n" +
					"<p>As I stand out here in the wonders of the unknown at Hadley, I sort of realize there’s a fundamental truth to our nature, Man must explore, and this is exploration at its greatest.</p>\n" +
					"<p>Placeholder text by\n" +
					"<a href=\"http://spaceipsum.com/\">Space Ipsum</a>. Photographs by\n" +
					"<a href=\"https://www.flickr.com/photos/nasacommons/\">NASA on The Commons</a>.</p>";
}
