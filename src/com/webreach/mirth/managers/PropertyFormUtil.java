/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.managers;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.MirthUtil;
import com.webreach.mirth.managers.types.MirthProperty;

/**
 * PropertyFormUtil generates the JSP/HTML forms for a specified property type.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class PropertyFormUtil {
	private PropertyFormUtil() {};

	private static final String TYPE_PROPERTY_DELIMETER = "_";
	protected static transient Log logger = LogFactory.getLog(PropertyFormUtil.class);
	private static PropertyManager propertyManager = PropertyManager.getInstance();

	/**
	 * Generates the JSP/JSTL/HTML form for a specified component and type.
	 * 
	 * @param component
	 *            the component (endpoint, filter, etc.)
	 * @param type
	 *            the type (tcp, http, etc.)
	 * @return the form string.
	 */
	public static String getForm(String component, String type, boolean defaultType) {
		try {
//			logger.debug("generating form: " + component + "/" + type + " (default=" + defaultType + ")");

			ArrayList<MirthProperty> propertyList = propertyManager.getProperties(component, type);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element div = document.createElement("div");

			// generate variables
			Element paramIf = document.createElement("c:if");
			paramIf.setAttribute("test", "${!empty param.flag}");
			Element paramChoose = document.createElement("c:choose");

			// set the fields to the previously entered values
			Element paramWhen = document.createElement("c:when");
			paramWhen.setAttribute("test", "${param.flag == 'set'}");

			for (Iterator<MirthProperty> iter = propertyList.iterator(); iter.hasNext();) {
				MirthProperty mirthProperty = iter.next();
				Element set1 = document.createElement("c:set");
				set1.setAttribute("var", generateFormElementId(mirthProperty));
				set1.setAttribute("value", "${param." + generateFormElementId(mirthProperty) + "}");
				paramWhen.appendChild(set1);
			}

			paramChoose.appendChild(paramWhen);

			// set the fields to the default values
			Element paramOtherwise = document.createElement("c:otherwise");

			for (Iterator<MirthProperty> iter = propertyList.iterator(); iter.hasNext();) {
				MirthProperty mirthProperty = iter.next();
				Element set1 = document.createElement("c:set");
				set1.setAttribute("var", generateFormElementId(mirthProperty));
				set1.setAttribute("value", mirthProperty.getDefaultValue());
				paramOtherwise.appendChild(set1);
			}

			paramChoose.appendChild(paramOtherwise);
			paramIf.appendChild(paramChoose);
			div.appendChild(paramIf);

			Element set1 = document.createElement("c:set");
			set1.setAttribute("var", "visible");
			set1.setAttribute("value", "");
			div.appendChild(set1);

			Element if1 = document.createElement("c:if");

			if (defaultType) {
				if1.setAttribute("test", "${'" + type + "' != endpointType and !empty endpoint}");
			} else {
				if1.setAttribute("test", "${'" + type + "' != endpointType}");
			}

			Element set2 = document.createElement("c:set");
			set2.setAttribute("var", "visible");
			set2.setAttribute("value", "hidden");
			if1.appendChild(set2);
			div.appendChild(if1);

			// create fieldset
			Element fieldset = document.createElement("fieldset");
			fieldset.setAttribute("id", type + "_options");
			fieldset.setAttribute("class", "${visible}");

			// generate description divs
			Element legend = document.createElement("legend");
			legend.setTextContent(propertyManager.getTypeDisplayName(type));
			fieldset.appendChild(legend);

			for (Iterator<MirthProperty> iter = propertyList.iterator(); iter.hasNext();) {
				MirthProperty mirthProperty = iter.next();
				generateDescriptions(mirthProperty, document, fieldset);
			}

			// generate form
			Element table = document.createElement("table");

			for (Iterator<MirthProperty> iter = propertyList.iterator(); iter.hasNext();) {
				MirthProperty mirthProperty = iter.next();
				generateForm(mirthProperty, document, table);
			}

			fieldset.appendChild(table);
			div.appendChild(fieldset);
			document.appendChild(div);

			return MirthUtil.serializeDocument(document, true);
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	private static void generateDescriptions(MirthProperty mirthProperty, Document document, Element fieldset) {
//		logger.debug("generating descriptions for: " + mirthProperty.getName());

		Element div = document.createElement("div");
		div.setAttribute("class", "help hidden");
		div.setAttribute("id", generateFormElementId(mirthProperty) + "_help");

		Element h3 = document.createElement("h3");
		h3.setTextContent(mirthProperty.getDisplayName());

		Element p = document.createElement("p");
		p.setTextContent(mirthProperty.getDescription());

		div.appendChild(h3);
		div.appendChild(p);

		fieldset.appendChild(div);
	}

	// generate TYPE_PROPERTY string
	private static String generateFormElementId(MirthProperty mirthProperty) {
		return mirthProperty.getType().getName() + TYPE_PROPERTY_DELIMETER + mirthProperty.getName();
	}

	private static void generateForm(MirthProperty mirthProperty, Document document, Element table) {
		try {
//			logger.debug("generating form for: " + mirthProperty.getName());

			Element tr = document.createElement("tr");

			// the property name column
			Element tdName = document.createElement("td");
			tdName.setAttribute("class", "fieldLabel");
			Element label = document.createElement("label");
			label.setAttribute("for", mirthProperty.getName());
			label.setTextContent(mirthProperty.getDisplayName());
			tdName.appendChild(label);
			tr.appendChild(tdName);

			// the property value column
			Element tdValue = document.createElement("td");
			tdValue.setAttribute("class", "fieldValue");

			if (mirthProperty.getFormType().equals(MirthProperty.TEXT)) {
				Element textInput = document.createElement("input");
				textInput.setAttribute("id", generateFormElementId(mirthProperty));
				textInput.setAttribute("type", "text");
				textInput.setAttribute("name", generateFormElementId(mirthProperty));
				textInput.setAttribute("size", mirthProperty.getFormOptions()[0]);

				// if the maxlength is set to 0, there is no maxlength
				// maxlength also can not be a negative number
				if (Integer.parseInt(mirthProperty.getFormOptions()[1]) > 0) {
					textInput.setAttribute("maxlength", mirthProperty.getFormOptions()[1]);
				}

				textInput.setAttribute("class", "hasHelp");
				textInput.setAttribute("value", "${" + generateFormElementId(mirthProperty) + "}");
				tdValue.appendChild(textInput);
			} else if (mirthProperty.getFormType().equals(MirthProperty.TEXTAREA)) {
				Element textarea = document.createElement("textarea");
				textarea.setAttribute("name", generateFormElementId(mirthProperty));
				textarea.setAttribute("id", generateFormElementId(mirthProperty));
				textarea.setAttribute("rows", mirthProperty.getFormOptions()[0]);
				textarea.setAttribute("cols", mirthProperty.getFormOptions()[1]);

				String wrap = "";
				switch (Integer.parseInt(mirthProperty.getFormOptions()[2])) {
					case 1:
						wrap = "virtual";
						break;
					case 2:
						wrap = "physical";
						break;
					default:
						wrap = "off";
						break;
				}

				textarea.setAttribute("wrap", wrap);
				textarea.setTextContent("${" + generateFormElementId(mirthProperty) + "}");
				tdValue.appendChild(textarea);
			} else if (mirthProperty.getFormType().equals(MirthProperty.YES_NO)) {
				// generate logic for selecting previously chosen respone

				Element choose1 = document.createElement("c:choose");

				Element when1 = document.createElement("c:when");
				when1.setAttribute("test", "${" + generateFormElementId(mirthProperty) + " == '1'}");

				Element label1 = document.createElement("label");
				Element input1 = document.createElement("input");
				input1.setAttribute("class", "hasHelp");
				input1.setAttribute("id", generateFormElementId(mirthProperty));
				input1.setAttribute("name", generateFormElementId(mirthProperty));
				input1.setAttribute("type", "radio");
				input1.setAttribute("value", "1");
				input1.setAttribute("checked", "");
				input1.setTextContent("Yes");
				label1.appendChild(input1);
				when1.appendChild(label1);

				Element label2 = document.createElement("label");
				Element input2 = document.createElement("input");
				input2.setAttribute("class", "hasHelp");
				input2.setAttribute("id", generateFormElementId(mirthProperty));
				input2.setAttribute("name", generateFormElementId(mirthProperty));
				input2.setAttribute("type", "radio");
				input2.setAttribute("value", "0");
				input2.setTextContent("No");
				label2.appendChild(input2);
				when1.appendChild(label2);

				choose1.appendChild(when1);

				Element otherwise1 = document.createElement("c:otherwise");

				Element label3 = document.createElement("label");
				Element input3 = document.createElement("input");
				input3.setAttribute("class", "hasHelp");
				input3.setAttribute("id", generateFormElementId(mirthProperty));
				input3.setAttribute("name", generateFormElementId(mirthProperty));
				input3.setAttribute("type", "radio");
				input3.setAttribute("value", "1");
				input3.setTextContent("Yes");
				label3.appendChild(input3);
				otherwise1.appendChild(label3);

				Element label4 = document.createElement("label");
				Element input4 = document.createElement("input");
				input4.setAttribute("class", "hasHelp");
				input4.setAttribute("id", generateFormElementId(mirthProperty));
				input4.setAttribute("name", generateFormElementId(mirthProperty));
				input4.setAttribute("type", "radio");
				input4.setAttribute("value", "0");
				input3.setAttribute("checked", "");
				input4.setTextContent("No");
				label4.appendChild(input4);
				otherwise1.appendChild(label4);

				choose1.appendChild(otherwise1);
				tdValue.appendChild(choose1);
			} else if (mirthProperty.getFormType().equals(MirthProperty.SELECT) || mirthProperty.getFormType().equals(MirthProperty.MULTI_SELECT)) {
				Element select = document.createElement("select");
				select.setAttribute("name", generateFormElementId(mirthProperty));
				select.setAttribute("id", generateFormElementId(mirthProperty));
				select.setAttribute("class", "hasHelp");

				// multi-select box
				if (mirthProperty.getFormType().equals(MirthProperty.MULTI_SELECT)) {
					select.setAttribute("multiple", "true");
				}

				String[] formOptions = mirthProperty.getFormOptions();

				int startIndex = 0;
				// if it is a multi-select box, the first option is the box
				// size, followed by the values
				if (mirthProperty.getFormType().equals(MirthProperty.MULTI_SELECT)) {
					select.setAttribute("size", formOptions[0].trim());
					startIndex = 1;
				}

				// create the drop down
				for (int i = startIndex; i < formOptions.length; i++) {
					String outerFormValue;

					// if the display name and form name are different
					if (formOptions[i].indexOf(PropertyManager.OPTIONS_SUB_DELIMITER) != -1) {
						String[] values = formOptions[i].trim().split(PropertyManager.OPTIONS_SUB_DELIMITER);
						outerFormValue = values[0].trim();
					} else {
						outerFormValue = formOptions[i].trim();
					}

					Element if1 = document.createElement("c:if");
					if1.setAttribute("test", "${'" + outerFormValue + "' == " + generateFormElementId(mirthProperty) + "}");

					for (int j = startIndex; j < formOptions.length; j++) {
						String innerDisplayValue, innerFormValue;

						// if the display name and form name are different
						if (formOptions[j].indexOf(PropertyManager.OPTIONS_SUB_DELIMITER) != -1) {
							String[] values = formOptions[j].trim().split(PropertyManager.OPTIONS_SUB_DELIMITER);
							innerFormValue = values[0].trim();
							innerDisplayValue = values[1].trim();
						} else {
							innerFormValue = formOptions[i].trim();
							innerDisplayValue = innerFormValue;
						}

						Element option = document.createElement("option");
						option.setAttribute("value", innerFormValue);
						option.setTextContent(innerDisplayValue);

						if (i == j) {
							option.setAttribute("selected", "");
						}

						if1.appendChild(option);
					}
					select.appendChild(if1);
				}

				Element endIf = document.createElement("c:if");
				endIf.setAttribute("test", "${empty " + generateFormElementId(mirthProperty) + "}");

				for (int k = startIndex; k < formOptions.length; k++) {
					String displayValue, formValue;

					// if the display name and form name are different
					if (formOptions[k].indexOf(PropertyManager.OPTIONS_SUB_DELIMITER) != -1) {
						String[] values = formOptions[k].trim().split(PropertyManager.OPTIONS_SUB_DELIMITER);
						formValue = values[0].trim();
						displayValue = values[1].trim();
					} else {
						formValue = formOptions[0].trim();
						displayValue = formValue;
					}

					Element option = document.createElement("option");
					option.setAttribute("value", formValue);
					option.setTextContent(displayValue);
					endIf.appendChild(option);
				}

				select.appendChild(endIf);
				tdValue.appendChild(select);
			} else if (mirthProperty.getFormType().equals(MirthProperty.HIDDEN)) {
				Element hiddenInput = document.createElement("input");
				hiddenInput.setAttribute("type", "hidden");
				hiddenInput.setAttribute("name", generateFormElementId(mirthProperty));
				hiddenInput.setAttribute("value", mirthProperty.getDefaultValue());
				tdValue.appendChild(hiddenInput);
			} else if (mirthProperty.getFormType().equals(MirthProperty.PASSWORD)) {
				Element passwordInput = document.createElement("input");
				passwordInput.setAttribute("id", generateFormElementId(mirthProperty));
				passwordInput.setAttribute("type", "password");
				passwordInput.setAttribute("name", generateFormElementId(mirthProperty));
				passwordInput.setAttribute("size", mirthProperty.getFormOptions()[0]);

				// if the maxlength is set to 0, there is no maxlength
				// maxlength also can not be a negative number
				if (Integer.parseInt(mirthProperty.getFormOptions()[1]) > 0) {
					passwordInput.setAttribute("maxlength", mirthProperty.getFormOptions()[1]);
				}

				passwordInput.setAttribute("value", "${" + generateFormElementId(mirthProperty) + "}");
				tdValue.appendChild(passwordInput);
			}

			tr.appendChild(tdValue);
			table.appendChild(tr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
