package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SPM extends Segment {
	public _SPM(){
		fields = new Class[]{_SI.class, _EIP.class, _EIP.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CQ.class, _NM.class, _ST.class, _CWE.class, _CWE.class, _DR.class, _TS.class, _TS.class, _ID.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _CQ.class, _NM.class, _CWE.class, _CWE.class, _CWE.class};
		repeats = new int[]{0, 0, -1, 0, -1, -1, 0, 0, -1, 0, -1, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Specimen ID", "Specimen Parent Ids", "Specimen Type", "Specimen Type Modifier", "Specimen Additives", "Specimen Collection Method", "Specimen Source Site", "Specimen Source Site Modifier", "Specimen Collection Site", "Specimen Role", "Specimen Collection Amount", "Grouped Specimen Count", "Specimen Description", "Specimen Handling Code", "Specimen Risk Code", "Specimen Collection Date/Time", "Specimen Received Date/Time", "Specimen Expiration Date/Time", "Specimen Availability", "Specimen Reject Reason", "Specimen Quality", "Specimen Appropriateness", "Specimen Condition", "Specimen Current Quantity", "Number of Specimen Containers", "Container Type", "Container Condition", "Specimen Child Role"};
		description = "Specimen";
		name = "SPM";
	}
}
