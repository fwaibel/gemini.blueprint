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

package org.eclipse.gemini.blueprint.internal.service.collection;

import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.gemini.blueprint.GCTests;
import org.junit.Test;

/**
 * @author Costin Leau
 */
public class WeakCollectionTest {

    @Test
    public void testWeakList() {
        List<WeakReference<?>> list = new ArrayList<WeakReference<?>>();

        // add some weak references
        for (int i = 0; i < 20; i++) {
            list.add(new WeakReference<Object>(new Object()));
        }

        GCTests.assertGCed(new WeakReference<Object>(new Object()));
        for (int i = 0; i < list.size(); i++) {
            assertNull((list.get(i)).get());
        }
    }

    @Test
    public void testWeakHashMap() {
        Map<Object, Object> weakMap = new WeakHashMap<Object, Object>();

        for (int i = 0; i < 10; i++) {
            weakMap.put(new Object(), null);
        }

        GCTests.assertGCed(new WeakReference<Object>(new Object()));

        Set<Map.Entry<Object, Object>> entries = weakMap.entrySet();

        for (Iterator<Map.Entry<Object, Object>> iter = entries.iterator(); iter.hasNext(); ) {
            Map.Entry<Object, Object> entry = iter.next();
            assertNull(entry.getKey());
        }

    }
}
