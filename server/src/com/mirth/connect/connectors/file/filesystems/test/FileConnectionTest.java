/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MuleException;

import com.mirth.connect.connectors.file.filesystems.FileConnection;
import com.mirth.connect.connectors.file.filesystems.FileInfo;

public class FileConnectionTest
{
	private FileConnection fc;
	private File someFolder;


	@Test
	public void testListFiles() throws Exception
	{
		ArrayList<String> testFileNames = new ArrayList<String>();
		
		for(int i=0;i<10;i++)
		{
			File temp = File.createTempFile("ListFile", ".dat", someFolder);
			testFileNames.add(temp.getName());
		}
		
		List<FileInfo> retFiles = fc.listFiles(someFolder.getAbsolutePath(), "ListFile.+", true, true);
		
		
		for(int i=0;i<retFiles.size();i++)
		{
			assertTrue( testFileNames.contains(retFiles.get(i).getName()) );
		}
	}

	// NOTE: This only tests if the DIRECTORY is readable
	@Test
	public void testCanRead()
	{
		// Set it to be readable
		someFolder.setReadable(true);
		
		// Check to see if we can read a folder we know to be good
		assertTrue(fc.canRead(someFolder.getAbsolutePath()));
		
		// disable reading
		someFolder.setReadable(false,false);
		
		// Check the directory when we cant read
		// NOTE: This checks to see if the object returns the same as the Java IO
		// If we can still read this dir, then it will pass
		assertEquals(someFolder.canRead(), fc.canRead(someFolder.getAbsolutePath()));
	}

	@Test
	public void testCanWrite()
	{
		// Set it to be readable
		someFolder.setWritable(true);
		
		// Check to see if we can read a folder we know to be good
		assertTrue(fc.canWrite(someFolder.getAbsolutePath()));
		
		// disable reading
		someFolder.setWritable(false,false);
				
		// Check the directory when we cant read
		// NOTE: This checks to see if the object returns the same as the Java IO
		// If we can still read this dir, then it will pass
		assertEquals(someFolder.canWrite(), fc.canWrite(someFolder.getAbsolutePath()));
	}

	@Test
	public void testReadFile() throws IOException
	{
		// The string to write
		String testString = new String("This is just a test string");
		byte[] byteTest = testString.getBytes(Charset.defaultCharset());
		
		File testFile = new File(someFolder, "readFile"+System.currentTimeMillis()+".dat");
		
		// write the file
		FileUtils.writeByteArrayToFile(testFile, byteTest);
		
		InputStream in = null;
		try
		{
			in = fc.readFile(testFile.getName(), someFolder.getAbsolutePath());
		}
		catch (MuleException e)
		{
			fail("Threw a Mule exception");
		}
		
		// Read the file data
		byte[] tempRead = new byte[byteTest.length];
		in.read(tempRead);
		in.close();
		
		// check to make sure it is the same
		assertArrayEquals(byteTest, tempRead);
		
	}

	/*
	 * NOTHING TO TEST HERE
	@Test
	public void testCloseReadFile()
	{
		fail("Not yet implemented");
	}
	*/

	@Test
	public void testWriteFile()
	{
		String testString = new String("This is just a test string");
		byte[] byteTest = testString.getBytes(Charset.defaultCharset());
		
		
		File testFile = new File("writeFile"+System.currentTimeMillis()+".dat");
		
		try
		{
			fc.writeFile(testFile.getName(), someFolder.getAbsolutePath(), false, byteTest);
		}
		catch(Exception e)
		{
			fail("The FileConnection threw an exception, it should not have.");
		}
		
		File checkFile = new File(someFolder.getAbsolutePath()+File.separator+testFile.getName());
		
		// Now we load it back and check!
		try
		{
			byte[] verifyArr = FileUtils.readFileToByteArray(checkFile);
			assertArrayEquals(byteTest, verifyArr);
		}
		catch(Exception e)
		{
			fail("The JavaIO threw an exception ("+e.getClass().getName()+"): "+e.getMessage());
		}
		
		// Verify that appending works
		try
		{
			fc.writeFile(testFile.getName(), someFolder.getAbsolutePath(), true, byteTest);
		}
		catch(Exception e)
		{
			fail("The FileConnection threw an exception, it should not have (when appending).");
		}
		
		
		
		// Now we load it back and check!
		try
		{
			byte[] doubleCheck = new byte[byteTest.length*2];
			System.arraycopy(byteTest, 0, doubleCheck, 0, byteTest.length);
			System.arraycopy(byteTest, 0, doubleCheck, byteTest.length, byteTest.length);
			
			byte[] verifyArr = FileUtils.readFileToByteArray(checkFile);
			assertArrayEquals(doubleCheck, verifyArr);
		}
		catch(Exception e)
		{
			fail("The JavaIO threw an exception ("+e.getClass().getName()+"): "+e.getMessage());
		}
		
	}

	@Test(expected= MuleException.class)
	public void testDelete() throws MuleException
	{
		// Try to delete a file that doesn't exist, we should get an exception
		fc.delete("thisFileDoesNotExist.txt", System.getProperty("java.home"), false);
	}
	
	@Test
	public void testDeleteExists()
	{
		
		// Ok, now make some dummy file inside that folder
		File tempFile = null;
		
		try
		{
			// someone set us up a temp file!
			tempFile = File.createTempFile("mirthServerDE", ".dat", someFolder);
		}
		catch(Exception e)
		{
			fail("We could not make the file using regular java");
		}
		
		// delete the file, but throw exception if it doesn't exist
		try
		{
			fc.delete(tempFile.getName(), someFolder.getAbsolutePath(), false);
		}
		catch(MuleException e)
		{
			fail("An exception was thrown when deleting the file. It should not have been.");
		}
	}
	
	@Test
	public void testDeleteMayNotExist()
	{
		try
		{
			fc.delete("thisFileDoesNotExist.txt", System.getProperty("java.home"), true);
			assertTrue(true);
		}
		catch(MuleException e)
		{
			fail("An exception was thrown when there should not be one");
		}
	}

	@Test
	public void testMove()
	{
		File originalFile = new File(someFolder, "TestFile_"+System.currentTimeMillis()+"_.dat");
		File renamedFile = new File(someFolder, "TestRenamedFile_"+System.currentTimeMillis()+"_.dat");
		
		// Touch the file
		try
		{
			FileUtils.touch(originalFile);
		}
		catch(Exception e)
		{
			fail("We could not make the file using JavaIO");
		}
		
		try
		{
			fc.move(originalFile.getName(), someFolder.getAbsolutePath(), renamedFile.getName(), someFolder.getAbsolutePath());
		}
		catch(MuleException e)
		{
			fail("FileConnection.move threw a MuleException");
		}
		
		// The original file should be gone
		assertFalse(originalFile.exists());
		
		// the new file should exist
		assertTrue(renamedFile.exists());
	}
	
	//////////////////////////////////////////////////////////////
	
	
	
	@Before
	public void setUp() throws IOException
	{
		fc = new FileConnection();
		
		someFolder = new File("TEST_FileConnection");
		
		// Delete if exists
		if(someFolder.exists())
		{
			someFolder.setWritable(true,false);
			// clear it out if there is anything there
			FileUtils.deleteDirectory(someFolder);
		}
		
		if(!someFolder.mkdir())
		{
			throw new IOException("Could not create test folder");
		}	
	}
	
	@After
	public void tearDown() throws IOException
	{
		someFolder.setWritable(true,false);
		FileUtils.deleteDirectory(someFolder);
	}
	
	

}
