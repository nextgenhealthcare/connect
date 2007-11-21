package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYPCE extends Message{	
	public _QRYPCE(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "PC/ Pathway (problem-oriented) Query";
		name = "QRYPCE";
	}
}
