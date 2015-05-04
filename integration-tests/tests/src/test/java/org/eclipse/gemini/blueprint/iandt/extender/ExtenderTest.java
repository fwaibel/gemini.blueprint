/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.extender;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaultsWithoutExtender;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Hal Hildebrand Date: May 21, 2007 Time: 4:43:52 PM
 */
@ContextConfiguration
public class ExtenderTest extends BaseIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaultsWithoutExtender(),
				mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-extender").versionAsInProject().noStart(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "lifecycle").versionAsInProject()
        		);
    }

	// Specifically cannot wait - test scenario has bundles which are spring
	// powered, but will not be started.
	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return false;
	}

	@Test
	public void testLifecycle() throws Exception {
		assertNull("Guinea pig has already been started", System
				.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.close"));

		StringBuilder filter = new StringBuilder();
		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(ApplicationContext.class.getName()).append(
				")");
		filter.append("(").append("org.springframework.context.service.name");
		filter.append("=").append("org.eclipse.gemini.blueprint.iandt.lifecycle").append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);
		tracker.open();

		ApplicationContext appContext = (ApplicationContext) tracker.waitForService(1);

		assertNull("lifecycle application context does not exist", appContext);

		Bundle extenderBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.extender");
		assertNotNull("Extender bundle", extenderBundle);

		extenderBundle.start();

		tracker.open();

		appContext = (ApplicationContext) tracker.waitForService(60000);

		assertNotNull("lifecycle application context exists", appContext);

		assertNotSame("Guinea pig hasn't already been shutdown", "true", System
				.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.close"));

		assertEquals("Guinea pig started up", "true", System
				.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.startUp"));

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
