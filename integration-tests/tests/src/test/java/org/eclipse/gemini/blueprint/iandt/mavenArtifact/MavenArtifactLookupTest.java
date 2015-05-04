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

package org.eclipse.gemini.blueprint.iandt.mavenArtifact;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.io.FilePermission;
import java.util.List;
import java.util.PropertyPermission;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.MyListener;
import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.ReferenceContainer;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;

/**
 * @author Hal Hildebrand
 *         Date: Mar 5, 2007
 *         Time: 6:00:39 PM
 */

/**
 * This test ensures that Maven artifact lookup is maintained. Note that all
 * Maven artifact lookups are explicity using the type of the artifact - a
 * property of the artifact resolution that we need to preserve.
 */
@ContextConfiguration
public class MavenArtifactLookupTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		// return
		// "classpath:org.eclipse.gemini.blueprint.test/mavenArtifact/MavenArtifactLookupTest.MF";
		return null;
	}

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service").versionAsInProject(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "cardinality0to1").versionAsInProject(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service2").versionAsInProject().noStart()
        		);
    }

    @Test
	public void test0to1Cardinality() throws Exception {

		Bundle simpleService2Bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.simpleservice2");

		assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);

		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService2Bundle.getState()));

		assertEquals("Unxpected initial binding of service", 0, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		try {
			ReferenceContainer.service.stringValue();
			fail("Service should be unavailable");
		}
		catch (ServiceUnavailableException e) {
			// expected
		}

		startDependency(simpleService2Bundle);

		assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		assertNotNull(ReferenceContainer.service.stringValue());

	}

	private void startDependency(Bundle simpleService2Bundle) throws BundleException, InterruptedException {
		System.out.println("Starting dependency");
		simpleService2Bundle.start();

		waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.simpleservice2");

		System.out.println("Dependency started");
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return perms;
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}
}
