///******************************************************************************
// * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * and Apache License v2.0 which accompanies this distribution. 
// * The Eclipse Public License is available at 
// * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
// * is available at http://www.opensource.org/licenses/apache2.0.php.
// * You may elect to redistribute this code under either of these licenses. 
// *
// * Contributors:
// *   VMware Inc.
// *   Oracle Inc.
// *****************************************************************************/
//
//package org.eclipse.gemini.blueprint.iandt.cardinality0to1;
//
//import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
//import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNotSame;
//import static org.junit.Assert.fail;
//import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
//import static org.ops4j.pax.exam.CoreOptions.options;
//
//import java.io.File;
//import java.io.FilePermission;
//import java.security.Permission;
//import java.util.List;
//import java.util.PropertyPermission;
//
//import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
//import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.MyListener;
//import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.ReferenceContainer;
//import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
//import org.junit.Test;
//import org.ops4j.pax.exam.Configuration;
//import org.ops4j.pax.exam.Option;
//import org.ops4j.pax.exam.util.PathUtils;
//import org.osgi.framework.AdminPermission;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleException;
//import org.springframework.test.context.ContextConfiguration;
//
///**
// * @author Hal Hildebrand Date: Dec 6, 2006 Time: 6:04:42 PM
// */
//@ContextConfiguration(locations = {"classpath:org/eclipse/gemini/blueprint/iandt/bundleScope/scope-context.xml"})
//public class Cardinality0to1Test extends BaseIntegrationTest {
//
//    @Configuration
//    public Option[] config() {
//        return options(blueprintDefaults(),
//                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
//                mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service").versionAsInProject(),
//                mavenBundle("org.eclipse.gemini.blueprint.iandt", "cardinality0to1").versionAsInProject());
//    }
//
//    @Test
//    public void test0to1Cardinality() throws Exception {
//
// TODO - find solution how to use versionAsInProject within test method
//        String url = mavenBundle("org.eclipse.gemini.blueprint.iandt", "simple.service2").versionAsInProject().getURL();
//
//        Bundle simpleService2Bundle = bundleContext.installBundle(url);
//
//        assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);
//
//        assertNotSame("simple service 2 bundle is in the activated state!", Bundle.ACTIVE, simpleService2Bundle.getState());
//
//        assertEquals("Unxpected initial binding of service", 0, MyListener.BOUND_COUNT);
//        assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
//        assertNotNull("Service reference should be not null", ReferenceContainer.service);
//
//        try {
//            ReferenceContainer.service.stringValue();
//            fail("Service should be unavailable");
//        } catch (ServiceUnavailableException e) {
//            // expected
//        }
//
//        startDependency(simpleService2Bundle);
//
//        assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
//        assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
//        assertNotNull("Service reference should be not null", ReferenceContainer.service);
//
//        assertNotNull(ReferenceContainer.service.stringValue());
//
//    }
//
//    private void startDependency(Bundle simpleService2Bundle) throws BundleException, InterruptedException {
//        System.out.println("Starting dependency");
//        simpleService2Bundle.start();
//
//        waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.simpleservice2");
//
//        System.out.println("Dependency started");
//    }
//
//	protected List<Permission> getIAndTPermissions() {
//		List<Permission> perms = super.getIAndTPermissions();
//		// export package
//		perms.add(new PropertyPermission("*", "read"));
//		perms.add(new PropertyPermission("*", "write"));
//		return perms;
//	}
//
//	protected List<Permission> getTestPermissions() {
//		List<Permission> perms = super.getTestPermissions();
//		perms.add(new FilePermission("<<ALL FILES>>", "read"));
//		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
//		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
//		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
//		perms.add(new PropertyPermission("*", "read"));
//		perms.add(new PropertyPermission("*", "write"));
//		return perms;
//	}
//}
