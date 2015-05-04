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

package org.eclipse.gemini.blueprint.iandt.proxied.listener;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.awt.Shape;
import java.awt.geom.Area;
import java.io.File;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.ServiceRegistration;
import org.springframework.test.context.ContextConfiguration;
import org.eclipse.gemini.blueprint.iandt.proxy.listener.Listener;
import org.junit.Test;

/**
 * @author Costin Leau
 */
@ContextConfiguration("classpath:org/eclipse/gemini/blueprint/iandt/proxied/listener/service-import.xml")
public class ProxiedListenerTest extends BaseIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "proxy.listener").versionAsInProject()
        		);
    }

    @Test
	public void testListenerProxy() throws Exception {
		System.out.println(Listener.class.getName());
		Object obj = new Area();
		ServiceRegistration reg = bundleContext.registerService(Shape.class.getName(), obj, null);
		reg.unregister();
	}
}
