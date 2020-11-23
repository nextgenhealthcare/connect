package com.mirth.connect.server.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.mirth.connect.server.util.OAuthCredentials.OAuthToken;


public class OAuthCredentialsTest {

	@Test
	public void testOAuthTokenConstruction() throws Exception {
		String jsonResponse = "{\"access_token\":\"test_access_token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}";
		
		OAuthToken oAuthToken = new OAuthToken(jsonResponse);
		assertEquals("test_access_token", oAuthToken.getAccessToken());
		assertEquals("Bearer", oAuthToken.getTokenType());
		assertEquals(3600, oAuthToken.getExpiresIn());
	}
	
	@Test
	public void testTokenExpiration() throws Exception {
		OAuthCredentials oAuthCredentials = spy(new OAuthCredentials("https://testurl", "testclient", "testsecret"));
		doReturn(new OAuthToken("testaccesstoken", "Bearer", 3600)).when(oAuthCredentials).fetchBearerToken();
		
		oAuthCredentials.getOAuthToken();
		oAuthCredentials.setTokenExpired(true);
		oAuthCredentials.getOAuthToken();
		
		verify(oAuthCredentials, times(2)).fetchBearerToken();
	}
	
	@Test
	public void testTokenNotExpired() throws Exception {
		OAuthCredentials oAuthCredentials = spy(new OAuthCredentials("https://testurl", "testclient", "testsecret"));
		doReturn(new OAuthToken("testaccesstoken", "Bearer", 3600)).when(oAuthCredentials).fetchBearerToken();
		
		oAuthCredentials.getOAuthToken();
		oAuthCredentials.getOAuthToken();
		
		verify(oAuthCredentials, times(1)).fetchBearerToken();
	}
}
