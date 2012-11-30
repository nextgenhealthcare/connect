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

    // The BeanInfo editor resets the property display order to alphabetical based on variable name
    // each time changes are made.  Here is a copy of the desired display order to put it back to 
    // the desired, default display order (not alphabetic).
    // @formatter:off
    /*
    private static final int PROPERTY_handleRepetitions = 0;
    private static final int PROPERTY_handleSubcomponents = 1;
    private static final int PROPERTY_useStrictParser = 2;
    private static final int PROPERTY_useStrictValidation = 3;
    private static final int PROPERTY_stripNamespaces = 4;
    private static final int PROPERTY_inputSegmentDelimiter = 5;
    private static final int PROPERTY_outputSegmentDelimiter = 6;
    private static final int PROPERTY_successfulACKCode = 7;
    private static final int PROPERTY_successfulACKMessage = 8;
    private static final int PROPERTY_errorACKCode = 9;
    private static final int PROPERTY_errorACKMessage = 10;
    private static final int PROPERTY_rejectedACKCode = 11;
    private static final int PROPERTY_rejectedACKMessage = 12;
    private static final int PROPERTY_msh15ACKAccept = 13;
    */
    // @formatter:on

    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_handleRepetitions = 0;
    private static final int PROPERTY_handleSubcomponents = 1;
    private static final int PROPERTY_useStrictParser = 2;
    private static final int PROPERTY_useStrictValidation = 3;
    private static final int PROPERTY_stripNamespaces = 4;
    private static final int PROPERTY_inputSegmentDelimiter = 5;
    private static final int PROPERTY_outputSegmentDelimiter = 6;
    private static final int PROPERTY_successfulACKCode = 7;
    private static final int PROPERTY_successfulACKMessage = 8;
    private static final int PROPERTY_errorACKCode = 9;
    private static final int PROPERTY_errorACKMessage = 10;
    private static final int PROPERTY_rejectedACKCode = 11;
    private static final int PROPERTY_rejectedACKMessage = 12;
    private static final int PROPERTY_msh15ACKAccept = 13;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[14];
    
        try {
            properties[PROPERTY_errorACKCode] = new PropertyDescriptor ( "errorACKCode", com.mirth.connect.client.ui.beans.HL7Properties.class, "getErrorACKCode", "setErrorACKCode" ); // NOI18N
            properties[PROPERTY_errorACKCode].setDisplayName ( "Error ACK Code" );
            properties[PROPERTY_errorACKCode].setShortDescription ( "The ACK code to respond with when an error occurs during message processing. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_errorACKCode].setBound ( true );
            properties[PROPERTY_errorACKMessage] = new PropertyDescriptor ( "errorACKMessage", com.mirth.connect.client.ui.beans.HL7Properties.class, "getErrorACKMessage", "setErrorACKMessage" ); // NOI18N
            properties[PROPERTY_errorACKMessage].setDisplayName ( "Error ACK Message" );
            properties[PROPERTY_errorACKMessage].setShortDescription ( "The ACK message to respond with when an error occurs during message processing. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_errorACKMessage].setBound ( true );
            properties[PROPERTY_handleRepetitions] = new PropertyDescriptor ( "handleRepetitions", com.mirth.connect.client.ui.beans.HL7Properties.class, "isHandleRepetitions", "setHandleRepetitions" ); // NOI18N
            properties[PROPERTY_handleRepetitions].setDisplayName ( "Parse Field Repetitions" );
            properties[PROPERTY_handleRepetitions].setShortDescription ( "Parse field repetitions (applies to Non-Strict Parser only)." );
            properties[PROPERTY_handleRepetitions].setBound ( true );
            properties[PROPERTY_handleSubcomponents] = new PropertyDescriptor ( "handleSubcomponents", com.mirth.connect.client.ui.beans.HL7Properties.class, "isHandleSubcomponents", "setHandleSubcomponents" ); // NOI18N
            properties[PROPERTY_handleSubcomponents].setDisplayName ( "Parse Subcomponents" );
            properties[PROPERTY_handleSubcomponents].setShortDescription ( "Parse subcomponents (applies to Non-Strict Parser only)." );
            properties[PROPERTY_handleSubcomponents].setBound ( true );
            properties[PROPERTY_inputSegmentDelimiter] = new PropertyDescriptor ( "inputSegmentDelimiter", com.mirth.connect.client.ui.beans.HL7Properties.class, "getInputSegmentDelimiter", "setInputSegmentDelimiter" ); // NOI18N
            properties[PROPERTY_inputSegmentDelimiter].setDisplayName ( "Input Segment Delimiter" );
            properties[PROPERTY_inputSegmentDelimiter].setShortDescription ( "For inbound properties, this is the input delimiter character(s) expected to occur after each segment. For inbound and outbound properties, this is used to serialize the message from the message template." );
            properties[PROPERTY_inputSegmentDelimiter].setBound ( true );
            properties[PROPERTY_msh15ACKAccept] = new PropertyDescriptor ( "msh15ACKAccept", com.mirth.connect.client.ui.beans.HL7Properties.class, "isMsh15ACKAccept", "setMsh15ACKAccept" ); // NOI18N
            properties[PROPERTY_msh15ACKAccept].setDisplayName ( "MSH-15 ACK Accept" );
            properties[PROPERTY_msh15ACKAccept].setShortDescription ( "This setting determines if Mirth should check the MSH-15 field of an incoming message to control the acknowledgment conditions. The MSH-15 field specifies if a message should be always acknowledged, never acknowledged, or only acknowledged on error. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_msh15ACKAccept].setBound ( true );
            properties[PROPERTY_outputSegmentDelimiter] = new PropertyDescriptor ( "outputSegmentDelimiter", com.mirth.connect.client.ui.beans.HL7Properties.class, "getOutputSegmentDelimiter", "setOutputSegmentDelimiter" ); // NOI18N
            properties[PROPERTY_outputSegmentDelimiter].setDisplayName ( "Output Segment Delimiter" );
            properties[PROPERTY_outputSegmentDelimiter].setShortDescription ( "For inbound properties, this is not used. For outbound properties, this is the delimiter character(s) that will be used after each segment" );
            properties[PROPERTY_outputSegmentDelimiter].setBound ( true );
            properties[PROPERTY_rejectedACKCode] = new PropertyDescriptor ( "rejectedACKCode", com.mirth.connect.client.ui.beans.HL7Properties.class, "getRejectedACKCode", "setRejectedACKCode" ); // NOI18N
            properties[PROPERTY_rejectedACKCode].setDisplayName ( "Rejected ACK Code" );
            properties[PROPERTY_rejectedACKCode].setShortDescription ( "The ACK code to respond with when the message is filtered. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_rejectedACKCode].setBound ( true );
            properties[PROPERTY_rejectedACKMessage] = new PropertyDescriptor ( "rejectedACKMessage", com.mirth.connect.client.ui.beans.HL7Properties.class, "getRejectedACKMessage", "setRejectedACKMessage" ); // NOI18N
            properties[PROPERTY_rejectedACKMessage].setDisplayName ( "Rejected ACK Message" );
            properties[PROPERTY_rejectedACKMessage].setShortDescription ( "The ACK message to respond with when the message is filtered. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_rejectedACKMessage].setBound ( true );
            properties[PROPERTY_stripNamespaces] = new PropertyDescriptor ( "stripNamespaces", com.mirth.connect.client.ui.beans.HL7Properties.class, "isStripNamespaces", "setStripNamespaces" ); // NOI18N
            properties[PROPERTY_stripNamespaces].setDisplayName ( "Strip Namespaces" );
            properties[PROPERTY_stripNamespaces].setShortDescription ( "Strips namespace definitions from the transformed XML message (applies to Strict Parser only" );
            properties[PROPERTY_stripNamespaces].setBound ( true );
            properties[PROPERTY_successfulACKCode] = new PropertyDescriptor ( "successfulACKCode", com.mirth.connect.client.ui.beans.HL7Properties.class, "getSuccessfulACKCode", "setSuccessfulACKCode" ); // NOI18N
            properties[PROPERTY_successfulACKCode].setDisplayName ( "Successful ACK Code" );
            properties[PROPERTY_successfulACKCode].setShortDescription ( "The ACK code to respond with when the message processes successfully. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_successfulACKCode].setBound ( true );
            properties[PROPERTY_successfulACKMessage] = new PropertyDescriptor ( "successfulACKMessage", com.mirth.connect.client.ui.beans.HL7Properties.class, "getSuccessfulACKMessage", "setSuccessfulACKMessage" ); // NOI18N
            properties[PROPERTY_successfulACKMessage].setDisplayName ( "Successful ACK Message" );
            properties[PROPERTY_successfulACKMessage].setShortDescription ( "The ACK message to respond with when the message processes successfully. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings." );
            properties[PROPERTY_successfulACKMessage].setBound ( true );
            properties[PROPERTY_useStrictParser] = new PropertyDescriptor ( "useStrictParser", com.mirth.connect.client.ui.beans.HL7Properties.class, "isUseStrictParser", "setUseStrictParser" ); // NOI18N
            properties[PROPERTY_useStrictParser].setDisplayName ( "Use Strict Parser" );
            properties[PROPERTY_useStrictParser].setShortDescription ( "Parse messages based upon strict HL7 specifications." );
            properties[PROPERTY_useStrictParser].setBound ( true );
            properties[PROPERTY_useStrictValidation] = new PropertyDescriptor ( "useStrictValidation", com.mirth.connect.client.ui.beans.HL7Properties.class, "isUseStrictValidation", "setUseStrictValidation" ); // NOI18N
            properties[PROPERTY_useStrictValidation].setDisplayName ( "Validate in Strict Parser" );
            properties[PROPERTY_useStrictValidation].setShortDescription ( "Validate messages using HL7 specifications(applies to Strict Parser only)." );
            properties[PROPERTY_useStrictValidation].setBound ( true );
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
     *         properties of this bean. May return null if the
     *         information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     *         properties supported by this bean. May return null if the
     *         information should be obtained by automatic analysis.
     *         <p>
     *         If a property is indexed, then its entry in the result array will
     *         belong to the IndexedPropertyDescriptor subclass of
     *         PropertyDescriptor. A client of getPropertyDescriptors can use
     *         "instanceof" to check if a given PropertyDescriptor is an
     *         IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return An array of EventSetDescriptors describing the kinds of
     *         events fired by this bean. May return null if the information
     *         should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return An array of MethodDescriptors describing the methods
     *         implemented by this bean. May return null if the information
     *         should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * 
     * @return Index of default property in the PropertyDescriptor array
     *         returned by getPropertyDescriptors.
     *         <P>
     *         Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * 
     * @return Index of default event in the EventSetDescriptor array
     *         returned by getEventSetDescriptors.
     *         <P>
     *         Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}
