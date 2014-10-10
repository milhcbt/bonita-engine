/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.persistence;

import java.util.Collections;

import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SelectListDescriptorTest {

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.SelectListDescriptor#SelectListDescriptor(java.lang.String, java.util.Map, java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public final void throw_exception_when_contruct_object_without_query_options() {
        new SelectListDescriptor<Object>("queryName", Collections.EMPTY_MAP, PersistentObject.class, null);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.SelectListDescriptor#SelectListDescriptor(java.lang.String, java.util.Map, java.lang.Class, java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public final void throw_exception_when_contruct_object_without_query_options2() {
        new SelectListDescriptor<Object>("queryName", Collections.EMPTY_MAP, PersistentObject.class, Object.class, null);
    }

}
