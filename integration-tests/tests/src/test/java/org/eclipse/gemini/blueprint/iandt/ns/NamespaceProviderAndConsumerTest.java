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

package org.eclipse.gemini.blueprint.iandt.ns;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.awt.Shape;
import java.io.File;
import java.net.URL;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test that provides a namespace that is also used internally.
 * 
 * @author Costin Leau
 */
@ContextConfiguration("classpath:org/eclipse/gemini/blueprint/iandt/ns/context.xml")
public class NamespaceProviderAndConsumerTest extends BaseIntegrationTest {

	private Shape nsBean;

	private static final String BND_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.ns.own.provider";

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "ns.own.consumer").versionAsInProject()
        		);
    }

    @Test
	public void testApplicationContextWasProperlyStarted() throws Exception {
		assertNotNull(applicationContext);
		assertNotNull(applicationContext.getBean("nsDate"));
		assertNotNull(applicationContext.getBean("nsBean"));
	}

    @Test
    @Ignore
    // TOOD - check test failure
	public void testTestAutowiring() throws Exception {
		assertNotNull(nsBean);
	}

	public void testNamespaceFilesOnTheClassPath() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, BND_SYM_NAME);
		assertNotNull("cannot find handler bundle", bundle);
		URL handlers = bundle.getResource("META-INF/spring.handlers");
		URL schemas = bundle.getResource("META-INF/spring.schemas");

		assertNotNull("cannot find a handler inside the custom bundle", handlers);
		assertNotNull("cannot find a schema inside the custom bundle", schemas);
	}

	public void testNSBundlePublishedOkay() throws Exception {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ApplicationContext.class.getName(), "(" + Constants.BUNDLE_SYMBOLICNAME + "=" + BND_SYM_NAME + ")");
		assertNotNull(ref);
		ApplicationContext ctx = (ApplicationContext) bundleContext.getService(ref);
		assertNotNull(ctx);
		assertNotNull(ctx.getBean("nsBean"));
		assertNotNull(ctx.getBean("nsDate"));

	}

	/**
	 * @param nsBean The nsBean to set.
	 */
	public void setNsBean(Shape nsBean) {
		this.nsBean = nsBean;
	}
}