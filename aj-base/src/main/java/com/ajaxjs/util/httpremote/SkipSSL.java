package com.ajaxjs.util.httpremote;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Utility class for managing SSL certificates in HTTP connections.
 * Provides functionality to skip SSL certificate validation for HTTPS connections
 * and to create secure connections with custom certificates.
 * <p>
 * This class can be used when working with self-signed certificates or
 * when you need to bypass certificate validation for testing purposes.
 */
@Slf4j
public class SkipSSL {
    /**
     * Custom X509TrustManager implementation that accepts all certificates.
     * This manager doesn't perform any certificate validation and is useful for testing
     * with self-signed certificates or during development.
     */
    private static class MyX509TrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // No validation performed, accepts all server certificates
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // No validation performed, accepts all client certificates
        }
    }

    /**
     * Creates an SSL socket factory with certificate validation disabled.
     * Can optionally include client certificates for mutual TLS authentication.
     * 
     * @param kms array of key managers containing client certificates, or null to use no client certificates
     * @return an SSLSocketFactory that accepts all server certificates, or null if an error occurs
     */
    public static SSLSocketFactory getSocketFactory(KeyManager[] kms) {
        TrustManager[] trustAllCerts = new TrustManager[]{new MyX509TrustManager()};

        try {
            SSLContext sc = SSLContext.getInstance("SSL"); // Some use SSLContext.getInstance("TLS")
            /*
             * First parameter is the authorized key manager for authentication;
             * null means no client certificate is uploaded (common case);
             * Second parameter is the authorized certificate manager to verify server certificates;
             * Third parameter is a random number, can be null
             */
            sc.init(kms, trustAllCerts, null);

            return sc.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.warn("Certificate-based secure connection", e);
            return null;
        }
    }

    /**
     * Creates a secure connection with a client certificate from an input stream.
     * Loads a PKCS12 certificate from the given input stream and initializes a key manager.
     * 
     * @param in  input stream containing the PKCS12 certificate
     * @param pwd password for the certificate
     * @return array of key managers initialized with the certificate, or null if an error occurs
     */
    static KeyManager[] loadCert(InputStream in, String pwd) {
        try {
            // Load certificate into keystore
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, pwd.toCharArray());

            // Initialize key manager
            // Some use getInstance("SunX509")
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pwd.toCharArray());

            return kmf.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
            log.warn("Creating certificate-based secure connection", e);
            return null;
        }
    }

    /**
     * Creates a secure connection with a client certificate from a file path.
     * 
     * @param path file path to the PKCS12 certificate
     * @param pwd  password for the certificate
     * @return array of key managers initialized with the certificate, or null if an error occurs
     */
    static KeyManager[] loadCert(String path, String pwd) {
        try (FileInputStream in = new FileInputStream(path)) {
            return loadCert(in, pwd);
        } catch (IOException e) {
            log.warn("Creating certificate-based secure connection", e);
            return null;
        }
    }

    /**
     * Flag to track if global SSL initialization has been performed
     */
    static boolean init;

    /**
     * Initializes global SSL settings to bypass certificate validation for all HTTPS connections.
     * This method sets the default SSL socket factory and hostname verifier to accept all certificates.
     * It only performs initialization once, even if called multiple times.
     */
    public static void init() {
        if (!init) {
            HttpsURLConnection.setDefaultSSLSocketFactory(Objects.requireNonNull(getSocketFactory(null)));
            HttpsURLConnection.setDefaultHostnameVerifier((urlHostName, session) -> true);

            init = true;
        }
    }

    /**
     * Disables SSL certificate validation for a specific HTTPS connection.
     * Sets a custom SSL socket factory and hostname verifier that accepts all certificates.
     * 
     * @param conn the HTTPS connection to configure
     */
    public static void setSSL_Ignore(HttpsURLConnection conn) {
        conn.setSSLSocketFactory(getSocketFactory(null));
//        conn.setHostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String urlHostName, SSLSession session) {
//                return true;
//            }
//        });
        conn.setHostnameVerifier((urlHostName, session) -> true); // This might not be necessary
    }
}