
package org.mule.transformers.simple;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

public class ObjectToString extends AbstractTransformer {
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
		} else {
			output = src.toString();
		}

		return output;
	}
}
