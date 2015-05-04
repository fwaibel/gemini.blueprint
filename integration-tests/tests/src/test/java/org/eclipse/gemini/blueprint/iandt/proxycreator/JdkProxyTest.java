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

package org.eclipse.gemini.blueprint.iandt.proxycreator;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.ServiceReference;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration test checking the JDK proxy creation on invocation handlers that
 * cannot be seen by the proxy creator.
 * 
 * @author Costin Leau
 * 
 */
@ContextConfiguration
public class JdkProxyTest extends BaseIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "jdk.proxy").versionAsInProject()
        		);
    }

	@Test
	public void testJDKProxyCreationUsingTheInterfaceClassLoaderInsteadOfTheHandlerOne() throws Exception {
		// get the invocation handler directly
		InvocationHandler handler = getInvocationHandler();
		SomeInterfaceImplementation target = new SomeInterfaceImplementation();
		SomeInterface proxy = createJDKProxy(handler, target);

		SomeInterfaceImplementation.INVOCATION = 0;
		// invoke method on the proxy
		String str = proxy.doSmth();
		// print out the proxy message
		System.out.println("Proxy returned " + str);
		// assert the target wasn't touched
		assertEquals(0, SomeInterfaceImplementation.INVOCATION);
		// check proxy again
		assertSame(handler, Proxy.getInvocationHandler(proxy));

	}

	/**
	 * Poor man's solution.
	 * 
	 * @return
	 */
	private InvocationHandler getInvocationHandler() {
		ServiceReference ref = bundleContext.getServiceReference(InvocationHandler.class.getName());
		if (ref == null)
			throw new IllegalStateException("no invocation handler found");
		return (InvocationHandler) bundleContext.getService(ref);
	}

	private SomeInterface createJDKProxy(InvocationHandler handler, SomeInterface target) {
		return (SomeInterface) Proxy.newProxyInstance(target.getClass().getClassLoader(),
			new Class<?>[] { SomeInterface.class }, handler);
	}
}
