package io.legends.plol.application.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.config.builders.EntityListViewFactoryBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import io.legends.plol.application.domain.blog.author.Author;
import io.legends.plol.application.domain.blog.post.BlogPost;
import org.springframework.data.domain.Sort;
import io.legends.plol.application.domain.blog.BlogDomain;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Contains the main domain configuration.
 * At the very least configures where to scan for Spring Data repositories.
 */
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = BlogDomain.class)
class DomainConfiguration
{
	/**
	 * Nested sample EntityModule configuration.
	 * Customizes the list view of blog post for both the main blog post view, as well as the associated view on author.
	 */
	@AcrossDepends(required = EntityModule.NAME)
	@Configuration
	static class BlogPostAdminUiConfiguration implements EntityConfigurer
	{
		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			Consumer<EntityListViewFactoryBuilder> blogPostListView =
					lvb -> lvb.showProperties( "author", "title", "publicationSettings.publicationDate" )
					          .defaultSort( new Sort( Sort.Direction.DESC, "publicationSettings.publicationDate" ) )
					          .viewProcessor( new BlogPostListViewProcessor() );

			entities.withType( BlogPost.class )
			        .listView( blogPostListView );

			// Author has an association to the blog post, mapped by the @ManyToOne on BlogPost
			// The associated view is first customized with the default list view customization, and afterwards
			// we customize it again and remove the author property from the list view (as we're already in the scope of an author)
			entities.withType( Author.class )
			        .association(
					        ab -> ab.name( "blogPost.author" )
					                .listView( blogPostListView )
					                .listView( lvb -> lvb.showProperties( ".", "~author" ) )
			        );
		}

		/**
		 * A custom view processor that is added to a BlogPost list view.  It customizes the default table by highlighting
		 * rows for blog posts that are not published.  A Bootstrap class is added to the rows of those blog posts.
		 */
		class BlogPostListViewProcessor extends EntityViewProcessorAdapter
		{
			@Override
			protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
				builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class )
				          .valueRowProcessor( ( builderContext, row ) -> {
					          BlogPost post = EntityViewElementUtils.currentEntity( builderContext, BlogPost.class );

					          if ( !post.getPublicationSettings().isPublished() ) {
						          row.setStyle( Style.DANGER );
					          }
				          } );
			}
		}
	}
}
