/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//
// NOTE: Snatched and massaged from Apache Mina'a Examples
//

package org.apache.geronimo.gshell.whisper.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import javax.annotation.PostConstruct;

import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create a bougus SSLContext.
 *
 * @version $Rev$ $Date$
 */
public class BogusSSLContextFactory
    implements SSLContextFactory
{
    private static final String PROTOCOL = "TLS";

    private static final String DEFAULT_KEY_MANAGER_FACTORY_ALGORITHM = "SunX509";

    private static final String KEY_MANAGER_FACTORY_ALGORITHM;

    static {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        
        if (algorithm == null) {
            algorithm = DEFAULT_KEY_MANAGER_FACTORY_ALGORITHM;
        }

        KEY_MANAGER_FACTORY_ALGORITHM = algorithm;
    }

    //
    // NOTE: The keystore was generated using keytool:
    //   keytool -genkey -alias bogus -keysize 512 -validity 3650
    //           -keyalg RSA -dname "CN=bogus.com, OU=XXX CA,
    //               O=Bogus Inc, L=Stockholm, S=Stockholm, C=SE"
    //           -keypass boguspw -storepass boguspw -keystore bogus.cert

    private final Logger log = LoggerFactory.getLogger(getClass());

    // @Configuration
    private boolean preload = true;

    // @Configuration
    private String keystoreResource = "bogus.cert";

    // @Configuration
    private char[] keystorePassword = { 'b', 'o', 'g', 'u', 's', 'p', 'w' };

    private SSLContext serverInstance;

    private SSLContext clientInstance;

    @PostConstruct
    public synchronized void init() throws Exception {
        if (preload) {
            log.debug("Preloading SSLContext instances");
            
            try {
                createServerContext();
                createClientContext();
            }
            catch (GeneralSecurityException e) {
                throw new RuntimeException("Failed to setup SSLContext instances", e);
            }
        }
    }

    //
    // SSLContextFactory
    //

    public synchronized SSLContext createServerContext() throws GeneralSecurityException {
        if (serverInstance == null) {
            KeyStore keyStore;

            try {
                keyStore = KeyStore.getInstance("JKS");

                InputStream in = getClass().getResourceAsStream(keystoreResource);
                if (in == null) {
                    throw new GeneralSecurityException("Failed to load bogus keystore from resource: " + keystoreResource);
                }

                try {
                    keyStore.load(in, keystorePassword);
                }
                finally {
                    IOUtil.close(in);
                }
            }
            catch (IOException e) {
                throw new GeneralSecurityException("Failed to load bogus keystore", e);
            }

            // Set up key manager factory to use our key store
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_FACTORY_ALGORITHM);
            keyManagerFactory.init(keyStore, keystorePassword);

            // Initialize the SSLContext to work with our key managers.
            SSLContext context = SSLContext.getInstance(PROTOCOL);
            context.init(keyManagerFactory.getKeyManagers(), BogusTrustManagerFactory.X509_MANAGERS, null);

            serverInstance = context;

            log.debug("Created server SSLContext: {}", serverInstance);
        }

        return serverInstance;
    }

    public synchronized SSLContext createClientContext() throws GeneralSecurityException {
        if (clientInstance == null) {
            SSLContext context = SSLContext.getInstance(PROTOCOL);
            context.init(null, BogusTrustManagerFactory.X509_MANAGERS, null);

            clientInstance = context;

            log.debug("Created client SSLContext: {}", clientInstance);
        }

        return clientInstance;
    }

    //
    // BogusTrustManagerFactory
    //

    private static class BogusTrustManagerFactory
        extends TrustManagerFactorySpi
    {
        private static final X509TrustManager X509 = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String s) throws CertificateException {}

            public void checkServerTrusted(X509Certificate[] c, String s) throws CertificateException {}

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        private static final TrustManager[] X509_MANAGERS = { X509 };

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return X509_MANAGERS;
        }

        @Override
        protected void engineInit(KeyStore keystore) throws KeyStoreException {}

        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {}
    }
}
