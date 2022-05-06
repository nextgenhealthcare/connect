package com.mirth.connect.server.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.server.util.OAuthCredentials.OAuthToken;


public class OAuthCredentialsTest {
	
	private OAuthToken token1;
	private OAuthToken token2;
	
	@Before
	public void setup() {
		token1 = null;
		token2 = null;
	}

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
	
	@Test
	public void testGetOAuthTokenSynchronization() throws Exception {
		OAuthCredentials oAuthCredentials = spy(new TestOAuthCredentials("https://testurl", "testclient", "testsecret", 1000L));
		
		Thread thread1 = new Thread(() -> {
			token1 = oAuthCredentials.getOAuthToken();
		});
		
		Thread thread2 = new Thread(() -> {
			token2 = oAuthCredentials.getOAuthToken();
		});
		
		thread1.start();
		thread2.start();
		
		thread1.join();
		thread2.join();
		
		assertEquals(token1.getAccessToken(), token2.getAccessToken());
		verify(oAuthCredentials, times(1)).fetchBearerToken();
	}
	
	@Test
	public void testGetOAuthTokenSynchronizationWithExpiration() throws Exception {
		OAuthCredentials oAuthCredentials = spy(new TestOAuthCredentials("https://testurl", "testclient", "testsecret", 1000L));
		
		Thread thread1 = new Thread(() -> {
			token1 = oAuthCredentials.getOAuthToken();
		});
		
		Thread thread2 = new Thread(() -> {
			oAuthCredentials.setTokenExpired(true);
			token2 = oAuthCredentials.getOAuthToken();
		});
		
		thread1.start();
		thread2.start();
		
		thread1.join();
		thread2.join();
		
		assertEquals(token1.getAccessToken(), token2.getAccessToken());
		verify(oAuthCredentials, times(1)).fetchBearerToken();
	}
	
	private static class TestOAuthCredentials extends OAuthCredentials {
		private long fetchBearerTokenSleep;
		private int accessTokenPostfix;

		public TestOAuthCredentials(String url, String clientId, String clientSecret, long fetchBearerTokenSleep) {
			super(url, clientId, clientSecret);
			this.fetchBearerTokenSleep = fetchBearerTokenSleep;
			accessTokenPostfix = 1;
		}
		
		@Override
		protected OAuthToken fetchBearerToken() {
			try {
				Thread.sleep(fetchBearerTokenSleep);
			} catch (InterruptedException e) {}
			
			setTokenExpired(false);
			return new OAuthToken("testaccesstoken" + accessTokenPostfix++, "Bearer", 3600);
		}
		
	}
}
