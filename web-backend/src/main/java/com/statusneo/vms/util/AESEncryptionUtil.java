/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for AES encryption and decryption of sensitive data.
 * Uses AES-128 encryption with a fixed secret key.
 */
public class AESEncryptionUtil {

    // AES encryption algorithm specification
    private static final String ALGORITHM = "AES";
    // Secret key must be exactly 16 characters for AES-128
    private static final String SECRET_KEY = "MySecretKey12345"; // Must be 16 chars (AES-128)

    /**
     * Encrypts the given data using AES encryption.
     * Process:
     * 1. Creates a secret key from the fixed key
     * 2. Initializes cipher in encryption mode
     * 3. Encrypts the data
     * 4. Base64 encodes the encrypted bytes for safe transmission
     */
    public static String encrypt(String data) {
        try {
            // Create secret key specification for AES
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            // Get cipher instance for AES
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Initialize cipher in encryption mode with the secret key
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // Perform encryption
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            // Convert encrypted bytes to Base64 string for safe transmission
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.getMessage());
        }
    }

    /**
     * Decrypts the given encrypted data using AES decryption.
     * Process:
     * 1. Base64 decodes the encrypted string
     * 2. Creates a secret key from the fixed key
     * 3. Initializes cipher in decryption mode
     * 4. Decrypts the data
     */
    public static String decrypt(String encryptedData) {
        try {
            // Create secret key specification for AES
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            // Get cipher instance for AES
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Initialize cipher in decryption mode with the secret key
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // Decode Base64 string to encrypted bytes
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            // Perform decryption and convert to string
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.getMessage());
        }
    }
}