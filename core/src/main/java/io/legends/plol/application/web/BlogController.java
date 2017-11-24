package io.legends.plol.application.web;

import io.legends.plol.application.domain.blog.post.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Simple controller for loading blog post homepage and blog post detail.
 */
@Controller
@RequiredArgsConstructor
public class BlogController
{
	private final BlogPostRepository blogPostRepository;

	@GetMapping("/")
	public String homepage( @PageableDefault(size = 3) Pageable pageable, Model model ) {
		model.addAttribute(
				"blogsPage",
				blogPostRepository.findByPublicationSettings_PublishedIsTrueOrderByPublicationSettings_PublicationDateDesc( pageable )
		);

		return "th/plol/homepage";
	}

	@GetMapping("/post/{blogId}")
	public String blogPost( @PathVariable long blogId, Model model ) {
		model.addAttribute( "post", blogPostRepository.findOne( blogId ) );
		return "th/plol/blog-post";
	}
}
