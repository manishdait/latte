package com.example.latte_api.smtp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESUtil {
  private final String ALGORITHM = "AES";
  private final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
  @Value("${latte.secret}")
  private String secret;

  public String encrypt(String txt) {
    SecretKey secretKey = new SecretKeySpec(secret.getBytes(), ALGORITHM);
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);

      byte[] bytes = cipher.doFinal(txt.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(bytes);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error encrypting text");
    }
  }
  
  public String decrypt(String txt) {
    byte[] bytes = Base64.getDecoder().decode(txt);

    SecretKey secretKey = new SecretKeySpec(secret.getBytes(), ALGORITHM);
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      
      byte[] decrypt = cipher.doFinal(bytes);
      return new String(decrypt);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error decrypting text");
    }
  }
}
