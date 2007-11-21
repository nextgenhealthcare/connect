package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA19 extends Message{	
	public _ADTA19(){
		segments = new Class[]{_MSH.class, _QRD.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{true, true};
		groups = new int[][]{}; 
		description = "No Description.";
		name = "ADTA19";
	}
}
