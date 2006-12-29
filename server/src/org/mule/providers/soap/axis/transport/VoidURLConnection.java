/* 
* $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/transport/VoidURLConnection.java,v 1.1 2005/08/29 07:34:44 rossmason Exp $
* $Revision: 1.1 $
* $Date: 2005/08/29 07:34:44 $
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
package org.mule.providers.soap.axis.transport;

import java.net.URL;

/**
 * A fake url connection used to bypass Axis's use of the URLStreamHandler to
 * mask uris as Urls.  This was also necessary because of the uncessary use of static
 * blocking in the axis URLStreamHandler objects.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public class VoidURLConnection extends java.net.URLConnection
{
    public VoidURLConnection(URL url)
    {
        super(url);
    }

    public void connect()
    {
    }
}
