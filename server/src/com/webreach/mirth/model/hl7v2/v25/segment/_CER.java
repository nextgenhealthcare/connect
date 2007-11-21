package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CER extends Segment {
	public _CER(){
		fields = new Class[]{_SI.class, _ST.class, _ST.class, _XON.class, _XCN.class, _ED.class, _ID.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class, _ID.class, _ST.class, _CWE.class, _CWE.class, _CWE.class, _ID.class, _CWE.class, _ID.class, _CWE.class, _CWE.class, _CWE.class, _TS.class, _TS.class, _TS.class, _TS.class, _TS.class, _TS.class, _TS.class, _CE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Serial Number", "Version", "Granting Authority", "Issuing Authority", "Signature of Issuing Authority", "Granting Country", "Granting State/Province", "Granting County/Parish", "Certificate Type", "Certificate Domain", "Subject ID", "Subject Name", "Subject Directory Attribute Extension (Health Professional Data)", "Subject Public Key Info", "Authority Key Identifier", "Basic Constraint", "Crl Distribution Point", "Jurisdiction Country", "Jurisdiction State/Province", "Jurisdiction County/Parish", "Jurisdiction Breadth", "Granting Date", "Issuing Date", "Activation Date", "Inactivation Date", "Expiration Date", "Renewal Date", "Revocation Date", "Revocation Reason Code", "Certificate Status"};
		description = "Certificate Detail";
		name = "CER";
	}
}
