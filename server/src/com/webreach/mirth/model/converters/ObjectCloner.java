package com.webreach.mirth.model.converters;

import java.io.*;

import org.apache.log4j.Logger;

public class ObjectCloner
{
	// so that nobody can accidentally create an ObjectCloner object
	private ObjectCloner()
	{
	}

	// returns a deep copy of an object
	public static Object deepCopy(Object oldObj) throws ObjectClonerException
	{
		Logger logger = Logger.getLogger(ObjectCloner.class);
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);

			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();

			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);

			Object result = ois.readObject();
			
			oos.close();
			ois.close();
			
			// return the new object
			return result;
		}
		catch (Exception e)
		{
			throw new ObjectClonerException(e);
		}
	}

}
