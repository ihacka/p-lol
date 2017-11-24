package io.legends.plol.application.web.ui;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Layout template registered as the default template of the main {@link WebTemplateRegistry}.
 * Builds a top navigation menu and registers some web resources.
 * One of the registered resources is a bean that exposes the configured static resources path
 * by exposing it in JSON.
 * <p>
 * See the {@link #registerAsDefaultTemplate(WebTemplateRegistry)} method.
 */
@Component
public class DefaultLayoutTemplate extends LayoutTemplateProcessorAdapterBean
{
	private final WebAppPathResolver pathResolver;

	public DefaultLayoutTemplate( WebAppPathResolver pathResolver ) {
		super( "default", "th/plol/layout/default-layout" );

		this.pathResolver = pathResolver;
	}

	@Override
	protected void buildMenus( MenuFactory menuFactory ) {
		// Publish a top navigation menu
		menuFactory.buildMenu( "topNav" );
	}

	@Override
	protected void registerWebResources( WebResourceRegistry registry ) {
		// Manually add JQuery JS and Bootstrap CSS/JS
		registry.addWithKey(
				WebResource.CSS,
				"bootstrap",
				"//maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css",
				WebResource.EXTERNAL
		);
		registry.addWithKey(
				WebResource.CSS,
				"font-awesome",
				"//maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css",
				WebResource.EXTERNAL
		);
		registry.addWithKey(
				WebResource.JAVASCRIPT_PAGE_END,
				"jquery",
				"//ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js",
				WebResource.EXTERNAL
		);
		registry.addWithKey(
				WebResource.JAVASCRIPT_PAGE_END,
				"bootstrap",
				"//maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js",
				WebResource.EXTERNAL
		);

		// Static resources of the application
		registry.addWithKey(
				WebResource.CSS,
				"plol-css",
				"/static/plol/css/main.css",
				WebResource.VIEWS
		);
		registry.addWithKey(
				WebResource.JAVASCRIPT_PAGE_END,
				"plol-js",
				"/static/plol/js/main.js",
				WebResource.VIEWS
		);

		// Register the configured static resources path so it can be used from javascript.
		// This illustrates passing custom objects for JSON serialization to the frontend.
		Map<String, String> acrossWebPathVariables = new HashMap<>();
		acrossWebPathVariables.put( "resourcePath", StringUtils.removeEnd( pathResolver.path( "@resource:/" ), "/" ) );
		acrossWebPathVariables.put( "staticPath", StringUtils.removeEnd( pathResolver.path( "@static:/" ), "/" ) );
		registry.addWithKey( WebResource.JAVASCRIPT, AcrossWebModule.NAME, acrossWebPathVariables, WebResource.DATA );
	}

	/**
	 * Actually register the template as default in the template registry.
	 */
	@Autowired
	public void registerAsDefaultTemplate( WebTemplateRegistry webTemplateRegistry ) {
		webTemplateRegistry.setDefaultTemplateName( "default" );
	}
}
