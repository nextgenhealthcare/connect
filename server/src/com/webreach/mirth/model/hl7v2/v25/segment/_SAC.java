package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SAC extends Segment {
	public _SAC(){
		fields = new Class[]{_EI.class, _EI.class, _EI.class, _EI.class, _EI.class, _SPS.class, _TS.class, _CE.class, _CE.class, _EI.class, _NA.class, _CE.class, _EI.class, _NA.class, _CE.class, _NM.class, _NM.class, _NM.class, _NM.class, _CE.class, _NM.class, _NM.class, _NM.class, _CE.class, _CE.class, _CE.class, _CWE.class, _CE.class, _SN.class, _CE.class, _SN.class, _NM.class, _CE.class, _NM.class, _CE.class, _NM.class, _CE.class, _NM.class, _CE.class, _CE.class, _CE.class, _CE.class, _CWE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"External Accession Identifier", "Accession Identifier", "Container Identifier", "Primary (parent) Container Identifier", "Equipment Container Identifier", "Specimen Source", "Registration Date/Time", "Container Status", "Carrier Type", "Carrier Identifier", "Position In Carrier", "Tray Type", "Tray Identifier", "Position In Tray", "Location", "Container Height", "Container Diameter", "Barrier Delta", "Bottom Delta", "Container Height/Diameter/Delta Units", "Container Volume", "Available Specimen Volume", "Initial Specimen Volume", "Volume Units", "Separator Type", "Cap Type", "Additive", "Specimen Component", "Dilution Factor", "Treatment", "Temperature", "Hemolysis Index", "Hemolysis Index Units", "Lipemia Index", "Lipemia Index Units", "Icterus Index", "Icterus Index Units", "Fibrin Index", "Fibrin Index Units", "System Induced Contaminants", "Drug Interference", "Artificial Blood", "Special Handling Code", "Other Environmental Factors"};
		description = "Specimen Container Detail";
		name = "SAC";
	}
}
