//package com.statusneo.vms.service;
//
//import org.jasypt.util.text.AES256TextEncryptor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EncryptionService {
//
//    private final AES256TextEncryptor textEncryptor;
//
//    public EncryptionService(@Value("${encryption.secret-key}") String secretKey) {
//        this.textEncryptor = new AES256TextEncryptor();
//        this.textEncryptor.setPassword(secretKey);
//    }
//
//    public String encrypt(String data) {
//        return textEncryptor.encrypt(data);
//    }
//
//    public String decrypt(String encryptedData) {
//        return textEncryptor.decrypt(encryptedData);
//    }
//}