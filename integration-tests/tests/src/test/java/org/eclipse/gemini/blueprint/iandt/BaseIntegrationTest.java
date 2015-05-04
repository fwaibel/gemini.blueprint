package org.eclipse.gemini.blueprint.iandt;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.AllPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyPermission;

import org.eclipse.gemini.blueprint.test.AbstractBlueprintTest;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundlePermission;
import org.osgi.framework.PackagePermission;
import org.osgi.framework.ServicePermission;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Created by dsklyut on 12/2/14.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public abstract class BaseIntegrationTest extends AbstractBlueprintTest {

	// todo: figure out how is this was used.
	private class PermissionManager implements SynchronousBundleListener {

		private final PermissionAdmin pa;

		/**
		 * Constructs a new <code>PermissionManager</code> instance.
		 *
		 * @param bc
		 */
		private PermissionManager(BundleContext bc) {
			ServiceReference<PermissionAdmin> ref = bc.getServiceReference(PermissionAdmin.class);
			if (ref != null) {
				logger.trace("Found permission admin " + ref);
				pa = (PermissionAdmin) bc.getService(ref);
				bc.addBundleListener(this);
				logger.trace("Default permissions are "
						+ ObjectUtils.nullSafeToString(pa
								.getDefaultPermissions()));
				logger.warn("Security turned ON");
			} else {
				logger.warn("Security turned OFF");
				pa = null;
			}
		}

		public void bundleChanged(BundleEvent event) {
			if (event.getType() == BundleEvent.INSTALLED) {
				Bundle bnd = event.getBundle();
				String location = bnd.getLocation();
				// iandt bundles
				if (location.indexOf("iandt") > -1
						|| location.indexOf("integration-tests") > -1) {
					logger.trace("Discovered I&T test...");
					List<Permission> perms = getIAndTPermissions();
					// define permission info
					PermissionInfo[] pi = getPIFromPermissions(perms);
					logger.info("About to set permissions " + perms
							+ " for I&T bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bnd) + "@"
							+ location);
					pa.setPermissions(location, pi);
				}
				// on the fly test
				else if (location.indexOf("onTheFly") > -1) {
					logger.trace("Discovered on the fly test...");
					List<Permission> perms = getTestPermissions();

					// define permission info
					PermissionInfo[] pi = getPIFromPermissions(perms);
					logger.info("About to set permissions " + perms
							+ " for OnTheFly bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bnd) + "@"
							+ location);
					pa.setPermissions(location, pi);
				}
				// logging bundle
				else if (bnd.getSymbolicName().indexOf("log4j.osgi") > -1) {
					logger.trace("Setting permissions on log4j bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bnd));
					List<Permission> perms = new ArrayList<Permission>();
					// defaults
					perms.add(new AllPermission());
					PermissionInfo[] defaultPerm = pa.getDefaultPermissions();
					if (defaultPerm != null)
						CollectionUtils.mergeArrayIntoCollection(defaultPerm,
								perms);
					pa.setPermissions(location, getPIFromPermissions(perms));
				}
			}
		}

		private PermissionInfo[] getPIFromPermissions(List<Permission> perms) {
			PermissionInfo[] pi = new PermissionInfo[perms.size()];
			int index = 0;
			for (Iterator<Permission> iterator = perms.iterator(); iterator.hasNext();) {
				Permission perm = (Permission) iterator.next();
				pi[index++] = new PermissionInfo(perm.getClass().getName(),
						perm.getName(), perm.getActions());
			}
			return pi;
		}
	}

	/**
	 * Returns the list of permissions for the running test.
	 *
	 * @return
	 */
	protected List<Permission> getTestPermissions() {
		List<Permission> perms = new ArrayList<Permission>();
		perms.add(new PackagePermission("*", PackagePermission.EXPORT));
		perms.add(new PackagePermission("*", PackagePermission.IMPORT));
		perms.add(new BundlePermission("*", BundlePermission.HOST));
		perms.add(new BundlePermission("*", BundlePermission.PROVIDE));
		perms.add(new BundlePermission("*", BundlePermission.REQUIRE));
		perms.add(new ServicePermission("*", ServicePermission.REGISTER));
		perms.add(new ServicePermission("*", ServicePermission.GET));
		perms.add(new PropertyPermission("*", "read,write"));
		// required by Spring
		perms.add(new RuntimePermission("*", "accessDeclaredMembers"));
		perms.add(new ReflectPermission("*", "suppressAccessChecks"));
		// logging permission
		perms.add(new FilePermission("-", "write"));
		perms.add(new FilePermission("-", "read"));
		return perms;
	}

	protected List<Permission> getIAndTPermissions() {
		List<Permission> perms = new ArrayList<Permission>();
		// export package
		perms.add(new PackagePermission("*", PackagePermission.EXPORT));
		perms.add(new PackagePermission("*", PackagePermission.IMPORT));
		perms.add(new BundlePermission("*", BundlePermission.FRAGMENT));
		perms.add(new BundlePermission("*", BundlePermission.PROVIDE));
		perms.add(new ServicePermission("*", ServicePermission.REGISTER));
		perms.add(new ServicePermission("*", ServicePermission.GET));
		perms.add(new PropertyPermission("*", "read,write"));

		// required by Spring
		perms.add(new RuntimePermission("*", "accessDeclaredMembers"));
		perms.add(new ReflectPermission("*", "suppressAccessChecks"));

		// logging permission
		perms.add(new FilePermission("-", "write"));
		perms.add(new FilePermission("-", "read"));

		return perms;
	}

	protected String[] getBundleContentPattern() {
		String pkg = getClass().getPackage().getName().replace('.', '/')
				.concat("/");
		return new String[] {
				BaseIntegrationTest.class.getName().replace('.', '/')
						.concat("*.class"), pkg + "**/*" };
	}
}
