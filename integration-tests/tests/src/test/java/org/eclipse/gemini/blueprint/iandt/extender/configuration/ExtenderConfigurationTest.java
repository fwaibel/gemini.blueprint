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

package org.eclipse.gemini.blueprint.iandt.extender.configuration;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;

/**
 * Extender configuration fragment.
 * 
 * @author Costin Leau
 * 
 */
@ContextConfiguration("classpath:org/eclipse/gemini/blueprint/iandt/extender/configuration/config.xml")
public class ExtenderConfigurationTest extends BaseIntegrationTest {

	private ApplicationContext context;

	@Before
	public void onSetUp() throws Exception {
		context = (ApplicationContext) applicationContext.getBean("appCtx");
	}

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "extender-fragment-bundle").versionAsInProject().noStart()
        		);
    }

    @Test
	public void testExtenderConfigAppCtxPublished() throws Exception {
		ServiceReference[] refs =
				bundleContext.getAllServiceReferences("org.springframework.context.ApplicationContext", null);
		for (int i = 0; i < refs.length; i++) {
			System.out.println(OsgiStringUtils.nullSafeToString(refs[i]));
		}
		assertNotNull(context);
	}

    @Test
	public void testPackageAdminReferenceBean() throws Exception {
		if (PackageAdmin.class.hashCode() != 0)
			;
//		logger.info("Calling package admin bean");
		assertNotNull(context.getBean("packageAdmin"));
	}

    @Test
    public void testShutdownTaskExecutor() throws Exception {
        assertTrue(context.containsBean("shutdownTaskExecutor"));
        Object bean = context.getBean("shutdownTaskExecutor");
        assertTrue("unexpected type", bean instanceof TaskExecutor);
    }

    @Test
	public void testTaskExecutor() throws Exception {
		assertTrue(context.containsBean("taskExecutor"));
		Object bean = context.getBean("taskExecutor");
		assertTrue("unexpected type", bean instanceof TaskExecutor);
	}

    @Test
	public void testCustomProperties() throws Exception {
		assertTrue(context.containsBean("extenderProperties"));
		Object bean = context.getBean("extenderProperties");
		assertTrue("unexpected type", bean instanceof Properties);
	}

	// TODO - felix doesn't support fragments, so disable this test
//	protected boolean isDisabledInThisEnvironment(String testMethodName) {
//		return getPlatformName().indexOf("elix") > -1;
//	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}
}