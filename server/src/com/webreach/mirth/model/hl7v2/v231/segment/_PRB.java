package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRB extends Segment {
	public _PRB(){
		fields = new Class[]{_ID.class, _TS.class, _CE.class, _EI.class, _EI.class, _NM.class, _TS.class, _TS.class, _TS.class, _CE.class, _CE.class, _CE.class, _CE.class, _CE.class, _TS.class, _TS.class, _ST.class, _CE.class, _CE.class, _NM.class, _CE.class, _CE.class, _CE.class, _ST.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Action Code", "Action Date/Time", "Problem ID", "Problem Instance ID", "Episode of Care ID", "Problem List Priority", "Problem Established Date/Time", "Anticipated Problem Resolution Date/Time", "Actual Problem Resolution Date/Time", "Problem Classification", "Problem Management Discipline", "Problem Persistence", "Problem Confirmation Status", "Problem Life Cycle Status", "Problem Life Cycle Status Date/Time", "Problem Date of Onset", "Problem Onset Text", "Problem Ranking", "Certainty of Problem", "Probability of Problem (0-1)", "Individual Awareness of Problem", "Problem Prognosis", "Individual Awareness of Prognosis", "Family/Significant Other Awareness of Problem/Prognosis", "Security/Sensitivity"};
		description = "Problem Detail";
		name = "PRB";
	}
}
