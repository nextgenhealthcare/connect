package com.webreach.mirth.client.ui.beans;

import java.io.Serializable;
import java.beans.PropertyChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Sep 18, 2007
 * Time: 9:53:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMProperties extends Object implements Serializable {


        public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";

        private PropertyChangeSupport propertySupport;

        public DICOMProperties()
        {
            propertySupport = new PropertyChangeSupport(this);
        }
        /**
         * Holds value of property includeGroupLength.
         */
        private boolean includeGroupLength = false;

        /**
         * Getter for property includeGroupLength.
         *
         * @return Value of property includeGroupLength.
         */
        public boolean isIncludeGroupLength()
        {
            return this.includeGroupLength;
        }
        /**
         * Setter for property includeGroupLength.
         *
         * @param includeGroupLength
         *            New value of property includeGroupLength.
         */
        public void setIncludeGroupLength(boolean includeGroupLength)
        {
            this.includeGroupLength = includeGroupLength;
        }
}
