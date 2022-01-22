package com.mirth.connect.server.util;

import java.lang.reflect.Constructor;

import com.mirth.connect.donkey.model.channel.DebugOptions;

public class DebuggerUtil {

    public static DebugOptions parseDebugOptions(String debugOptionsString) {
        String[] options = debugOptionsString.split(",");
        Object[] params = new Object[options.length];
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals("t")) {
                params[i] = true;
            } else {
                params[i] = false;
            }
        }
        Object debugOptions = null;

        try {
            Constructor<?> c = Class.forName("com.mirth.connect.donkey.model.channel.DebugOptions").getDeclaredConstructors()[0];
            debugOptions = c.newInstance(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        List<String> debugOptionsList = Arrays.asList(debugOptionsString.split(","));

//        debugOptionsList.forEach(option -> {
//        });
//        Object debugOptions = null;
//        
//        try {
//            Class clazz = Class.forName("com.mirth.connect.donkey.model.channel.DebugOptions");
//            java.lang.reflect.Constructor constructor = clazz.getConstructor(new Class[] { DebugOptions.class });
//            debugOptions = constructor.newInstance(new Object[] { true, true, true, true, true, true, true  });
//            
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

//        Class arguments[] = new Class[] {};
//        java.lang.reflect.Method objMethod = clazz.getMethod(methodName, arguments);
//        Object result = objMethod.invoke(invoker, (Object[]) arguments);
//        System.out.println(result);

//        DebugOptions debugOptions = new DebugOptions();

        return (DebugOptions) debugOptions;
    }

}
