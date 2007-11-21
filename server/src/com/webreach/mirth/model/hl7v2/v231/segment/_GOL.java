package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _GOL extends Segment {
	public _GOL(){
		fields = new Class[]{_ID.class, _TS.class, _CE.class, _EI.class, _EI.class, _NM.class, _TS.class, _TS.class, _CE.class, _CE.class, _CE.class, _TS.class, _TS.class, _TS.class, _TQ.class, _CE.class, _ST.class, _CE.class, _TS.class, _CE.class, _XPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Action Code", "Action Date/Time", "Goal ID", "Goal Instance ID", "Episode of Care ID", "Goal List Priority", "Goal Established Date/Time", "Expected Goal Achieve Date/Time", "Goal Classification", "Goal Management Discipline", "Current Goal Review Status", "Current Goal Review Date/Time", "Next Goal Review Date/Time", "Previous Goal Review Date/Time", "Goal Review Interval", "Goal Evaluation", "Goal Evaluation Comment", "Goal Life Cycle Status", "Goal Life Cycle Status Date/Time", "Goal Target Type", "Goal Target Name"};
		description = "Goal Detail";
		name = "GOL";
	}
}
