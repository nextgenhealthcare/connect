package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCI extends Segment {
	public _PCI(){
		fields = new Class[]{_XCN.class, _CX.class, _IS.class, _FT.class, _FT.class, _DT.class, _ST.class, _ST.class, _IS.class, _ST.class, _ST.class, _ST.class, _ST.class, _DT.class, _XPN.class, _ST.class, _IS.class, _IS.class, _ST.class, _IS.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Provider ID (internal)", "ID", "Classification ID", "SSN", "Tax ID", "Date of birth", "Is USA Citizen", "Alien Number", "Birth Country ID", "Birth City", "Birth State ID", "Gender", "Original State ID", "Original Year", "Maiden Name", "Comment", "Standing ID", "Practicing Specialties", "Languages", "Provider Type", "Line of Business", "Revised By"};
		description = "Provider Credentials Information Segment";
		name = "PCI";
	}
}
