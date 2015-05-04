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

package org.eclipse.gemini.blueprint.iandt.configopt;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.awt.Point;
import java.io.File;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

/**
 * Integration test for publish-context directive.
 * 
 * @author Costin Leau
 * 
 */
@ContextConfiguration
public class NoPublishTest extends BehaviorBaseTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "nopublish-bundle").versionAsInProject()
        		);
    }

    @Test
	public void testBehaviour() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.springframework.osgi.iandt.config-opt.nopublish");
		// wait for the listener to catch up
		Thread.sleep(1000);
		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// check that the appCtx is not publish
		assertContextServiceIs(bundle, false, 1000);

		// but the point service is
		assertNotNull("point service should have been published"
				+ bundleContext.getServiceReference(Point.class));
	}
}
