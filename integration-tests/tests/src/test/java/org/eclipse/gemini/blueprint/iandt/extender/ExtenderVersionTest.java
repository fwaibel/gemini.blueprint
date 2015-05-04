/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.extender;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.awt.Point;
import java.io.File;
import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Costin Leau
 * 
 */
@ContextConfiguration
public class ExtenderVersionTest extends BaseIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "extender-version-bundle").versionAsInProject()
        		);
    }

	// given bundle should not be picked up by the extender since it expects a
	// certain version
	@Test
	@Ignore
	// TODO - how to test service not published?
	public void testBundleIgnoredBasedOnSpringExtenderVersion() throws Exception {
		assertNull("no point should be published ", bundleContext.getServiceReference(Point.class.getName()));
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		return perms;
	}
}
