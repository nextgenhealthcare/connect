/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util.migration;

import com.mirth.connect.donkey.util.DonkeyElement;

/**
 * <p>
 * Classes that implement this interface have the ability to migrate serialized object instances
 * from an earlier version of Mirth Connect to a later version. A custom converter is registered
 * with XStream that automatically detects objects that implement this interface and runs the
 * appropriate interface methods.
 * </p>
 * 
 * <p>
 * When adding this interface to a class in a given version of Mirth Connect, the migrate methods
 * prior to that version must be left blank. This is because serialized instances of the class will
 * not contain version information. Without version information, the migration code in
 * MigratableConverter will assume that the object could have originated from a pre-3.0.0 version
 * and will then run all migration methods from 3.0.0 onward. If a class becomes migratable in
 * version 3.x.x, then it should not need to have any migration logic prior to that version. (See
 * comments in MigratableConverter)
 * </p>
 */
public interface Migratable {
    public void migrate3_0_1(DonkeyElement element);
}
