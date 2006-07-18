/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package org.mule.providers.ejb;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.rmi.RmiConnector;
import org.mule.umo.lifecycle.InitialisationException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
/*
 * Code by (c) 2005 P.Oikari.
 *
 * @author <a href="mailto:pnirvin@hotmail.com">P.Oikari</a>
 * @version $Revision: 1.1 $
 */
public class EjbConnector extends RmiConnector
{
  private String jndiInitialFactory;

  private String jndiUrlPkgPrefixes;

  private String jndiProviderUrl;

  private Context jndiContext;

  public String getProtocol()
  {
    return "ejb";
  }

  protected void initJndiContext() throws NamingException, InitialisationException
  {
    if (null == jndiContext)
    {
      Hashtable props = new Hashtable();
      jndiInitialFactory = "weblogic.jndi.WLInitialContextFactory";
 

      if (null != jndiInitialFactory)
        props.put(Context.INITIAL_CONTEXT_FACTORY, jndiInitialFactory);

      if (jndiProviderUrl != null)
        props.put(Context.PROVIDER_URL, jndiProviderUrl);

      if (jndiUrlPkgPrefixes != null)
        props.put(Context.URL_PKG_PREFIXES, jndiUrlPkgPrefixes);

      jndiContext = new InitialContext(props);
    }
  }

  public Context getJndiContext(String jndiProviderUrl) throws InitialisationException
  {
    try
    {
      setJndiProviderUrl(jndiProviderUrl);

      initJndiContext();
    }
    catch (Exception e)
    {
      throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X, "EJB Connector"), e, this);
    }

    return jndiContext;
  }

  public void setJndiContext(Context jndiContext)
  {
    this.jndiContext = jndiContext;
  }

  public void setJndiInitialFactory(String jndiInitialFactory)
  {
    this.jndiInitialFactory = jndiInitialFactory;
  }

  public String getJndiInitialFactory()
  {
    return jndiInitialFactory;
  }

  public void setJndiUrlPkgPrefixes(String jndiUrlPkgPrefixes)
  {
    this.jndiUrlPkgPrefixes = jndiUrlPkgPrefixes;
  }

  public String getJndiUrlPkgPrefixes()
  {
    return jndiUrlPkgPrefixes;
  }

  public String getJndiProviderUrl()
  {
    return jndiProviderUrl;
  }

  public void setJndiProviderUrl(String jndiProviderUrl)
  {
    this.jndiProviderUrl = "t3://" + jndiProviderUrl;
  }
}
