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

package org.apache.geronimo.gshell.remote.crypto;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an abstraction of the crypto bits which are required for some remote shell communications.
 *
 * @version $Rev$ $Date$
 */
public class CryptoContext
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    //
    // TODO: See if we should use DSA or RSA for this...
    //
    
    private String transformation = "RSA";

    private String provider;

    private final KeyPair keyPair;

    public CryptoContext() throws Exception {
        KeyPairGenerator keyGen = createKeyPairGenerator();
        keyGen.initialize(1024);
        
        keyPair = keyGen.genKeyPair();
    }

    public CryptoContext(final String transformation, final String provider) throws Exception {
        this();
        this.transformation = transformation;
        this.provider = provider;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    private byte[] codec(final int mode, final Key key, final byte[] bytes) throws Exception {
        assert key != null;
        assert bytes != null;

        Cipher cipher = createCipher();
        cipher.init(mode, key);

        return cipher.doFinal(bytes);
    }

    public byte[] encrypt(final Key key, final byte[] bytes) throws Exception {
        return codec(Cipher.ENCRYPT_MODE, key, bytes);
    }

    public byte[] encrypt(final byte[] bytes) throws Exception {
        return encrypt(keyPair.getPublic(), bytes);
    }

    public byte[] decrypt(final Key key, final byte[] bytes) throws Exception {
        return codec(Cipher.DECRYPT_MODE, key, bytes);
    }

    public byte[] decrypt(final byte[] bytes) throws Exception {
        return decrypt(keyPair.getPrivate(), bytes);
    }

    public PublicKey deserializePublicKey(final byte[] bytes) throws Exception {
        assert bytes != null;

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        KeyFactory keyFactory = createKeyFactory();

        return keyFactory.generatePublic(spec);
    }
    
    //
    // JCE Access
    //

    private KeyPairGenerator createKeyPairGenerator() throws Exception {
        if (provider != null) {
            return KeyPairGenerator.getInstance(transformation, provider);
        }
        else {
            return KeyPairGenerator.getInstance(transformation);
        }
    }

    private Cipher createCipher() throws Exception {
        if (provider != null) {
            return Cipher.getInstance(transformation, provider);
        }
        else {
            return Cipher.getInstance(transformation);
        }
    }

    private KeyFactory createKeyFactory() throws Exception {
        if (provider != null) {
            return KeyFactory.getInstance(transformation, provider);
        }
        else {
            return KeyFactory.getInstance(transformation);
        }
    }
}