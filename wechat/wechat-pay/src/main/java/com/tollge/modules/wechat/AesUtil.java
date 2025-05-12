package com.tollge.modules.wechat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AesUtil {

  static final int KEY_LENGTH_BYTE = 32;

  static final int TAG_LENGTH_BIT = 128;

  public static String decryptToString(byte[] associatedData, byte[] nonce, String ciphertext, byte[] aesKey) {

    try {

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

      SecretKeySpec key = new SecretKeySpec(aesKey, "AES");

      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);

      cipher.init(Cipher.DECRYPT_MODE, key, spec);

      cipher.updateAAD(associatedData);

      return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");

    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

      throw new IllegalStateException(e);

    } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
             BadPaddingException | UnsupportedEncodingException e) {

      throw new IllegalArgumentException(e);

    }

  }

}
