/* 
* $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/transport/vm/Handler.java,v 1.1 2005/08/26 09:07:10 rossmason Exp $
* $Revision: 1.1 $
* $Date: 2005/08/26 09:07:10 $
* ------------------------------------------------------------------------------------------------------
* 
* Copyright (c) SymphonySoft Limited. All rights reserved.
* http://www.symphonysoft.com
* 
* The software in this package is published under the terms of the BSD
* style license a copy of which has been included with this distribution in
* the LICENSE.txt file. 
*
*/
package org.mule.providers.soap.axis.transport.vm;

import org.mule.providers.soap.axis.transport.VoidURLConnection;

import java.net.URL;
import java.net.URLConnection;

/**
 * A Dummy Url handler for handling vm.  This is needed becuase Axis uses urlStreamHandlers
 * to parse non-http urls.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public class Handler extends java.net.URLStreamHandler
{
    protected URLConnection openConnection(URL url) {
        return new VoidURLConnection(url);
    }
}
