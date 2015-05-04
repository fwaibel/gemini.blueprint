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

package org.eclipse.gemini.blueprint.iandt.dependencyWithDelay;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.iandt.dependencies.Dependent;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.Test;

/**
 * @author Hal Hildebrand Date: Aug 27, 2007 Time: 9:36:23 AM
 */
@ContextConfiguration
public class FactoryDependencyTest extends BaseIntegrationTest {

	private static final String DELAY_PROP = "org.eclipse.gemini.blueprint.iandt.dependencies.factory.delay";

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "dependencies").versionAsInProject().noStart(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service2").versionAsInProject().noStart(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service3").versionAsInProject().noStart(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "dependendencies.factory").versionAsInProject().noStart()
        		);
    }

	// dependency bundle - depends on service2, service3 and, through a nested
	// reference, to service1
	// simple.service2 - publishes service2
	// simple.service3 - publishes service3
	// simple - publishes service1
    @Test
	public void testDependencies() throws Exception {
		System.setProperty(DELAY_PROP, "10000");

		Bundle dependencyTestBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.dependencies");
		Bundle simpleService2Bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.simpleservice2");
		Bundle simpleService3Bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.simpleservice3");
		Bundle factoryServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.dependencies.factory");

		assertNotNull("Cannot find the factory service bundle", factoryServiceBundle);
		assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);
		assertNotNull("Cannot find the simple service 3 bundle", simpleService3Bundle);
		assertNotNull("dependencyTest can't be resolved", dependencyTestBundle);

		assertNotSame("factory service bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			factoryServiceBundle.getState()));

		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService2Bundle.getState()));

		assertNotSame("simple service 3 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService3Bundle.getState()));

		startDependencyAsynch(dependencyTestBundle);
		Thread.sleep(2000); // Yield to give bundle time to get into waiting
		// state.
		ServiceReference<Dependent> dependentRef = bundleContext.getServiceReference(Dependent.class);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		startDependency(simpleService3Bundle);

		dependentRef = bundleContext.getServiceReference(Dependent.class);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		startDependency(simpleService2Bundle);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		dependentRef = bundleContext.getServiceReference(Dependent.class);

		startDependency(factoryServiceBundle);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.dependencies");

		dependentRef = bundleContext.getServiceReference(Dependent.class);

		assertNotNull("Service has not been started!", dependentRef);

		Object dependent = bundleContext.getService(dependentRef);

		assertNotNull("Service is not available!", dependent);

	}

	private void startDependency(Bundle bundle) throws BundleException, InterruptedException {
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}

	private void startDependencyAsynch(final Bundle bundle) {
		System.out.println("starting dependency test bundle");
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					bundle.start();
					System.out.println("started dependency test bundle");
				}
				catch (BundleException ex) {
					System.err.println("can't start bundle " + ex);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.setDaemon(false);
		thread.setName("dependency test bundle");
		thread.start();
	}

	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return true;
	}

	protected long getDefaultWaitTime() {
		return 60L;
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		list.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.RESOLVE));
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}
}