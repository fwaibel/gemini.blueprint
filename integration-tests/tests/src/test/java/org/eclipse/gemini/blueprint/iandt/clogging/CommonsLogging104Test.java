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

package org.eclipse.gemini.blueprint.iandt.clogging;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration test for commons logging 1.0.4 and its broken logging discovery.
 * 
 * @author Costin Leau
 * 
 */
@ContextConfiguration(locations = {"classpath:org/eclipse/gemini/blueprint/iandt/bundleScope/scope-context.xml"})
public class CommonsLogging104Test extends BaseIntegrationTest {

	/** logger */
	private static final Log log = LogFactory.getLog(CommonsLogging104Test.class);

    @Configuration
    public Option[] config() {
        return options(blueprintDefaults(),
        		// TODO where did this bundle come from?
//        		bnds.add("org.eclipse.bundles,commons-logging,20070611");
//        		org.eclipse.virgo.mirrored/org.apache.commons.logging/1.0.4.v201101211617
                mavenBundle("commons-logging", "commons-logging").version("1.0.4"));
    }

    @Test
	public void testSimpleLoggingStatement() throws Exception {
		log.info("logging statement");
	}

    // TODO - not sure what to do here
	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		// all below fail
		LogFactory.releaseAll();
		//System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.NoOpLog");
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		//		System.out.println("TCCL is " + cl);
		Thread.currentThread().setContextClassLoader(null);
//		super.preProcessBundleContext(platformBundleContext);
	}

}
