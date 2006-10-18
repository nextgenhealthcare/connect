package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import sun.security.krb5.internal.util.o;

import com.ibm.wsdl.xml.WSDLReaderImpl;

public class WSDLDefinition {
	private String wsdlLocation;
	private List<WSOperation> operations;
	public List<WSOperation> getOperations(){
		return operations;
	}
	class LocReader extends WSDLReaderImpl{
		public WSDLLocator getLocator(){
			return this.loc;
		}
	}
	class WSOperation{
		private List<WSParameter> parameters;
		private String name;
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<WSParameter> getParameters() {
			return parameters;
		}

		public void setParameters(List<WSParameter> parameters) {
			this.parameters = parameters;
		}
		 
	}
	class WSParameter{
		private String name;
		private String type;
		private String value;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

	public WSDLDefinition(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
		this.operations = new ArrayList<WSOperation>();

		LocReader reader = new LocReader();
		try {
			Definition wsdlDefinition = reader.readWSDL(wsdlLocation);
			/*For complex type mappings, use the following
			List types = new ArrayList();
			try {
				Map map = new HashMap();
				Parser.getAllSchemaTypes(wsdlDefinition, types, reader.getLocator());
			} catch (WSIFException e) {
				
			}
			Map schemaTypes = typesToMap(types); //TODO: For future use
			*/
			Map bindings = wsdlDefinition.getBindings();
			Iterator<Binding> it = bindings.values().iterator();
			while (it.hasNext()) {
				Binding wsdlBinding = it.next();
				List<BindingOperation> ops = wsdlBinding.getBindingOperations();
				Iterator<BindingOperation> itOperations = ops.iterator();
				while (itOperations.hasNext()) {
					BindingOperation op = itOperations.next();
					WSOperation operation = new WSOperation();
					operation.setName(op.getName());
					List<WSParameter> parameters = new ArrayList<WSParameter>();
					//System.out.println("Operation: " + op.getName());
					Map parts = op.getOperation().getInput().getMessage()
							.getParts();
					Iterator<Part> paramIterator = parts.values().iterator();
					while (paramIterator.hasNext()) {
						Part opPart = paramIterator.next();
						QName typeName = opPart.getTypeName();
						String type = "";
						if (typeName != null)
							type = typeName.getLocalPart();
						WSParameter param = new WSParameter();
						param.setName(opPart.getName());
						param.setType(type);
						parameters.add(param);
						
					}
					operation.setParameters(parameters);
					operations.add(operation);
				}
			}
		} catch (WSDLException e) {
			e.printStackTrace();
		}

	}
	/*For future use with WSIF
	public Map typesToMap(List typeList){
		HashMap<String,SchemaType> map = new HashMap<String,SchemaType>();
		Iterator<SchemaType> it = typeList.iterator();
		while(it.hasNext()){
			SchemaType element = it.next();
			map.put(element.getTypeName().getLocalPart(), element);
		}
		return map;
		
	}
	*/
	//Definition wsdlDefinition = reader.readWSDL("http://winterspring.msoft.com:8080/MirthView/MirthViewServices?wsdl");
	//Definition wsdlDefinition = reader.readWSDL("http://www.xmethods.net/sd/2001/TemperatureService.wsdl");
	//**Definition wsdlDefinition = reader.readWSDL("http://ws.strikeiron.com/relauto/iplookup?WSDL");
	//Definition wsdlDefinition = reader.readWSDL("http://localhost:8081/services/EchoUMO?wsdl");
	//Definition wsdlDefinition = reader.readWSDL("http://www.spraci.com/services/soap/index.php?wsdl");
	
	public static void main(String[] args){
		WSDLDefinition definition = new WSDLDefinition("http://www.spraci.com/services/soap/index.php?wsdl");
		List<WSOperation> operations = definition.getOperations();
		Iterator<WSOperation> it = operations.iterator();
		while (it.hasNext()){
			WSOperation op = it.next();
			System.out.println("Operation: " + op.getName());
			List<WSParameter> params = op.getParameters();
			Iterator<WSParameter> itParam = params.iterator();
			while (itParam.hasNext()){
				WSParameter param = itParam.next();
				System.out.println("           Params: (" + param.getType() + ") "	+ param.getName());
			}
		}
	}
}
