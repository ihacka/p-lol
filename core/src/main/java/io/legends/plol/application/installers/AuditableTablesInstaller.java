package io.legends.plol.application.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.modules.hibernate.installers.AuditableSchemaInstaller;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.Collections;

/**
 * The BlogPost is implemented as a {@link com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity},
 * with fields like createdDate, createdBy, lastModifiedDate, lastModifiedBy that are automatically set by another module.
 * This installer adds the required table columns for those fields.
 */
@Order(2)
@Installer(name = "auditable-tables", description = "Adds auditing columns to core tables")
public class AuditableTablesInstaller extends AuditableSchemaInstaller
{
	@Override
	protected Collection<String> getTableNames() {
		return Collections.singleton( "ax_blog_post" );
	}
}