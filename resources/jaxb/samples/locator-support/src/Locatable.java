/*
 * @(#)$Id: Locatable.java,v 1.1 2004/06/25 21:12:08 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.extra;

import org.xml.sax.Locator;

/**
 * Optional interface implemented by JAXB objects to expose
 * location information from which an object is unmarshalled.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Locatable {
    /**
     * @return
     *      null if the location information is unavaiable,
     *      or otherwise return a immutable valid {@link Locator}
     *      object.
     */
    Locator sourceLocation();
}
