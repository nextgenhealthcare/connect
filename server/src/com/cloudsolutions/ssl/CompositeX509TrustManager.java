package com.cloudsolutions.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Represents an ordered list of {@link X509TrustManager}s with additive trust. If any one of the
 * composed managers trusts a certificate chain, then it is trusted by the composite manager.
 *
 * This is necessary because of the fine-print on {@link SSLContext#init}:
 *     Only the first instance of a particular key and/or trust manager implementation type in the
 *     array is used. (For example, only the first javax.net.ssl.X509KeyManager in the array will be used.)
 *
 * @author codyaray
 * @since 4/22/2013
 * @see http://stackoverflow.com/questions/1793979/registering-multiple-keystores-in-jvm
 */
public class CompositeX509TrustManager implements X509TrustManager {

  private final List<X509TrustManager> trustManagers;

  public CompositeX509TrustManager(List trustManagers) {
    this.trustManagers = ImmutableList.copyOf(trustManagers);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    for (X509TrustManager trustManager : trustManagers) {
      try {
        trustManager.checkClientTrusted(chain, authType);
        return; // someone trusts them. success!
      } catch (CertificateException e) {
        // maybe someone else will trust them
      }
    }
    throw new CertificateException("None of the TrustManagers trust this certificate chain");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    for (X509TrustManager trustManager : trustManagers) {
      try {
        trustManager.checkServerTrusted(chain, authType);
        return; // someone trusts them. success!
      } catch (CertificateException e) {
        // maybe someone else will trust them
      }
    }
    throw new CertificateException("None of the TrustManagers trust this certificate chain");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    ImmutableList.Builder certificates = ImmutableList.builder();
    for (X509TrustManager trustManager : trustManagers) {
      certificates.add(trustManager.getAcceptedIssuers());
    }
    return Iterables.toArray(certificates.build(), X509Certificate.class);
  }

}