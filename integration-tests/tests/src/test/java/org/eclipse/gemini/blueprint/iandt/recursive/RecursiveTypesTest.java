package org.eclipse.gemini.blueprint.iandt.recursive;

import static org.eclipse.gemini.blueprint.test.BlueprintOptions.blueprintDefaults;
import static org.eclipse.gemini.blueprint.test.BlueprintOptions.withLogging;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class RecursiveTypesTest extends BaseIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                blueprintDefaults(),
                withLogging(new File(PathUtils.getBaseDir() + "/target/test-classes/logback.xml").toURI()),
                mavenBundle("org.eclipse.gemini.blueprint.iandt", "recursive").versionAsInProject().noStart()
        		);
    }

	@Test
	public void testBeanReference() throws Exception {

		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.recursive");
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}
}
