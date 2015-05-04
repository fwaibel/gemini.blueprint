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
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

/**
 * Integration test for Sync Wait but this time, by checking the waiting by
 * satisfying the dependency through this test.
 * 
 * @author Costin Leau
 * 
 */
@ContextConfiguration
public class SyncWaitWithoutDependencyTest extends BehaviorBaseTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "sync-wait-bundle").versionAsInProject().noStart(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "sync-tail-bundle").versionAsInProject().noStart()
        		);
    }

    @Test
	public void testBehaviour() throws Exception {

		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.springframework.osgi.iandt.config-opt.sync-wait");
		Bundle tail = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.springframework.osgi.iandt.config-opt.sync-tail");

		bundle.start();

		assertTrue("bundle " + bundle + "should have started", OsgiBundleUtils.isBundleActive(bundle));

		// start bundle dependency
		tail.start();

		assertTrue("bundle " + tail + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tail));

		// check appCtx hasn't been published
		assertContextServiceIs(bundle, false, 500);
		// check the dependency ctx
		assertContextServiceIs(tail, true, 500);

		// restart the bundle (to catch the tail)
		bundle.stop();
		bundle.start();

		// check appCtx has been published
		assertContextServiceIs(bundle, true, 500);
	}
}
