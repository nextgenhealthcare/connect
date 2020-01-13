package com.cloudsolutions.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509KeyManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.sun.istack.Nullable;


/**
 * Represents an ordered list of {@link X509KeyManager}s with most-preferred managers first.
 *
 * This is necessary because of the fine-print on {@link SSLContext#init}:
 *     Only the first instance of a particular key and/or trust manager implementation type in the
 *     array is used. (For example, only the first javax.net.ssl.X509KeyManager in the array will be used.)
 *
 * @author codyaray
 * @since 4/22/2013
 * @see http://stackoverflow.com/questions/1793979/registering-multiple-keystores-in-jvm
 */
public class CompositeX509KeyManager implements X509KeyManager {

  private final List<X509KeyManager> keyManagers;

  /**
   * Creates a new {@link CompositeX509KeyManager}.
   *
   * @param keyManagers the X509 key managers, ordered with the most-preferred managers first.
   */
  public CompositeX509KeyManager(List<X509KeyManager> keyManagers) {
    this.keyManagers = ImmutableList.copyOf(keyManagers);
  }

  /**
   * Chooses the first non-null client alias returned from the delegate
   * {@link X509TrustManagers}, or {@code null} if there are no matches.
   */
  @Override
  public @Nullable String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
    for (X509KeyManager keyManager : keyManagers) {
      String alias = keyManager.chooseClientAlias(keyType, issuers, socket);
      if (alias != null) {
        return alias;
      }
    }
    return null;
  }

  /**
   * Chooses the first non-null server alias returned from the delegate
   * {@link X509TrustManagers}, or {@code null} if there are no matches.
   */
  @Override
  public @Nullable String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    for (X509KeyManager keyManager : keyManagers) {
      String alias = keyManager.chooseServerAlias(keyType, issuers, socket);
      if (alias != null) {
        return alias;
      }
    }
    return null;
  }

  /**
   * Returns the first non-null private key associated with the
   * given alias, or {@code null} if the alias can't be found.
   */
  @Override
  public @Nullable PrivateKey getPrivateKey(String alias) {
    for (X509KeyManager keyManager : keyManagers) {
      PrivateKey privateKey = keyManager.getPrivateKey(alias);
      if (privateKey != null) {
        return privateKey;
      }
    }
    return null;
  }

  /**
   * Returns the first non-null certificate chain associated with the
   * given alias, or {@code null} if the alias can't be found.
   */
  @Override
  public @Nullable X509Certificate[] getCertificateChain(String alias) {
    for (X509KeyManager keyManager : keyManagers) {
      X509Certificate[] chain = keyManager.getCertificateChain(alias);
      if (chain != null && chain.length > 0) {
        return chain;
      }
    }
    return null;
  }

  /**
   * Get all matching aliases for authenticating the client side of a
   * secure socket, or {@code null} if there are no matches.
   */
  @Override
  public @Nullable String[] getClientAliases(String keyType, Principal[] issuers) {
    Builder<String> aliases = ImmutableList.builder();
    for (X509KeyManager keyManager : keyManagers) {
      aliases.add(keyManager.getClientAliases(keyType, issuers));
    }
    String[] arr= Iterables.toArray(aliases.build(), String.class);
    return emptyToNull(arr);
  }

  /**
   * Get all matching aliases for authenticating the server side of a
   * secure socket, or {@code null} if there are no matches.
   */
  @Override
  public @Nullable String[] getServerAliases(String keyType, Principal[] issuers) {
    Builder<String> aliases = ImmutableList.builder();
    for (X509KeyManager keyManager : keyManagers) {
      aliases.add(keyManager.getServerAliases(keyType, issuers));
    }
    return emptyToNull(Iterables.toArray(aliases.build(), String.class));
  }

  @Nullable
  private static <T> T[] emptyToNull(T[] arr) {
    return (arr.length == 0) ? null : arr;
  }

}