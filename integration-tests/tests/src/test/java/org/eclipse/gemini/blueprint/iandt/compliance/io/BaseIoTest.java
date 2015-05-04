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

package org.eclipse.gemini.blueprint.iandt.compliance.io;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.eclipse.gemini.blueprint.test.FilteringProbeBuilder;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;

/**
 * Common base test class for IO integration testing.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BaseIoTest extends BaseIntegrationTest {

	protected final static String PACKAGE = "org/eclipse/gemini/blueprint/iandt/io/";
	private static final String FRAGMENT_1 = "org.eclipse.gemini.blueprint.iandt.io.fragment.1";
	private static final String FRAGMENT_2 = "org.eclipse.gemini.blueprint.iandt.io.fragment.2";

	protected Resource thisClass;

	protected ResourceLoader loader, defaultLoader;

	protected ResourcePatternResolver patternLoader;

	protected Bundle bundle;

	protected String[] getBundleContentPattern() {
		return ObjectUtils.addObjectToArray(super.getBundleContentPattern(),
			"org/eclipse/gemini/blueprint/iandt/io/BaseIoTest.class");
	}

	@Before
	public void onSetUp() throws Exception {
		// load file using absolute path
		defaultLoader = new DefaultResourceLoader();
		thisClass = defaultLoader.getResource(getClass().getName().replace('.', '/').concat(".class"));
		bundle = bundleContext.getBundle();
		loader = new OsgiBundleResourceLoader(bundle);
		patternLoader = new OsgiBundleResourcePatternResolver(loader);

	}

	@After
	public void onTearDown() throws Exception {
		thisClass = null;
	}

	protected String getManifestLocation() {
		// reuse the manifest from Fragment Io Tests
		return "org/eclipse/gemini/blueprint/iandt/io/FragmentIoTests.MF";
	}

	@ProbeBuilder
	public TestProbeBuilder customizeProbe(TestProbeBuilder builder) {
		FilteringProbeBuilder custom = new FilteringProbeBuilder(builder.getTempDir());
		custom.addContentPattern(getBundleContentPattern());
		custom.setManifestLocation(getManifestLocation());

		custom.setHeader(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "org.eclipse.gemini.blueprint.iandt.io.functional.fragments");
		postProcessProbeCustomization(custom);
		return custom;
	}

	protected TestProbeBuilder postProcessProbeCustomization(TestProbeBuilder builder) {
		return builder;
	}

	@Configuration
	public Option[] config() {
		return options(
				blueprintDefaults(),
				withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
				mavenBundle("org.eclipse.gemini.blueprint.iandt", "io.fragment.1.bundle").versionAsInProject().start(false),
				mavenBundle("org.eclipse.gemini.blueprint.iandt", "io.fragment.2.bundle").versionAsInProject().start(false));
	}

	protected Object[] copyEnumeration(Enumeration<?> enm) {
		List<Object> list = new ArrayList<>();
		while (enm != null && enm.hasMoreElements())
			list.add(enm.nextElement());
		return list.toArray();
	}

	protected void assertResourceArray(Object[] array, int expectedSize) {
		logger.debug(ObjectUtils.nullSafeToString(array));
		assertTrue("found only " + ObjectUtils.nullSafeToString(array), array.length == expectedSize);
	}

	protected List<Permission> getTestPermissions() {
		List<Permission> list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		// log files
		list.add(new FilePermission("<<ALL FILES>>", "delete"));
		list.add(new FilePermission("<<ALL FILES>>", "write"));
		list.add(new AdminPermission("*", AdminPermission.LISTENER));
		list.add(new AdminPermission("(name=" + FRAGMENT_1 + ")", AdminPermission.RESOURCE));
		list.add(new AdminPermission("(name=" + FRAGMENT_2 + ")", AdminPermission.RESOURCE));
		return list;
	}

	protected void printPathWithinContext(Resource[] resources) {
        for (Resource resource : resources) {
            assertTrue(resource instanceof ContextResource);
            logger.debug("Path within context " + ((ContextResource) resource).getPathWithinContext());
        }
	}
}