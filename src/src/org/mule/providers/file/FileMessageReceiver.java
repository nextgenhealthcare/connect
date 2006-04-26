/*
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileMessageReceiver.java,v 1.12 2005/11/12 20:55:57 lajos Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/12 20:55:57 $
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

package org.mule.providers.file;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.Utility;
import java.util.Vector;
import java.io.File;
import java.io.FilenameFilter;
import java.io.*;
/**
 * <code>FileMessageReceiver</code> is a polling listener that reads files
 * from a directory.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */

public class FileMessageReceiver extends PollingMessageReceiver
{
    private String readDir = null;

    private String moveDir = null;

    private File readDirectory = null;

    private File moveDirectory = null;

    private String moveToPattern = null;

    private FilenameFilter filenameFilter = null;

    public FileMessageReceiver(UMOConnector connector,
                               UMOComponent component,
                               UMOEndpoint endpoint,
                               String readDir,
                               String moveDir,
                               String moveToPattern,
                               Long frequency) throws InitialisationException
    {
        super(connector, component, endpoint, frequency);
        this.readDir = readDir;
        this.moveDir = moveDir;
        this.moveToPattern = moveToPattern;
        if (endpoint.getFilter() instanceof FilenameFilter) {
            filenameFilter = (FilenameFilter) endpoint.getFilter();
        }
    }

    public void doConnect() throws Exception
    {
        if (readDir != null) {
            readDirectory = Utility.openDirectory(readDir);
            if (!(readDirectory.canRead())) {
                throw new ConnectException(new Message(Messages.FILE_X_DOES_NOT_EXIST, readDirectory.getAbsolutePath()),
                                           this);
            } else {
                logger.debug("Listening on endpointUri: " + readDirectory.getAbsolutePath());
            }
        }
        if (moveDir != null) {
            moveDirectory = Utility.openDirectory((moveDir));
            if (!(moveDirectory.canRead()) || !moveDirectory.canWrite()) {
                throw new ConnectException(new Message("file", 5), this);
            }
        }
    }

    public void doDisconnect() throws Exception
    {
    }

    public void poll()
    {
        try {
            File[] files = listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                processFile(files[i]);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized void processFile(File file) throws UMOException
    {
	boolean checkFileAge = ((FileConnector) connector).getCheckFileAge();
	if (checkFileAge) {
	    long fileAge = ((FileConnector) connector).getFileAge();
	    long lastMod = file.lastModified();
	    long now = (new java.util.Date()).getTime();
	    if ((now - lastMod) < fileAge) return;
	}

        File destinationFile = null;
        String orginalFilename = file.getName();
        UMOMessageAdapter msgAdapter = connector.getMessageAdapter(file);
        msgAdapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, orginalFilename);
        if (moveDir != null) {
            String fileName = file.getName();
            if (moveToPattern != null) {
                fileName = ((FileConnector) connector).getFilenameParser().getFilename(msgAdapter, moveToPattern);
            }
            destinationFile = new File(moveDir, fileName);
        }
        boolean resultOfFileMoveOperation = false;
        try {
            // Perform some quick checks to make sure file can be processed
            if (!(file.canRead() && file.exists() && file.isFile())) {
                throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getName()));
            } else {
                //Read in the file, parse it line by line 
        		Vector fileContents = getFileLines(file);
        		for (int i = 0; i < fileContents.size(); i++){
        			msgAdapter = connector.getMessageAdapter(fileContents.elementAt(i));   
                    UMOMessage message = new MuleMessage(msgAdapter);
                    routeMessage(message, endpoint.isSynchronous());
        		}
        		//move the file if needed
                if (destinationFile != null) {
                    resultOfFileMoveOperation = file.renameTo(destinationFile);
                    if (!resultOfFileMoveOperation) {
                        throw new MuleException(new Message("file",
                                                            4,
                                                            file.getAbsolutePath(),
                                                            destinationFile.getAbsolutePath()));
                    }
                    
                }

                if (((FileConnector) connector).isAutoDelete()) {
                    msgAdapter.getPayloadAsBytes();

                    // no moveTo directory
                    if (destinationFile == null) {
                        resultOfFileMoveOperation = file.delete();
                        if (!resultOfFileMoveOperation) {
                            throw new MuleException(new Message("file", 3, file.getAbsolutePath()));
                        }
                    }
                }

            }
        } catch (Exception e) {
            boolean resultOfRollbackFileMove = false;
            if (resultOfFileMoveOperation) {
                resultOfRollbackFileMove = rollbackFileMove(destinationFile, file.getAbsolutePath());
            }
            Exception ex = new MuleException(new Message("file", 2, file.getName(), (resultOfRollbackFileMove
                    ? "successful" : "unsuccessful")), e);
            handleException(ex);
        }
    }
    public Vector getFileLines(File aFile) {
        //...checks on aFile are elided
        Vector contents = new Vector();

        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
          //use buffering
          //this implementation reads one line at a time
          //FileReader always assumes default encoding is OK!
          input = new BufferedReader( new FileReader(aFile) );
          String line = null; //not declared within while loop
          while (( line = input.readLine()) != null){
            contents.add(line);
          }
        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        finally {
          try {
            if (input!= null) {
              //flush and close both "input" and its underlying FileReader
              input.close();
            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        return contents;
      }
    /**
     * Exception tolerant roll back method
     */
    private boolean rollbackFileMove(File sourceFile, String destinationFilePath)
    {
        boolean result = false;
        try {
            result = sourceFile.renameTo(new File(destinationFilePath));
        } catch (Throwable t) {
            logger.debug("rollback of file move failed: " + t.getMessage());
        }
        return result;
    }

    /**
     * Get a list of files to be processed.
     * 
     * @return a list of files to be processed.
     * @throws org.mule.MuleException which will wrap any other exceptions or
     *             errors.
     */
    File[] listFiles() throws MuleException
    {
        File[] todoFiles = new File[0];
        try {
            todoFiles = readDirectory.listFiles(filenameFilter);
        } catch (Exception e) {
            throw new MuleException(new Message("file", 1), e);
        }
        return todoFiles;
    }

}
