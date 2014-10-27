package com.meltmedia.jackson.crypto;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meltmedia.jackson.crypto.EncryptedJson.Cipher;
import com.meltmedia.jackson.crypto.EncryptedJson.KeyDerivation;

public class DynamicEncryptionServiceTest {

  public static String IV_FROM_NODE = "Z/t5j0YbTCGstAurhMWzow==";
  public static String SALT_FROM_NODE = "LhrWBg==";
  public static String VALUE_FROM_NODE = "p5lQp8jLM3Xt6pGL2s/YpPC+Xr28FJQl07Qh30qtDIE=";
  public static String DECRYPTED_FROM_NODE = "This is my text!";
  DynamicEncryptionService cipher;
  Base64 base64 = new Base64();

  @Before
  public void setUp() throws UnsupportedEncodingException {
    DynamicEncryptionConfiguration config = new DynamicEncryptionConfiguration();
    CipherSettings settings = new CipherSettings();
    settings.setKeyLength(256);
    settings.setIterationCount(2000);
    settings.setPassword("password".toCharArray());

    config.getKeys().put("default", "password".toCharArray());
    config.setCurrentKey("default");

    cipher = new DynamicEncryptionService(config);
  }

  @Test
  public void shouldDecryptValueFromNodeCipher() throws EncryptionException, UnsupportedEncodingException {
    EncryptedJson value = new EncryptedJson();
    value.setKeyName("default");
    value.setIv(base64.decode(IV_FROM_NODE));
    value.setSalt(base64.decode(SALT_FROM_NODE));
    value.setValue(base64.decode(VALUE_FROM_NODE));
    value.setIterations(2000);
    value.setKeyLength(256);
    value.setCipher(Cipher.AES_256_CBC);
    value.setKeyDerivation(KeyDerivation.PBKDF_2);

    String result = cipher.decrypt(value, "UTF-8");

    assertThat("decrypt value from node cipher", result, equalTo(DECRYPTED_FROM_NODE));
  }

  @Test
  public void shouldSerializeInBase64() throws EncryptionException, UnsupportedEncodingException, JsonProcessingException {
    EncryptedJson value = new EncryptedJson();
    value.setKeyName("default");
    value.setIv(base64.decode(IV_FROM_NODE));
    value.setSalt(base64.decode(SALT_FROM_NODE));
    value.setValue(base64.decode(VALUE_FROM_NODE));
    value.setIterations(2000);
    value.setKeyLength(256);
    value.setCipher(Cipher.AES_256_CBC);
    value.setKeyDerivation(KeyDerivation.PBKDF_2);

    ObjectMapper mapper = new ObjectMapper();
    String serialized = mapper.writeValueAsString(value);

    assertThat("the iv is found in the text", serialized, containsString(IV_FROM_NODE));
    assertThat("the salt is found in the text", serialized, containsString(SALT_FROM_NODE));
    assertThat("the value is found in the text", serialized, containsString(VALUE_FROM_NODE));
  }

  @Test
  public void shouldDecryptEncryptedValues() throws EncryptionException, UnsupportedEncodingException {
    EncryptedJson value = cipher.encrypt(DECRYPTED_FROM_NODE, "UTF-8");
    String decrypted = cipher.decrypt(value, "UTF-8");

    assertThat("a value encrypted with the cipher can be decrypted", decrypted, equalTo(DECRYPTED_FROM_NODE));
  }
  
  @Test
  public void shouldFailIfNoKeyNameSpecified() throws UnsupportedEncodingException {
    EncryptedJson value = new EncryptedJson();
    value.setIv(base64.decode(IV_FROM_NODE));
    value.setSalt(base64.decode(SALT_FROM_NODE));
    value.setValue(base64.decode(VALUE_FROM_NODE));
    value.setIterations(2000);
    value.setKeyLength(256);
    value.setCipher(Cipher.AES_256_CBC);
    value.setKeyDerivation(KeyDerivation.PBKDF_2);
    value.setKeyName("undefined");

    try {
      cipher.decrypt(value, "UTF-8");
      fail("Missing key name did not cause exception.");
    }
    catch( EncryptionException ee ) {
      assertThat("the property is identified in message", ee.getMessage(), containsString("undefined"));
    }
  }
  

}