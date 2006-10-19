package com.webreach.mirth.server.util;

import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.webreach.mirth.model.WSDefinition;
import com.webreach.mirth.model.WSOperation;
import com.webreach.mirth.model.WSParameter;

public class WebServiceReader {
	private String address;

	public WebServiceReader(String address) {
		this.address = address;
	}

	public WSDefinition getWSDefinition() throws Exception {
		WSDefinition wsDefinition = new WSDefinition();

		// for dealing with complex types, this needs to be extended to
		// implement a getLocation method
		WSDLReaderImpl reader = new WSDLReaderImpl();

		Definition definition = reader.readWSDL(address);
		Iterator bindingIter = definition.getBindings().values().iterator();

		while (bindingIter.hasNext()) {
			Binding binding = (Binding) bindingIter.next();
			Iterator bindingOperationIter = binding.getBindingOperations().iterator();

			while (bindingOperationIter.hasNext()) {
				BindingOperation bindingOperation = (BindingOperation) bindingOperationIter.next();

				WSOperation wsOperation = new WSOperation();
				wsOperation.setName(bindingOperation.getName());

				Iterator partIterator = bindingOperation.getOperation().getInput().getMessage().getParts().values().iterator();

				while (partIterator.hasNext()) {
					Part part = (Part) partIterator.next();
					QName typeName = part.getTypeName();
					String type = new String();

					if (typeName != null) {
						type = typeName.getLocalPart();
					}

					WSParameter wsParameter = new WSParameter();
					wsParameter.setName(part.getName());
					wsParameter.setType(type);

					wsOperation.getParameters().add(wsParameter);
				}

				wsDefinition.getOperations().add(wsOperation);
			}
		}

		return wsDefinition;
	}
}
