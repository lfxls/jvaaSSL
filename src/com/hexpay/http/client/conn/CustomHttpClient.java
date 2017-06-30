package com.hexpay.http.client.conn;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class CustomHttpClient
{
  private static HttpClient customHttpClient = httpClientInstance();
  private static final int TIME_OUT = 60000;
  private static final int MAX_CONNECTIONS_TOTAL = 200;
  private static final int MAX_CONNECTIONS_PER_ROUTE = 50;

  public static HttpClient GetHttpClient()
  {
    return customHttpClient;
  }

  private static HttpClient httpClientInstance()
  {
    SSLSocketFactory sf = null;
    try
    {
      KeyStore trustStore = KeyStore.getInstance("JKS");
      InputStream inputStream = new FileInputStream("G:/SSL/keytool/client.truststore");
      trustStore.load(inputStream, "123456".toCharArray());
      sf = new MySSLSocketFactory(trustStore);
      sf
        .setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      
//      trustStore.load(inputStream, "123456".toCharArray());
//      sf = new SSLSocketFactory(trustStore);
//      sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
    catch (KeyManagementException e) {
      System.out.println(e.getMessage());
    }
    catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
    }
    catch (UnrecoverableKeyException e) {
      System.out.println(e.getMessage());
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
    catch (CertificateException e) {
      System.out.println(e.getMessage());
    }
    catch (KeyStoreException e) {
      System.out.println(e.getMessage());
    }

    SchemeRegistry schReg = new SchemeRegistry();
    schReg.register(
      new Scheme("http", 
      PlainSocketFactory.getSocketFactory(), 80));
    schReg.register(new Scheme("https", sf, 8443));

    PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager(
      schReg);
    conMgr.setMaxTotal(200);
    conMgr.setDefaultMaxPerRoute(50);

    customHttpClient = new DefaultHttpClient(conMgr);

    HttpParams params = customHttpClient.getParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpConnectionParams.setConnectionTimeout(params, 60000);
    HttpConnectionParams.setSoTimeout(params, 60000);
    return customHttpClient;
  }

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }
}
