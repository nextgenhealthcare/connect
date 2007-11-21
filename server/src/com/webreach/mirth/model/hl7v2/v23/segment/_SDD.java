package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SDD extends Segment {
	public _SDD(){
		fields = new Class[]{_CX.class, _CX.class, _CX.class, _CX.class, _CX.class, _CX.class, _NM.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"DesignID", "DefinitionID", "StudyTypeID", "QuestionID", "CategoryID", "Parent", "Sequence", "RevisedBy"};
		description = "Study Design Segment";
		name = "SDD";
	}
}
