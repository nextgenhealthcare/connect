/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/extensions/MuleProvider.java,v 1.7 2005/08/28 10:24:33 rossmason Exp $
 * $Revision: 1.7 $
 * $Date: 2005/08/28 10:24:33 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.soap.axis.extensions;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.mule.MuleManager;
import org.mule.impl.RequestContext;
import org.mule.umo.UMOSession;

import com.webreach.mirth.connectors.soap.ServiceProxy;
import com.webreach.mirth.connectors.soap.ServiceProxy.AxisServiceHandler;
import com.webreach.mirth.connectors.soap.axis.AxisConnector;
import com.webreach.mirth.connectors.soap.axis.AxisMessageReceiver;

/**
 * <code>MuleProvider</code> Is an Axis service endpoint that builds services
 * from Mule managed components
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */
public class MuleProvider extends RPCProvider
{
    private AxisConnector connector;
    private AxisMessageReceiver receiver;
    private AxisServiceHandler handler;
    public MuleProvider(AxisConnector connector)
    {
        this.connector = connector;
    }

    protected Object makeNewServiceObject(MessageContext messageContext, String s) throws Exception
    {
        receiver = (AxisMessageReceiver)connector.getReceiver(messageContext.getTargetService());
        if (receiver == null) {
            throw new AxisFault("Could not find Mule registered service: " + s);
        }
        Class[] classes = ServiceProxy.getInterfaceForClass("com.webreach.mirth.server.mule.components.SoapService");
        return ServiceProxy.createAxisProxy(receiver, true, classes);
    }
    /**
     * This method encapsulates the method invocation.             
     *
     * @param msgContext MessageContext
     * @param method the target method.
     * @param obj the target object
     * @param argValues the method arguments
     */
    protected Object invokeMethod(MessageContext msgContext,
                                  Method method, Object obj,
                                  Object[] argValues)
            throws Exception {
    		try{
    			return new ServiceProxy.AxisServiceHandler(receiver, true).invoke(obj, method, argValues);
    		}catch (Throwable e){
    			throw new Exception(e);
    		}
   // 	return ServiceProxy.createAxisServiceHandler(msgCon, synchronous)
   //     return (method.invoke(obj, argValues));
        	
        }
    protected Class getServiceClass(String s, SOAPService soapService, MessageContext messageContext) throws AxisFault
    {
        UMOSession session = MuleManager.getInstance().getModel().getComponentSession(soapService.getName());
        try {
            Class[] classes =  ServiceProxy.getInterfaceForClass("com.webreach.mirth.server.mule.components.SoapService");
            return Proxy.getProxyClass(Thread.currentThread().getContextClassLoader(), classes);
        } catch (Exception e) {
            throw new AxisFault("Failed to implementation class for component: " + e.getMessage(), e);
        }
    }

    public void invoke(MessageContext msgContext) throws AxisFault
    {
        super.invoke(msgContext);
        if (RequestContext.getExceptionPayload() != null) {
            Throwable t = RequestContext.getExceptionPayload().getException();
            if (t instanceof Exception) {
                AxisFault fault = AxisFault.makeFault((Exception) t);
                if (t instanceof RuntimeException)
                    fault.addFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION, "true");
                throw fault;
            } else {
                throw (Error) t;
            }
        }
    }

}
