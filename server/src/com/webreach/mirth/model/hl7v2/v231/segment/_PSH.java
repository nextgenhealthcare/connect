package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PSH extends Segment {
	public _PSH(){
		fields = new Class[]{_ST.class, _ST.class, _TS.class, _TS.class, _TS.class, _CQ.class, _CQ.class, _ID.class, _FT.class, _CQ.class, _ID.class, _FT.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Report Type", "Report Form Identifier", "Report Date", "Report Interval Start Date", "Report Interval End Date", "Quantity Manufactured", "Quantity Distributed", "Quantity Distributed Method", "Quantity Distributed Comment", "Quantity In Use", "Quantity In Use Method", "Quantity In Use Comment", "Number of Product Experience Reports Filed by Facility", "Number of Product Experience Reports Filed by Distributor"};
		description = "Product Summary Header";
		name = "PSH";
	}
}
