package com.tollge.modules.wechat;

import com.tollge.common.util.Properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class WeChatPaySigner {
  private static final String PRIVATE_KEY_PEM = Properties.getString("wechat", "pay.privateKey");
  private static final String PEM_NO = Properties.getString("wechat", "pay.pemNo");

  // 2. 加载私钥
  private static final PrivateKey privateKey = loadPrivateKey();

  public static void signQuery(String mchid, String method, String bodyStr, String uriPath, String machid) {
    long timestamp = System.currentTimeMillis() / 1000;
    String nonceStr = generateNonceStr();

    // 1. 构造签名串
    String signatureStr = buildSignatureString(method, uriPath, timestamp, nonceStr, bodyStr);

    // 3. 生成签名
    byte[] signatureBytes = sign(signatureStr.getBytes(StandardCharsets.UTF_8));
    String signature = Base64.getEncoder().encodeToString(signatureBytes);

    // 4. 构建Authorization头
    String authorizationHeader = buildAuthorizationHeader(mchid, timestamp, nonceStr, signature);

    System.out.println("Authorization Header: " + authorizationHeader);
  }

  private static String buildSignatureString(String method, String urlPath, long timestamp, String nonceStr, String bodyStr) {
    // 构造签名串
    ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
    try {
      signatureStream.write(method.getBytes(StandardCharsets.UTF_8));
      signatureStream.write('\n');
      signatureStream.write(urlPath.getBytes(StandardCharsets.UTF_8));
      signatureStream.write('\n');
      signatureStream.write(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
      signatureStream.write('\n');
      signatureStream.write(nonceStr.getBytes(StandardCharsets.UTF_8));
      signatureStream.write('\n');
      signatureStream.write(bodyStr.getBytes(StandardCharsets.UTF_8));
      signatureStream.write('\n');
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return signatureStream.toString(StandardCharsets.UTF_8);
  }

  private static PrivateKey loadPrivateKey() {
    byte[] keyBytes = Base64.getDecoder().decode(WeChatPaySigner.PRIVATE_KEY_PEM);

    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    try {
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePrivate(spec);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static byte[] sign(byte[] data) {
    try {
      Signature signer = Signature.getInstance("SHA256withRSA");
      signer.initSign(WeChatPaySigner.privateKey);
      signer.update(data);
      return signer.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new RuntimeException(e);
    }

  }

  private static String generateNonceStr() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return bytesToHex(bytes);
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hex = new StringBuilder();
    for (byte b : bytes) {
      hex.append(String.format("%02X", b));
    }
    return hex.toString();
  }

  private static String buildAuthorizationHeader(String mchid, long timestamp, String nonceStr, String signature) {
    return String.format(
      "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",signature=\"%s\",timestamp=\"%d\",serial_no=\"%s\"",
      mchid, // 替换为实际商户号
      nonceStr,
      signature,
      timestamp,
      PEM_NO // 替换为实际证书序列号
    );
  }
}
