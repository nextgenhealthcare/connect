package com.webreach.mirth.connectors.soap.transformers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>SOAPRequestToString</code> transformer returns string from incoming
 * SOAP data payload. Based on Mule ObjectToString
 * 
 * @author <a href="mailto:chrisl@webreachinc.com">Chris Lang</a>
 * @version $Revision: 1.0 $
 */
public class SOAPRequestToString extends AbstractTransformer {
	public Object doTransform(Object src) throws TransformerException {
		String output = "";
		if (src instanceof Map) {
			Map map = (Map) src;
			Iterator iter = map.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				Object value = map.get(key);
				output += key.toString() + ":" + value.toString() + "|";
			}
		} else if (src instanceof Collection) {
			Collection coll = (Collection) src;
			Object[] objs = coll.toArray();

			for (int i = 0; i < objs.length; i++) {
				output += objs[i].toString() + "|";
			}

		} else if (src instanceof String) {
			output = (String) src;
		} else if (src instanceof Object[]) {
			output = (String) (((Object[]) src)[0]);
		} else {
			output = src.toString();
		}

		return output;
	}
}
