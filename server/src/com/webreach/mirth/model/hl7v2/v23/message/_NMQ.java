package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMQ extends Message{	
	public _NMQ(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _NCK.class, _NST.class, _NSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, false, false, false};
		groups = new int[][]{{2, 3, 0, 0}, {4, 6, 1, 1}}; 
		description = "Network Management Query";
		name = "NMQ";
	}
}
