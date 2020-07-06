//jTDS JDBC Driver for Microsoft SQL Server and Sybase
//Copyright (C) 2004 The jTDS Project
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package net.sourceforge.jtds.ssl;

/**
 * SSL settings
 *
 * @author Rob Worsnop
 * @author Mike Hutchinson
 * @version $Id: Ssl.java,v 1.3 2005-04-20 16:49:30 alin_sinpalean Exp $
 */
public interface Ssl {
    /**
     * SSL is not used.
     */
    String SSL_OFF = "off";
    /**
     * SSL is requested; a plain socket is used if SSL is not available.
     */
    String SSL_REQUEST = "request";
    /**
     * SSL is required; an exception if thrown if SSL is not available.
     */
    String SSL_REQUIRE = "require";
    /**
     * SSL is required and the server must return a certificate signed by a
     * client-trusted authority.
     */
    String SSL_AUTHENTICATE = "authenticate";
    /** Size of TLS record header. */
    int  TLS_HEADER_SIZE = 5;
    /** TLS Change Cipher Spec record type. */
    byte TYPE_CHANGECIPHERSPEC = 20;
    /** TLS Alert record type. */
    byte TYPE_ALERT = 21;
    /** TLS Handshake record. */
    byte TYPE_HANDSHAKE = 22;
    /** TLS Application data record. */
    byte TYPE_APPLICATIONDATA = 23;
    /** TLS Hand shake Header Size. */
    int HS_HEADER_SIZE = 4;
    /** TLS Hand shake client key exchange sub type. */
    int TYPE_CLIENTKEYEXCHANGE = 16;
    /** TLS Hand shake client hello sub type. */
    int TYPE_CLIENTHELLO = 1;

}
