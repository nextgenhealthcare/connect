/* 
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/util/queue/FilePersistenceStrategy.java,v 1.2 2005/06/03 01:20:30 gnt Exp $
 * $Revision: 1.2 $
 * $Date: 2005/06/03 01:20:30 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package org.mule.util.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.doomdark.uuid.UUIDGenerator;
import org.mule.MuleManager;
import org.mule.config.MuleConfiguration;
import org.mule.umo.UMOEvent;
import org.mule.util.file.DeleteException;

import com.webreach.mirth.model.QueuedMessage;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class FilePersistenceStrategy implements QueuePersistenceStrategy
{

    private static final Log logger = LogFactory.getLog(FilePersistenceStrategy.class);

    public static final String EXTENSION = ".msg";

    private File store;

    private UUIDGenerator gen = UUIDGenerator.getInstance();

    public FilePersistenceStrategy()
    {
    }

    protected String getId(Object obj)
    {
        String id = gen.generateRandomBasedUUID().toString();
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#store(java.lang.Object)
     */
    public Object store(String queue, Object obj) throws IOException
    {
    	String id = null;
    	
    	if (obj instanceof QueuedMessage) {
    	    id = ((QueuedMessage) obj).getMessageObject().getId();
	    } else if (obj instanceof UMOEvent) {
    	    id = ((UMOEvent) obj).getId();
    	} else {
    	    id = getId(obj);
    	}

        
        File file = new File(store, queue + File.separator + id + EXTENSION);
        file.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(obj);
        oos.close();
        return id;
    }
    
	public void removeQueue(String queue) throws IOException {
		if (store.exists()) {
			// NOTE: could not use FileUtils here because the listFiles
			// method
			// does not return directories
			File file = new File(store, queue);
			FileUtils.forceDelete(file);
		}
	}
	    
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#remove(java.lang.Object)
     */
    public void remove(String queue, Object id) throws IOException
    {
        File file = new File(store, queue + File.separator + id + EXTENSION);
        if (file.exists()) {
            if (!file.delete()) {
                throw new DeleteException(file);
            }
        } else {
            throw new FileNotFoundException(file.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#load(java.lang.Object)
     */
    public Object load(String queue, Object id) throws IOException
    {
        File file = new File(store, queue + File.separator + id + EXTENSION);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            return obj;
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException("Error loading persistent object").initCause(e);
        } finally {
            if (ois != null) {
                ois.close();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#restore()
     */
    public List restore() throws IOException
    {
        try {
            List msgs = new ArrayList();
            restoreFiles(store, msgs);
            logger.debug("Restore retrieved " + msgs.size() + " objects");
            return msgs;
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException("Could not restore").initCause(e);
        }
    }

    protected void restoreFiles(File dir, List msgs) throws IOException, ClassNotFoundException
    {
        File[] files = dirListByAscendingDate(dir); 
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                restoreFiles(files[i], msgs);
            } else if (files[i].getName().endsWith(EXTENSION)) {
                String id = files[i].getCanonicalPath();
                id = id.substring(store.getCanonicalPath().length() + 1, id.length() - EXTENSION.length());
                String queue = id.substring(0, id.indexOf(File.separator));
                id = id.substring(queue.length() + 1);
                msgs.add(new HolderImpl(queue, id));
            }
        }
    }
    
    private File[] dirListByAscendingDate(File folder) {
    	File files[] = folder.listFiles();
    	if (files == null) {
    		return null;
    	}
    	Arrays.sort(files, new Comparator<File>() {
    		public int compare(final File o1, final File o2) {
    			return new Long(o1.lastModified()).compareTo(new Long(o2.lastModified()));
    		}
    	});
        return files;
      } 

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.util.queue.QueuePersistenceStrategy#open()
     */
    public void open() throws IOException
    {
        String path = MuleManager.getConfiguration().getWorkingDirectory() + File.separator
                + MuleConfiguration.DEFAULT_QUEUE_STORE;
        store = new File(path).getCanonicalFile();
        store.mkdirs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#close()
     */
    public void close() throws IOException
    {
        // Nothing to do
    }

    protected static class HolderImpl implements Holder
    {
        private String queue;
        private Object id;

        public HolderImpl(String queue, Object id)
        {
            this.queue = queue;
            this.id = id;
        }

        public Object getId()
        {
            return id;
        }

        public String getQueue()
        {
            return queue;
        }
    }

}
