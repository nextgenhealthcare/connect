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


package org.mule.providers.rmi;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.ClassHelper;
import org.mule.util.Utility;

import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;

/**
 * <code>RmiConnector</code> can bind or sent to a given rmi port on a given
 * host.
 * 
 * @author <a href="mailto:fsweng@bass.com.my">fs Weng</a>
 * @version $Revision: 1.6 $
 */
public class RmiConnector extends AbstractServiceEnabledConnector
{
    public static final int DEFAULT_RMI_REGISTRY_PORT = 1099;

    public static final int MSG_PARAM_SERVICE_METHOD_NOT_SET = 1;

    public static final int MSG_PROPERTY_SERVICE_METHOD_PARAM_TYPES_NOT_SET = 2;

    public static final String PROPERTY_RMI_SECURITY_POLICY = "securityPolicy";

    public static final String PROPERTY_RMI_SERVER_CODEBASE = "serverCodebase";

    public static final String PROPERTY_SERVER_CLASS_NAME = "serverClassName";

    public static final String PROPERTY_SERVICE_METHOD_PARAM_TYPES = "methodArgumentTypes";

    public static final String PARAM_SERVICE_METHOD = "method";

    private String securityPolicy = null;

    private String serverCodebase = null;

    private String serverClassName = null;

    private ArrayList methodArgumentTypes = null;

    private Class[] argumentClasses = null;

    private SecurityManager securityManager = new RMISecurityManager();

    public String getProtocol()
    {
        return "RMI";
    }

    /**
     * @return Returns the securityPolicy.
     */
    public String getSecurityPolicy()
    {
        return securityPolicy;
    }

    /**
     * @param path The securityPolicy to set.
     */
    public void setSecurityPolicy(String path)
    {
        // verify securityPolicy existence
        if (path != null) {
            URL url = Utility.getResource(path, RmiConnector.class);
            if (url == null) {
                throw new IllegalArgumentException("Error on initialization, RMI security policy does not exist");
            }
            this.securityPolicy = url.toString();
        }
    }

    /**
     * Method getServerCodebase
     * 
     * 
     * @return
     * 
     */
    public String getServerCodebase()
    {
        return (this.serverCodebase);
    }

    /**
     * Method setServerCodebase
     * 
     * 
     * @param serverCodebase
     * 
     */
    public void setServerCodebase(String serverCodebase)
    {
        this.serverCodebase = serverCodebase;
    }

    /**
     * Method getServerClassName
     * 
     * 
     * @return
     * 
     */
    public String getServerClassName()
    {
        return (this.serverClassName);
    }

    /**
     * Method setServerClassName
     * 
     * 
     * @param serverClassName
     * 
     */
    public void setServerClassName(String serverClassName)
    {
        this.serverClassName = serverClassName;
    }

    /**
     * Method getMethodArgumentTypes
     * 
     * 
     * @return
     * 
     */
    public ArrayList getMethodArgumentTypes()
    {
        return (this.methodArgumentTypes);
    }

    /**
     * Method setMethodArgumentTypes
     * 
     * 
     * @param methodArgumentTypes
     * 
     */
    public void setMethodArgumentTypes(ArrayList methodArgumentTypes) throws ClassNotFoundException
    {
        Class argumentClasses[] = null;

        this.methodArgumentTypes = methodArgumentTypes;

        if (getMethodArgumentTypes() != null)

        {
            argumentClasses = new Class[methodArgumentTypes.size()];

            for (int i = 0; i < methodArgumentTypes.size(); i++) {
                String className = (String) methodArgumentTypes.get(i);
                argumentClasses[i] = ClassHelper.loadClass(className.trim(), this.getClass());
            }
        }

        setArgumentClasses(argumentClasses);
    }

    /**
     * Method getArgumentClasses
     * 
     * 
     * @return
     * 
     */
    public Class[] getArgumentClasses()
    {
        return (this.argumentClasses);
    }

    /**
     * Method setArgumentClasses
     * 
     * 
     * @param argumentClasses
     * 
     */
    public void setArgumentClasses(Class[] argumentClasses)
    {
        this.argumentClasses = argumentClasses;
    }

    public void doInitialise() throws InitialisationException
    {
        super.doInitialise();

        if(securityPolicy!=null) {
           // System.setProperty("java.security.policy", securityPolicy);
        }

        // Set security manager
        if (securityManager != null) {
           // System.setSecurityManager(securityManager);
        }
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
