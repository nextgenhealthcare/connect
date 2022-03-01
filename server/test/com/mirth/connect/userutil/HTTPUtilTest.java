package com.mirth.connect.userutil;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.mirth.connect.server.userutil.HTTPUtil;

public class HTTPUtilTest {
	
	@Test
	public void testHttpBodyToXmlWithInputStream() throws Exception {
		InputStream inputStream = new ByteArrayInputStream(HTTP_BODY.getBytes());
		String xml = HTTPUtil.httpBodyToXml(inputStream, CONTENT_TYPE);
		assertTrue(xml.contains("multipart=\"no\""));
	}
	
	@Test
	public void testHttpBodyToXmlWithInputStreamMultipart() throws Exception {
		InputStream inputStream = new ByteArrayInputStream(MULTIPART_HTTP_BODY.getBytes());
		String xml = HTTPUtil.httpBodyToXml(inputStream, MULTIPART_CONTENT_TYPE);
		assertTrue(xml.contains("multipart=\"yes\""));
	}
	
	@Test
	public void testHttpBodyToXmlMultipartWithString() throws Exception {
		String xml = HTTPUtil.httpBodyToXml(HTTP_BODY, CONTENT_TYPE);
		assertTrue(xml.contains("multipart=\"no\""));
	}
	
	@Test
	public void testHttpBodyToXmlMultipartWithStringMultipart() throws Exception {
		String xml = HTTPUtil.httpBodyToXml(MULTIPART_HTTP_BODY, MULTIPART_CONTENT_TYPE);
		assertTrue(xml.contains("multipart=\"yes\""));
	}
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data;boundary=testBoundary;charset=UTF-8";
	private static final String HTTP_BODY = "test body";
	private static final String MULTIPART_HTTP_BODY = "--testBoundary\n" + 
			"Content-Disposition: form-data; name=\"part1\"\n" + 
			"\n" + 
			"test1\n" + 
			"--testBoundary\n" + 
			"Content-Disposition: form-data; name=\"part2\"\n" + 
			"\n" + 
			"test2\n" + 
			"--testBoundary--";
}
