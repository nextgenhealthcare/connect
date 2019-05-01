package com.mirth.connect.connectors.file.filesystems;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mirth.connect.connectors.file.FTPSchemeProperties;
import com.mirth.connect.connectors.file.FileScheme;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;

public class FileSystemConnectionFactoryTest {

	@Test
	public void testFTPGetPoolKeyNoInitialCommands() {
		List<String> commands = null;
		FTPSchemeProperties ftpSchemeProps = new FTPSchemeProperties();
		ftpSchemeProps.setInitialCommands(commands);
		FileSystemConnectionOptions options = new FileSystemConnectionOptions(true, "username", "password", ftpSchemeProps);
		FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileScheme.FTP, options, "host", 9000, false, false, 0);
		
		String expected = "ftp://" + "username" + ":" + "password" + "@" + "host" + ":" + 9000;
		assertEquals(expected, factory.getPoolKey());
	}
	
	@Test
	public void testFTPGetPoolKeyNoSchemeProperties() {
		FTPSchemeProperties ftpSchemeProps = null;
		FileSystemConnectionOptions options = new FileSystemConnectionOptions(true, "username", "password", ftpSchemeProps);
		FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileScheme.FTP, options, "host", 9000, false, false, 0);
		
		String expected = "ftp://" + "username" + ":" + "password" + "@" + "host" + ":" + 9000;
		assertEquals(expected, factory.getPoolKey());
	}
	
	@Test
	public void testFTPGetPoolKeyWithInitialCommands() {
		List<String> commands = new ArrayList<>();
		commands.add("testcommand1");
		commands.add("testcommand2");
		FTPSchemeProperties ftpSchemeProps = new FTPSchemeProperties();
		ftpSchemeProps.setInitialCommands(commands);
		FileSystemConnectionOptions options = new FileSystemConnectionOptions(true, "username", "password", ftpSchemeProps);
		FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileScheme.FTP, options, "host", 9000, false, false, 0);
		
		String expected = "ftp://" + "username" + ":" + "password" + "@" + "host" + ":" + 9000 + ":" + commands.get(0) + ":" + commands.get(1);
		assertEquals(expected, factory.getPoolKey());
	}
	
	
}
