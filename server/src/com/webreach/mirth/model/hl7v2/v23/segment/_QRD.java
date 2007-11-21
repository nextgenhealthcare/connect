package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRD extends Segment {
	public _QRD(){
		fields = new Class[]{_TS.class, _ID.class, _ID.class, _ST.class, _ID.class, _TS.class, _CQ.class, _XCN.class, _CE.class, _CE.class, _CM.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Query Date/Time", "Query Format Code", "Query Priority", "Query ID", "Deferred Response Type", "Deferred Response Date/Time", "Quantity Limited Request", "Who Subject Filter", "What Subject Filter", "What Department Data Code", "What Data Code Value Qual.", "Query Results Level"};
		description = "Original Style Query Definition";
		name = "QRD";
	}
}
