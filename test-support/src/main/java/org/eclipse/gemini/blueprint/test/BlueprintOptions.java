package org.eclipse.gemini.blueprint.test;

import java.io.File;
import java.net.URI;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Created by dsklyut on 11/12/14.
 */
public class BlueprintOptions {

	/*
	 * // FIXME: the JAXP package (for 1.4 VMs) should be discovered in an OSGi
	 * manner defaults.add("org.apache.xerces.jaxp.*");
	 */

	/**
	 * Enables old-school equinox console.
	 *
	 * @param port
	 * @return
	 */
	public static Option withConsole(int port) {
		return composite(
				systemProperty("osgi.console").value(String.valueOf(port)),
				systemProperty("osgi.console.enable.builtin").value("true"));
	}

	public static Option withLogging() {
		return withLogging(new File(PathUtils.getBaseDir()
				+ "/target/classes/logback.xml").toURI());
	}

	public static Option withLogging(URI configuration) {
		// Set logback configuration via system property.
		return systemProperty("logback.configurationFile").value(
				configuration.toString());

	}

	public static Option blueprintDefaults() {
		return composite(
				bootDelegationPackages("sun.*", "com.sun.*", "org.w3c.*",
						"org.xml.*", "javax.*", "javax.sql.*",
						"javax.transaction.*", "javax.activation.*",
						"org.apache.xerces.jaxp.*"), cleanCaches(false),
				junitBundles(), coreBundles()

		);
	}

	public static Option blueprintDefaultsWithoutExtender() {
		return composite(
				bootDelegationPackages("sun.*", "com.sun.*", "org.w3c.*",
						"org.xml.*", "javax.*", "javax.sql.*",
						"javax.transaction.*", "javax.activation.*",
						"org.apache.xerces.jaxp.*"), cleanCaches(false),
				junitBundles(), coreBundlesWithoutExtender()

		);
	}

	public static Option coreBundlesWithoutExtender() {
        return composite(

//      // pax
//      url( "link:classpath:META-INF/links/org.ops4j.pax.exam.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.base.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
//      url( "link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),

      mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
      mavenBundle("org.slf4j", "jcl-over-slf4j").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
      mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),
      mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES),

      // junit bundle
//      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.junit").versionAsInProject(),

      // add spring bundles
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.aopalliance").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-beans").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-core").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-context").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-expression").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-aop").versionAsInProject(),
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.spring-test").versionAsInProject(),

      // add blueprint bundles
      mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-io").versionAsInProject(),
      mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-core").versionAsInProject(),
      mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-test").versionAsInProject()
);
    }

	public static Option coreBundles() {
		return composite(
				coreBundlesWithoutExtender(),
				mavenBundle("org.eclipse.gemini.blueprint", "gemini-blueprint-extender").versionAsInProject().start()
		);
    }
}
