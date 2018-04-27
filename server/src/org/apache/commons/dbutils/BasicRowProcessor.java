/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.dbutils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the <code>RowProcessor</code> interface.
 * 
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see RowProcessor
 */
public class BasicRowProcessor implements RowProcessor {

	/**
	 * The default BeanProcessor instance to use if not supplied in the
	 * constructor.
	 */
	private static final BeanProcessor defaultConvert = new BeanProcessor();

	/**
	 * The Singleton instance of this class.
	 */
	private static final BasicRowProcessor instance = new BasicRowProcessor();

	/**
	 * Returns the Singleton instance of this class.
	 * 
	 * @return The single instance of this class.
	 * @deprecated Create instances with the constructors instead. This will be
	 *             removed after DbUtils 1.1.
	 */
    @Deprecated
	public static BasicRowProcessor instance() {
		return instance;
	}

	/**
	 * Use this to process beans.
	 */
	private BeanProcessor convert = null;

	/**
	 * BasicRowProcessor constructor. Bean processing defaults to a
	 * BeanProcessor instance.
	 */
	public BasicRowProcessor() {
		this(defaultConvert);
	}

	/**
	 * BasicRowProcessor constructor.
	 * 
	 * @param convert
	 *            The BeanProcessor to use when converting columns to bean
	 *            properties.
	 * @since DbUtils 1.1
	 */
	public BasicRowProcessor(BeanProcessor convert) {
		super();
		this.convert = convert;
	}

	/**
	 * Convert a <code>ResultSet</code> row into an <code>Object[]</code>.
	 * This implementation copies column values into the array in the same order
	 * they're returned from the <code>ResultSet</code>. Array elements will
	 * be set to <code>null</code> if the column was SQL NULL.
	 * 
	 * @see org.apache.commons.dbutils.RowProcessor#toArray(java.sql.ResultSet)
	 */
	public Object[] toArray(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		Object[] result = new Object[cols];

		for (int i = 0; i < cols; i++) {
			result[i] = rs.getObject(i + 1);
		}

		return result;
	}

	/**
	 * Convert a <code>ResultSet</code> row into a JavaBean. This
	 * implementation delegates to a BeanProcessor instance.
	 * 
	 * @see org.apache.commons.dbutils.RowProcessor#toBean(java.sql.ResultSet,
	 *      java.lang.Class)
	 * @see org.apache.commons.dbutils.BeanProcessor#toBean(java.sql.ResultSet,
	 *      java.lang.Class)
	 */
	public Object toBean(ResultSet rs, Class type) throws SQLException {
		return this.convert.toBean(rs, type);
	}

	/**
	 * Convert a <code>ResultSet</code> into a <code>List</code> of
	 * JavaBeans. This implementation delegates to a BeanProcessor instance.
	 * 
	 * @see org.apache.commons.dbutils.RowProcessor#toBeanList(java.sql.ResultSet,
	 *      java.lang.Class)
	 * @see org.apache.commons.dbutils.BeanProcessor#toBeanList(java.sql.ResultSet,
	 *      java.lang.Class)
	 */
	public List toBeanList(ResultSet rs, Class type) throws SQLException {
		return this.convert.toBeanList(rs, type);
	}

	/**
	 * Convert a <code>ResultSet</code> row into a <code>Map</code>. This
	 * implementation returns a <code>Map</code> with case insensitive column
	 * names as keys. Calls to <code>map.get("COL")</code> and
	 * <code>map.get("col")</code> return the same value.
	 * 
	 * @see org.apache.commons.dbutils.RowProcessor#toMap(java.sql.ResultSet)
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map toMap(ResultSet resultSet) throws SQLException {
        Map resultMap = new CaseInsensitiveHashMap();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colCount = metaData.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            /*
             * Retrieve the alias for the column. If no alias was specified, this method should
             * return the actual column name.
             */
            String alias = metaData.getColumnLabel(i);

            if (resultMap.containsKey(alias)) {
                /*
                 * Currently, duplicate keys/aliases would get overwritten in the resultMap, so we
                 * throw an exception to keep the message from processing since it would contain
                 * incomplete data and so that the user can have a chance to modify the query and
                 * select those records again. In the future, we plan to allow duplicate field names
                 * (MIRTH-3138).
                 */
                throw new SQLException("Multiple columns have the alias '" + alias + "'. To prevent this error from occurring, specify unique aliases for each column.");
            }

            resultMap.put(alias, resultSet.getObject(i));
        }

        return resultMap;
    }

	/**
	 * A Map that converts all keys to lowercase Strings for case insensitive
	 * lookups. This is needed for the toMap() implementation because databases
	 * don't consistenly handle the casing of column names.
	 */
	public static class CaseInsensitiveHashMap extends HashMap {

		/**
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
		public boolean containsKey(Object key) {
			return super.containsKey(key.toString().toLowerCase());
		}

		/**
		 * @see java.util.Map#get(java.lang.Object)
		 */
		public Object get(Object key) {
			return super.get(key.toString().toLowerCase());
		}

		/**
		 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
		 */
		public Object put(Object key, Object value) {
			return super.put(key.toString().toLowerCase(), value);
		}

		/**
		 * @see java.util.Map#putAll(java.util.Map)
		 */
		public void putAll(Map m) {
			Iterator iter = m.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				Object value = m.get(key);
				this.put(key, value);
			}
		}

		/**
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		public Object remove(Object key) {
			return super.remove(key.toString().toLowerCase());
		}
	}

}
