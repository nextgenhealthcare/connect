package com.mirth.connect.connectors.file.filesystems;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mirth.connect.connectors.file.FTPSchemeProperties;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

public class FtpConnectionTest {

	List<String> recievedCommands;
	
	@Before
	public void before() {
		recievedCommands =  new ArrayList<>();
	}

	@Test
	public void testInitializeSendsInitialCommands() throws Exception {
		List<String> commands = new ArrayList<>();
		commands.add("TEST1");
		commands.add("TEST2");
		
		
		FTPClient client = mock(FTPClient.class);
		Answer<Integer> answer = new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock arg0) throws Throwable {
				recievedCommands.add(arg0.getArgument(0));
				return 200;
			}
		};
		when(client.sendCommand(anyString())).thenAnswer(answer);
		when(client.getReplyCode()).thenReturn(200);
		when(client.login(null, null)).thenReturn(true);
		when(client.login(anyString(), anyString())).thenReturn(true);
		when(client.setFileType(anyInt())).thenReturn(true);
		
		FileSystemConnectionOptions fileSystemOptions = mock(FileSystemConnectionOptions.class);
		FTPSchemeProperties ftpSchemeProps = new FTPSchemeProperties();
		ftpSchemeProps.setInitialCommands(commands);
		when(fileSystemOptions.getSchemeProperties()).thenReturn(ftpSchemeProps);
		new FtpConnection("host", 9000, fileSystemOptions, true, 0, client);
		
		assertEquals(commands.size(), recievedCommands.size());
		assertArrayEquals(commands.toArray(), recievedCommands.toArray());
	}
	@Test
	public void testInitializeSendsZeroCommands() throws Exception {
		FTPClient client = mock(FTPClient.class);
		
		Answer<Integer> answer = new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock arg0) throws Throwable {
				recievedCommands.add(arg0.getArgument(0));
				return 200;
			}
		};
		when(client.sendCommand(anyString())).thenAnswer(answer);
		when(client.getReplyCode()).thenReturn(200);
		when(client.login(null, null)).thenReturn(true);
		when(client.login(anyString(), anyString())).thenReturn(true);
		when(client.setFileType(anyInt())).thenReturn(true);
		
		FileSystemConnectionOptions fileSystemOptions = mock(FileSystemConnectionOptions.class);
		FTPSchemeProperties ftpSchemeProps = new FTPSchemeProperties();
		ftpSchemeProps.setInitialCommands(null);
		when(fileSystemOptions.getSchemeProperties()).thenReturn(ftpSchemeProps);
		new FtpConnection("host", 9000, fileSystemOptions, true, 0, client);
		
		assertEquals(0, recievedCommands.size());
	}
}

