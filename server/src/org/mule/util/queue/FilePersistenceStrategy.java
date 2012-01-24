/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
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

import com.mirth.connect.model.QueuedMessage;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class FilePersistenceStrategy implements QueuePersistenceStrategy
{

    private static final Log logger = LogFactory.getLog(FilePersistenceStrategy.class);

    public static final String EXTENSION = ".msg";
    public static final String IDSEPARATOR = "__";

    private File store;

    private UUIDGenerator gen = UUIDGenerator.getInstance();

    public FilePersistenceStrategy()
    {
    }

    protected String getFileName(String objId)
    {
    	return FilePersistenceQueue.getInstance().generateId() + IDSEPARATOR + objId + EXTENSION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#store(java.lang.Object)
     */
    public Object store(String queue, Object obj) throws IOException
    {
    	String id = "";
    	
    	if (obj instanceof QueuedMessage) {
    	    id = ((QueuedMessage) obj).getMessageObject().getId();
	    } else if (obj instanceof UMOEvent) {
    	    id = ((UMOEvent) obj).getId();
    	} else {
    	    id = gen.generateRandomBasedUUID().toString();
    	}
        
    	File file = new File(store, queue + File.separator + getFileName(id));
        file.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(obj);
        oos.close();
        FilePersistenceQueue.getInstance().putInMessageMap(queue, id, file);
        return id;
    }
    
	public void removeQueue(String queue) throws IOException {
		if (store.exists()) {
			// NOTE: could not use FileUtils here because the listFiles
			// method
			// does not return directories
			FilePersistenceQueue.getInstance().clearQueueMessageMap(queue);
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
    	File file = FilePersistenceQueue.getInstance().peekFromMessageMap(queue, id.toString());
    	if ((file != null) && (file.exists())) {
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
    	File file = FilePersistenceQueue.getInstance().getFromMessageMap(queue, id.toString());
    	// If getFromMessageMap returned null, the file does not exist yet
    	if (file == null) {
    		file = new File(store, queue + File.separator + id + EXTENSION);
    	}
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            return obj;
         } catch (Exception e) {
         	logger.error("Error reading broken queue file. If it exists, it should be manually removed: " + file.getAbsolutePath(), e);
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
    public List<Holder> restore() throws IOException
    {
        try {
            List<Holder>  msgs = new ArrayList<Holder>();
        	FilePersistenceQueue.getInstance().clearQueueMap();
            restoreFiles(store, msgs);
            logger.debug("Restore retrieved " + msgs.size() + " objects");
            return msgs;
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException("Could not restore").initCause(e);
        }
    }

    protected void restoreFiles(File dir, List<Holder> msgs) throws IOException, ClassNotFoundException
    {
        File[] files = dirListByAscendingName(dir); 
        int extensionLength = EXTENSION.length();
        int idSeparatorLength = IDSEPARATOR.length();
        String queue = dir.getName();
        String id = "";
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                restoreFiles(files[i], msgs);
            } else if (files[i].getName().endsWith(EXTENSION)) {
            	try {
            		String fileName = files[i].getName();
            		int idSeparatorIndex = fileName.lastIndexOf(IDSEPARATOR);
            		if (idSeparatorIndex < 0){
            			idSeparatorIndex = 0 ;
            			logger.warn("File " + files[i].getAbsolutePath() + " doesn't have a sequence number. This is only normal if you've just updated Mirth Connect.");
            		} else {
            			idSeparatorIndex += idSeparatorLength;
            		}
            		if (idSeparatorIndex > 0) {
            		    FilePersistenceQueue.getInstance().updateQueueId(fileName.substring(0,idSeparatorIndex - IDSEPARATOR.length()));
            		}
            		id = fileName.substring(idSeparatorIndex, fileName.length() - extensionLength);
                    FilePersistenceQueue.getInstance().putInMessageMap(queue, id, files[i]);
	                msgs.add(new HolderImpl(queue, id));
            	} catch (Exception e) {
            		logger.warn("Error loading queues. The queued message " + files[i].getCanonicalPath() + " could not load.");
            	}
            }
        }
    }

    public static File[] dirListByAscendingName(File folder) {

        File files[] = folder.listFiles();
        if (files == null) {
        	return null;
        }
        Arrays.sort(files, new Comparator<File>()
        {
            public int compare(final File f1, final File f2) {
                try {
                    String n1 = f1.getName();
                    String n2 = f2.getName();
                    Long l1 = Long.parseLong(n1.substring(0, n1.indexOf(IDSEPARATOR)));
                    Long l2 = Long.parseLong(n2.substring(0, n2.indexOf(IDSEPARATOR)));
                    return new Long(l1).compareTo(l2);
                } catch (Exception e) {
                    return new Long(f1.lastModified()).compareTo(new Long(f2.lastModified()));
                }
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
