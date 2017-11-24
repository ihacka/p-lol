package io.legends.plol.application.web;

import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.EventName;
import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.template.ClearTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Simple controller that maps static pages with or without the layout template applies.
 * This controller also registers some top menu items.
 */
@Controller
public class ExamplePagesController
{
	@Event
	void registerTopNavMenuItems( @EventName("topNav") BuildMenuEvent<Menu> topNav ) {
		topNav.builder()
		      .item( "/home", "Home", "/" ).order( 1 ).and()
		      .item( "/about", "About", "/about" ).order( 2 ).and()
			  .item( "/contact", "Contact", "#");
	}
	@GetMapping("/about")
	public String about() {
		return "th/plol/about";
	}

	@ClearTemplate
	@GetMapping("/no-layout")
	public String pageWithoutLayout() {
		return "th/plol/no-layout";
	}
}