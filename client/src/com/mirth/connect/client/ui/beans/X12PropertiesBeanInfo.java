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

public class X12PropertiesBeanInfo extends EDIPropertiesBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( com.mirth.connect.client.ui.beans.X12Properties.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_elementDelimiter = 0;
    private static final int PROPERTY_inferX12Delimiters = 1;
    private static final int PROPERTY_segmentDelimiter = 2;
    private static final int PROPERTY_subelementDelimiter = 3;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[4];
    
        try {
            properties[PROPERTY_elementDelimiter] = new PropertyDescriptor ( "elementDelimiter", com.mirth.connect.client.ui.beans.X12Properties.class, "getElementDelimiter", "setElementDelimiter" ); // NOI18N
            properties[PROPERTY_elementDelimiter].setDisplayName ( "Element Delimiter" );
            properties[PROPERTY_elementDelimiter].setShortDescription ( "Characters that delimit the elements in the message." );
            properties[PROPERTY_elementDelimiter].setBound ( true );
            properties[PROPERTY_inferX12Delimiters] = new PropertyDescriptor ( "inferX12Delimiters", com.mirth.connect.client.ui.beans.X12Properties.class, "isInferX12Delimiters", "setInferX12Delimiters" ); // NOI18N
            properties[PROPERTY_inferX12Delimiters].setDisplayName ( "Infer X12 Delimiters" );
            properties[PROPERTY_inferX12Delimiters].setShortDescription ( "Infer the standard X12 delimiters." );
            properties[PROPERTY_inferX12Delimiters].setBound ( true );
            properties[PROPERTY_segmentDelimiter] = new PropertyDescriptor ( "segmentDelimiter", com.mirth.connect.client.ui.beans.X12Properties.class, "getSegmentDelimiter", "setSegmentDelimiter" ); // NOI18N
            properties[PROPERTY_segmentDelimiter].setDisplayName ( "Segment Delimiter" );
            properties[PROPERTY_segmentDelimiter].setShortDescription ( "Characters that delimit the segments in the message." );
            properties[PROPERTY_segmentDelimiter].setBound ( true );
            properties[PROPERTY_subelementDelimiter] = new PropertyDescriptor ( "subelementDelimiter", com.mirth.connect.client.ui.beans.X12Properties.class, "getSubelementDelimiter", "setSubelementDelimiter" ); // NOI18N
            properties[PROPERTY_subelementDelimiter].setDisplayName ( "Subelement Delimiter" );
            properties[PROPERTY_subelementDelimiter].setShortDescription ( "Characters that delimit the subelements in the message." );
            properties[PROPERTY_subelementDelimiter].setBound ( true );
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

