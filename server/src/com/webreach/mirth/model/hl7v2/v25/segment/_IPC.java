package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IPC extends Segment {
	public _IPC(){
		fields = new Class[]{_EI.class, _EI.class, _EI.class, _EI.class, _CE.class, _CE.class, _EI.class, _CE.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Accession Identifier", "Requested Procedure ID", "Study Instance Uid", "Scheduled Procedure Step ID", "Modality", "Protocol Code", "Scheduled Station Name", "Scheduled Procedure Step Location", "Scheduled Ae Title"};
		description = "Imaging Procedure Control Segment";
		name = "IPC";
	}
}
