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

package org.eclipse.gemini.blueprint.iandt.servicedependency;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.iandt.simpleservice.MyService;
import org.eclipse.gemini.blueprint.iandt.tccl.TCCLService;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * @author Costin Leau
 * 
 */
@ContextConfiguration("classpath:org/eclipse/gemini/blueprint/iandt/servicedependency/multi-export-multi-collection-import.xml")
public class CollectionImporterTest extends BaseIntegrationTest {

	private static final String TCCL_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.tccl";

	private static final String SERVICE_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.simpleservice";

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "tccl.intf").versionAsInProject(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "tccl").versionAsInProject(),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service").versionAsInProject()
        		);
    }

    @Test
	public void testExporterAWhenImporterAGoesDownAndUp() throws Exception {
		assertTrue("exporterA should be running", isExporterAStarted());
		logger.info("Taking down serviceA...");
		takeDownServiceA();
		assertFalse("serviceA should take exporterA down", isExporterAStarted());
		logger.info("Putting up serviceA...");
		putUpServiceA();
		// check exporter
		assertTrue("serviceA is up again, so should exporterA", isExporterAStarted());
	}

    @Test
    @Ignore
    // TODO - throws OOM exception in Eclipse
	public void testExporterBWhenImporterAGoesDownAndUp() throws Exception {
		assertTrue("exporterB should be running", isExporterBStarted());
		logger.info("Taking down serviceA...");
		takeDownServiceA();
		assertFalse("serviceA should take exporterB down", isExporterBStarted());
		logger.info("Putting up serviceA...");
		putUpServiceA();
		// check exporter
		assertTrue("service A is up again, so should exporterB", isExporterBStarted());
	}

    @Test
	public void testExporterBWhenImporterAGoesDownThenImporterBThenBothUpAgain() throws Exception {
		assertTrue("exporterB should be running", isExporterBStarted());

		takeDownServiceA();
		assertFalse("serviceA should take exporterB down", isExporterBStarted());

		// take down B
		takeDownServiceC();
		// check exporter
		assertFalse("serviceC down should keep exporterB down", isExporterBStarted());

		putUpServiceA();
		// check exporter
		assertFalse("service C is still down and so should be exporterB", isExporterBStarted());
		putUpServiceC();
		// check exporter
		assertTrue("service A,B up -> exporterB up", isExporterBStarted());
	}

	private void checkAndTakeDownService(String beanName, Class<?> type, String bundleSymName) throws Exception {
		ServiceReference ref = bundleContext.getServiceReference(type.getName());
		Object service = bundleContext.getService(ref);
		Assert.isInstanceOf(type, service);

		Bundle dependency = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, bundleSymName);
		// stop dependency bundle -> no importer -> exporter goes down
		dependency.stop();
	}

	private void putUpService(String bundleSymName) throws Exception {
		Bundle dependency = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, bundleSymName);
		dependency.start();
		waitOnContextCreation(bundleSymName);
	}

	private void takeDownServiceA() throws Exception {
		checkAndTakeDownService("serviceA", MyService.class, SERVICE_SYM_NAME);
	}

	private void putUpServiceA() throws Exception {
		putUpService(SERVICE_SYM_NAME);
	}

	private void takeDownServiceC() throws Exception {
		checkAndTakeDownService("serviceC", TCCLService.class, TCCL_SYM_NAME);
	}

	private void putUpServiceC() throws Exception {
		putUpService(TCCL_SYM_NAME);
	}

	private boolean isExporterBStarted() throws Exception {
		return (bundleContext.getServiceReference(SimpleBean.class.getName()) != null);
	}

	private boolean isExporterAStarted() throws Exception {
		return (bundleContext.getServiceReference(Map.class.getName()) != null);
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
