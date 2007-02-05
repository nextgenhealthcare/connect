/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software 
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
    
	See the GNU Lesser General Public License for more details:    
	http://www.gnu.org/licenses/lgpl.txt
*/

package com.webreach.mirth.model.converters;

/**
 * Definition class for the X12N data model.
 * @author tfennelly
 */
public abstract class X12nModel {
    
    /**
     * Constructor.
     */
    private static X12nContainer[] containers = new X12nContainer[] {
        new X12nContainer("ISA", "IEA"),
        new X12nContainer("GS", "GE"),
        new X12nContainer("ST", "SE"),
    };
        
    public static boolean isContainerStartSegment(String segmentCode) {
        for (int i = 0; i < containers.length; i++) {
            if(containers[i].startSegmentCode.equalsIgnoreCase(segmentCode)) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isContainerEndSegment(String segmentCode) {
        for (int i = 0; i < containers.length; i++) {
            if(containers[i].endSegmentCode.equalsIgnoreCase(segmentCode)) {
                return true;
            }
        }
        
        return false;
    }

    public static String getStartSegmentCode(String endSegmentCode) {
        for (int i = 0; i < containers.length; i++) {
            if(containers[i].endSegmentCode.equalsIgnoreCase(endSegmentCode)) {
                return containers[i].startSegmentCode;
            }
        }

        return null;
    }

    private static class X12nContainer {
        private String startSegmentCode;
        private String endSegmentCode;

        public X12nContainer(String startSegmentCode, String endSegmentCode) {
            this.startSegmentCode = startSegmentCode;
            this.endSegmentCode = endSegmentCode;
        }
    }
}
