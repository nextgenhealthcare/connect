package com.mirth.connect.server.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

public class WebStartServletTest {

	private static WebStartServlet webStartServlet;

	@BeforeClass
	public static void setupClass() {
		webStartServlet = new TestWebStartServlet();
	}

	@Test
	public void testDoGetCore() throws Exception {
		// Test /webstart
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart");
		when(request.getServletPath()).thenReturn("/webstart");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals(CORE_JNLP.trim(), response.getResponseString().trim());
		assertEquals("application/x-java-jnlp-file", response.getContentType());
		assertEquals("no-cache", response.getHeader("Pragma"));
		assertEquals("nosniff", response.getHeader("X-Content-Type-Options:"));
		assertEquals("attachment; filename = \"webstart.jnlp\"", response.getHeader("Content-Disposition"));

		// Test /webstart.jnlp
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart.jnlp");
		when(request.getServletPath()).thenReturn("/webstart.jnlp");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals(CORE_JNLP.trim(), response.getResponseString().trim());
		assertEquals("application/x-java-jnlp-file", response.getContentType());
		assertEquals("no-cache", response.getHeader("Pragma"));
		assertEquals("nosniff", response.getHeader("X-Content-Type-Options:"));
		assertEquals("attachment; filename = \"webstart.jnlp\"", response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetCoreQueryParams() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart");
		when(request.getServletPath()).thenReturn("/webstart");

		Map<String, String[]> parameters = new HashMap<>();
		parameters.put("maxHeapSize", new String[] { "1024m" });
		parameters.put("time", new String[] { "123456789" });

		when(request.getParameterNames()).thenReturn(Collections.enumeration(parameters.keySet()));
		when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return parameters.get((String) args[0])[0];
			}
		});

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals(CORE_JNLP.trim(), response.getResponseString().trim());
		assertEquals("application/x-java-jnlp-file", response.getContentType());
		assertEquals("attachment; filename = \"webstart.jnlp\"", response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetCoreQueryParamsInvalidValues() throws Exception {
		// Test "maxHeapSize" invalid value
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart");
		when(request.getServletPath()).thenReturn("/webstart");

		Map<String, String[]> parameters1 = new HashMap<>();
		parameters1.put("maxHeapSize", new String[] { "1024" });

		when(request.getParameterNames()).thenReturn(Collections.enumeration(parameters1.keySet()));
		when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return parameters1.get((String) args[0])[0];
			}
		});

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));

		// Test "time" invalid value
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart");
		when(request.getServletPath()).thenReturn("/webstart");

		Map<String, String[]> parameters2 = new HashMap<>();
		parameters2.put("time", new String[] { "12h" });

		when(request.getParameterNames()).thenReturn(Collections.enumeration(parameters2.keySet()));
		when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return parameters2.get((String) args[0])[0];
			}
		});

		response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetCoreInvalidQueryParams() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart");
		when(request.getServletPath()).thenReturn("/webstart");

		Map<String, String[]> parameters = new HashMap<>();
		parameters.put("maxHeapSize", new String[] { "1024m" });
		parameters.put("time", new String[] { "123456789" });
		parameters.put("invalidKey", new String[] { "value" });

		when(request.getParameterNames()).thenReturn(Collections.enumeration(parameters.keySet()));
		when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return parameters.get((String) args[0])[0];
			}
		});

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetCoreModifiedURL() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart;rfd.bat");
		when(request.getServletPath()).thenReturn("/webstart");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetExtension() throws Exception {
		// Test /webstart/extensions/testextension
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart/extensions/testextension");
		when(request.getServletPath()).thenReturn("/webstart/extensions");
		when(request.getPathInfo()).thenReturn("/testextension");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals(EXTENSION_JNLP.trim(), response.getResponseString().trim());
		assertEquals("application/x-java-jnlp-file", response.getContentType());
		assertEquals("no-cache", response.getHeader("Pragma"));
		assertEquals("nosniff", response.getHeader("X-Content-Type-Options:"));
		assertEquals("attachment; filename = \"testextension.jnlp\"", response.getHeader("Content-Disposition"));

		// Test /webstart/extensions/testextension.jnlp
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart/extensions/testextension.jnlp");
		when(request.getServletPath()).thenReturn("/webstart/extensions");
		when(request.getPathInfo()).thenReturn("/testextension");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals(EXTENSION_JNLP.trim(), response.getResponseString().trim());
		assertEquals("application/x-java-jnlp-file", response.getContentType());
		assertEquals("no-cache", response.getHeader("Pragma"));
		assertEquals("nosniff", response.getHeader("X-Content-Type-Options:"));
		assertEquals("attachment; filename = \"testextension.jnlp\"", response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetExtensionQueryParams() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart/extensions/testextension");
		when(request.getServletPath()).thenReturn("/webstart/extensions");
		when(request.getPathInfo()).thenReturn("/testextension");

		Map<String, String[]> parameters = new HashMap<>();
		parameters.put("maxHeapSize", new String[] { "1024m" });

		when(request.getParameterNames()).thenReturn(Collections.enumeration(parameters.keySet()));
		when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return parameters.get((String) args[0])[0];
			}
		});
		when(request.getParameterMap()).thenReturn(parameters);

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));
	}

	@Test
	public void testDoGetExtensionModifiedURL() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/webstart/extensions/testextension;rfd.bat");
		when(request.getServletPath()).thenReturn("/webstart/extensions");
		when(request.getPathInfo()).thenReturn("/testextension");
		when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

		TestHttpServletResponse response = new TestHttpServletResponse();

		webStartServlet.doGet(request, response);

		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getResponseString().trim());
		assertEquals("", response.getContentType());
		assertNull(response.getHeader("Content-Disposition"));
	}

	private static class TestHttpServletResponse implements HttpServletResponse {

		private String contentType;
		private StringWriter stringWriter;
		private PrintWriter printWriter;
		private Map<String, List<String>> headers;

		public TestHttpServletResponse() {
			contentType = "";
			stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
			headers = new HashMap<>();
		}

		public String getResponseString() {
			return stringWriter.toString();
		}

		@Override
		public void flushBuffer() throws IOException {

		}

		@Override
		public int getBufferSize() {
			return 0;
		}

		@Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return null;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			return printWriter;
		}

		@Override
		public boolean isCommitted() {
			return false;
		}

		@Override
		public void reset() {

		}

		@Override
		public void resetBuffer() {

		}

		@Override
		public void setBufferSize(int arg0) {

		}

		@Override
		public void setCharacterEncoding(String arg0) {

		}

		@Override
		public void setContentLength(int arg0) {

		}

		@Override
		public void setContentLengthLong(long arg0) {

		}

		@Override
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		@Override
		public void setLocale(Locale arg0) {

		}

		@Override
		public void addCookie(Cookie arg0) {

		}

		@Override
		public void addDateHeader(String arg0, long arg1) {

		}

		@Override
		public void addHeader(String arg0, String arg1) {

		}

		@Override
		public void addIntHeader(String arg0, int arg1) {

		}

		@Override
		public boolean containsHeader(String arg0) {
			return false;
		}

		@Override
		public String encodeRedirectURL(String arg0) {
			return null;
		}

		@Override
		public String encodeRedirectUrl(String arg0) {
			return null;
		}

		@Override
		public String encodeURL(String arg0) {
			return null;
		}

		@Override
		public String encodeUrl(String arg0) {
			return null;
		}

		@Override
		public String getHeader(String key) {
			List<String> values = headers.get(key);
			if (values != null && !values.isEmpty()) {
				return values.get(0);
			}
			return null;
		}

		@Override
		public Collection<String> getHeaderNames() {
			return headers.keySet();
		}

		@Override
		public Collection<String> getHeaders(String key) {
			return headers.get(key);
		}

		@Override
		public int getStatus() {
			return 0;
		}

		@Override
		public void sendError(int arg0) throws IOException {

		}

		@Override
		public void sendError(int arg0, String arg1) throws IOException {

		}

		@Override
		public void sendRedirect(String arg0) throws IOException {

		}

		@Override
		public void setDateHeader(String arg0, long arg1) {

		}

		@Override
		public void setHeader(String key, String value) {
			List<String> values = headers.get(key);
			if (values == null) {
				values = new ArrayList<>();
				headers.put(key, values);
			}
			values.add(value);
		}

		@Override
		public void setIntHeader(String arg0, int arg1) {

		}

		@Override
		public void setStatus(int arg0) {

		}

		@Override
		public void setStatus(int arg0, String arg1) {

		}

	}

	private static class TestWebStartServlet extends WebStartServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected PropertiesConfiguration getMirthProperties() throws FileNotFoundException, ConfigurationException {
		    PropertiesConfiguration mirthPropertiesConfiguration = new PropertiesConfiguration();
		    mirthPropertiesConfiguration.setProperty("http.contextpath", "/");
		    mirthPropertiesConfiguration.setProperty("server.url", "");
		    mirthPropertiesConfiguration.setProperty("https.port", 8443);
		    mirthPropertiesConfiguration.setProperty("administrator.maxheapsize", "512m");
		    return mirthPropertiesConfiguration;
		}

		@Override
		protected Document getAdministratorJnlp(HttpServletRequest request) throws Exception {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(CORE_JNLP.getBytes()));
		}

		@Override
		protected Document getExtensionJnlp(String extensionPath) throws Exception {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(EXTENSION_JNLP.getBytes()));
		}
	}

	private static String CORE_JNLP = "<jnlp codebase=\"https://localhost:8443\" version=\"3.11.0\">\n"
			+ "	<information>\n" + "		<title>Mirth Connect Administrator 3.11.0</title>\n"
			+ "		<vendor>NextGen Healthcare</vendor>\n" + "		<homepage href=\"http://www.nextgen.com\"/>\n"
			+ "		<description>Open Source Healthcare Integration Engine</description>\n" + "		\n"
			+ "		<icon href=\"images/mirth_128_ico.png\"/>\n"
			+ "		<icon href=\"images/splashscreen.png\" kind=\"splash\"/> \n" + "		\n"
			+ "		<offline-allowed/>\n" + "\n" + "		<shortcut online=\"true\">\n"
			+ "            <!-- put a shortcut on the desktop -->\n" + "            <desktop/>\n"
			+ "            <!-- put shortcut in start menu too -->\n"
			+ "            <menu submenu=\"Mirth Connect\"/>\n" + "    	</shortcut>\n" + "    	\n"
			+ "	</information>\n" + "	\n" + "	<security>\n" + "		<all-permissions/>\n" + "	</security>\n"
			+ "	\n" + "	<update check=\"timeout\" policy=\"always\"/>\n" + "	\n" + "	<resources>\n"
			+ "		<j2se href=\"http://java.sun.com/products/autodl/j2se\" java-vm-args=\"--add-modules=java.sql.rowset --add-exports=java.base/com.sun.crypto.provider=ALL-UNNAMED --add-exports=java.base/sun.security.provider=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.security.cert=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.security.pkcs=ALL-UNNAMED --add-opens=java.base/sun.security.rsa=ALL-UNNAMED --add-opens=java.base/sun.security.x509=ALL-UNNAMED --add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens=java.desktop/com.apple.eio=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED\" max-heap-size=\"512m\" version=\"1.9+\"/>\n"
			+ "		<j2se href=\"http://java.sun.com/products/autodl/j2se\" max-heap-size=\"512m\" version=\"1.6+\"/>\n"
			+ "	<jar download=\"eager\" href=\"webstart/client-lib/mirth-client.jar\" main=\"true\" sha256=\"0Lv3mOM4e10OBhk78/ST2CzHrXtm+EcZibxV7VfdbI8=\"/>\n"
			+ "        <jar download=\"eager\" href=\"webstart/client-lib/mirth-client-core.jar\" sha256=\"testsha256\"/>\n"
			+ "        <extension href=\"webstart/extensions/test.jnlp\"/>\n" + "    </resources>\n" + "	\n"
			+ "	<application-desc main-class=\"com.mirth.connect.client.ui.Mirth\">\n"
			+ "        <argument>https://localhost:8443</argument>\n" + "        <argument>3.11.0</argument>\n"
			+ "    </application-desc>\n" + "</jnlp>";

	private static String EXTENSION_JNLP = "<jnlp>\n" + "    <information>\n"
			+ "        <title>Mirth Connect Extension - [Test Writer,Test Reader]</title>\n"
			+ "        <vendor>NextGen Healthcare</vendor>\n" + "    </information>\n" + "    <security>\n"
			+ "        <all-permissions/>\n" + "    </security>\n" + "    <resources>\n"
			+ "        <jar download=\"eager\" href=\"libs/file/test-client.jar\" sha256=\"testsha256\"/>\n"
			+ "        <jar download=\"eager\" href=\"libs/file/test-shared.jar\" sha256=\"testsha256\"/>\n"
			+ "    </resources>\n" + "    <component-desc/>\n" + "</jnlp>\n" + "";
}
