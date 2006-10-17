/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/ServiceProxy.java,v 1.4 2005/09/02 11:00:21 rossmason Exp $
 * $Revision: 1.4 $
 * $Date: 2005/09/02 11:00:21 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap;

import electric.service.IService;
import org.mule.config.MuleProperties;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.soap.glue.GlueMessageAdapter;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.ClassHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <code>ServiceProxy</code> is a proxy that wraps a soap endpointUri to look
 * like a Web service.
 * 
 * Also provides helper methods for building and describing web service
 * interfaces in Mule.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */

public class ServiceProxy
{
    public static Class[] getInterfacesForComponent(UMOComponent component) throws UMOException, ClassNotFoundException
    {
        Class[] interfaces = new Class[0];
        List ifaces = (List) component.getDescriptor().getProperties().get("serviceInterfaces");
        if (ifaces == null || ifaces.size() == 0) {
            interfaces = component.getDescriptor().getImplementationClass().getInterfaces();

        } else {
            interfaces = new Class[ifaces.size()];
            for (int i = 0; i < ifaces.size(); i++) {
                String iface = (String) ifaces.get(i);
                interfaces[i] = ClassHelper.loadClass(iface, ServiceProxy.class);
            }
        }

        interfaces = removeInterface(interfaces, Callable.class);
        return interfaces;
    }

    public static Class[] removeInterface(Class[] interfaces, Class iface)
    {
        if (interfaces == null)
            return null;
        List results = new ArrayList();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (!anInterface.equals(iface)) {
                results.add(anInterface);
            }
        }
        Class[] arResults = new Class[results.size()];
        if (arResults.length == 0) {
            return arResults;
        } else {
            results.toArray(arResults);
            return arResults;
        }
    }

    public static Method[] getMethods(Class[] interfaces)
    {
        List methodNames = new ArrayList();
        for (int i = 0; i < interfaces.length; i++) {
            methodNames.addAll(Arrays.asList(interfaces[i].getMethods()));
        }
        Method[] results = new Method[methodNames.size()];
        return (Method[]) methodNames.toArray(results);

    }

    public static String[] getMethodNames(Class[] interfaces)
    {
        Method[] methods = getMethods(interfaces);

        String[] results = new String[methods.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = methods[i].getName();
        }
        return results;
    }

    public static Object createGlueProxy(AbstractMessageReceiver receiver, boolean synchronous, Class[] classes)
    {
        return Proxy.newProxyInstance(ServiceProxy.class.getClassLoader(),
                                      classes,
                                      createGlueServiceHandler(receiver, synchronous));
    }

    public static InvocationHandler createGlueServiceHandler(AbstractMessageReceiver receiver, boolean synchronous)
    {
        return new GlueServiceHandler(receiver, synchronous);
    }

    public static Object createAxisProxy(AbstractMessageReceiver receiver, boolean synchronous, Class[] classes)
    {
        return Proxy.newProxyInstance(ServiceProxy.class.getClassLoader(),
                                      classes,
                                      createAxisServiceHandler(receiver, synchronous));
    }

    public static InvocationHandler createAxisServiceHandler(AbstractMessageReceiver receiver, boolean synchronous)
    {
        return new AxisServiceHandler(receiver, synchronous);
    }

    private static class AxisServiceHandler implements InvocationHandler
    {
        private AbstractMessageReceiver receiver;
        private boolean synchronous = true;

        public AxisServiceHandler(AbstractMessageReceiver receiver, boolean synchronous)
        {
            this.receiver = receiver;
            this.synchronous = synchronous;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            UMOMessageAdapter messageAdapter = receiver.getConnector().getMessageAdapter(args);
            messageAdapter.setProperty(MuleProperties.MULE_METHOD_PROPERTY, method);

            UMOMessage message = receiver.routeMessage(new MuleMessage(messageAdapter), synchronous);
            if (message != null) {
                return message.getPayload();
            } else {
                return null;
            }
        }
    }

    private static class GlueServiceHandler implements InvocationHandler
    {
        private AbstractMessageReceiver receiver;
        private boolean synchronous = true;

        public GlueServiceHandler(AbstractMessageReceiver receiver, boolean synchronous)
        {
            this.receiver = receiver;
            this.synchronous = synchronous;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            GlueMessageAdapter.GlueMessageHolder holder;
            if (args.length == 1) {
                holder = new GlueMessageAdapter.GlueMessageHolder(args[0], (IService) proxy);
            } else {
                holder = new GlueMessageAdapter.GlueMessageHolder(args, (IService) proxy);
            }
            UMOMessageAdapter messageAdapter = receiver.getConnector().getMessageAdapter(holder);
            messageAdapter.setProperty(MuleProperties.MULE_METHOD_PROPERTY, method);

            UMOMessage message = receiver.routeMessage(new MuleMessage(messageAdapter), synchronous);
            if(message.getExceptionPayload()!=null) {
                throw message.getExceptionPayload().getException();
            }
            if (message != null) {
                return message.getPayload();
            } else {
                return null;
            }
        }
    }
}
