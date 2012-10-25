/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class HL7PropertiesBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( com.mirth.connect.client.ui.beans.HL7Properties.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_handleRepetitions = 0;
    private static final int PROPERTY_handleSubcomponents = 1;
    private static final int PROPERTY_useStrictParser = 2;
    private static final int PROPERTY_useStrictValidation = 3;
    private static final int PROPERTY_stripNamespaces = 4;
    private static final int PROPERTY_convertLFtoCR = 5;
    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[6];
    
        try {
            properties[PROPERTY_handleRepetitions] = new PropertyDescriptor ( "handleRepetitions", com.mirth.connect.client.ui.beans.HL7Properties.class, "isHandleRepetitions", "setHandleRepetitions" ); // NOI18N
            properties[PROPERTY_handleRepetitions].setDisplayName ( "Parse Field Repetitions" );
            properties[PROPERTY_handleRepetitions].setShortDescription ( "Parse field repetitions (applies to Non-Strict Parser only)." );
            properties[PROPERTY_handleRepetitions].setBound ( true );
            properties[PROPERTY_handleSubcomponents] = new PropertyDescriptor ( "handleSubcomponents", com.mirth.connect.client.ui.beans.HL7Properties.class, "isHandleSubcomponents", "setHandleSubcomponents" ); // NOI18N
            properties[PROPERTY_handleSubcomponents].setDisplayName ( "Parse Subcomponents" );
            properties[PROPERTY_handleSubcomponents].setShortDescription ( "Parse subcomponents (applies to Non-Strict Parser only)." );
            properties[PROPERTY_handleSubcomponents].setBound ( true );
            properties[PROPERTY_useStrictParser] = new PropertyDescriptor ( "useStrictParser", com.mirth.connect.client.ui.beans.HL7Properties.class, "isUseStrictParser", "setUseStrictParser" ); // NOI18N
            properties[PROPERTY_useStrictParser].setDisplayName ( "Use Strict Parser" );
            properties[PROPERTY_useStrictParser].setShortDescription ( "Parse messages based upon strict HL7 specifications." );
            properties[PROPERTY_useStrictParser].setBound ( true );
            properties[PROPERTY_useStrictValidation] = new PropertyDescriptor ( "useStrictValidation", com.mirth.connect.client.ui.beans.HL7Properties.class, "isUseStrictValidation", "setUseStrictValidation" ); // NOI18N
            properties[PROPERTY_useStrictValidation].setDisplayName ( "Validate in Strict Parser" );
            properties[PROPERTY_useStrictValidation].setShortDescription ( "Validate messages using HL7 specifications (applies to Strict Parser only)." );
            properties[PROPERTY_useStrictValidation].setBound ( true );
            properties[PROPERTY_stripNamespaces] = new PropertyDescriptor ( "stripNamespaces", com.mirth.connect.client.ui.beans.HL7Properties.class, "isStripNamespaces", "setStripNamespaces" ); // NOI18N
            properties[PROPERTY_stripNamespaces].setDisplayName ( "Strip Namespaces" );
            properties[PROPERTY_stripNamespaces].setShortDescription ( "Strips namespace definitions from the transformed XML message (applies to Strict Parser only).  Will not remove namespace prefixes.  If you do not strip namespaces your default xml namespace will be set to the incoming data namespace.  If your outbound template namespace is different, you will have to set \"default xml namespace = 'namespace';\" via JavaScript before template mappings." );
            properties[PROPERTY_stripNamespaces].setBound ( true );
            properties[PROPERTY_convertLFtoCR] = new PropertyDescriptor ( "convertLFtoCR", com.mirth.connect.client.ui.beans.HL7Properties.class, "isConvertLFtoCR", "setConvertLFtoCR" ); // NOI18N
            properties[PROPERTY_convertLFtoCR].setDisplayName ( "Convert LF to CR" );
            properties[PROPERTY_convertLFtoCR].setShortDescription ( "Convert linefeeds (\\n) to carriage returns (\\r) automatically (applies to Non-Strict Parser only)." );
            properties[PROPERTY_convertLFtoCR].setBound ( true );
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        return properties;     }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;     }//GEN-LAST:Events

    // Method identifiers//GEN-FIRST:Methods

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.

        return methods;     }//GEN-LAST:Methods
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx

//GEN-FIRST:Superclass
    // Here you can add code for customizing the Superclass BeanInfo.
//GEN-LAST:Superclass
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}

