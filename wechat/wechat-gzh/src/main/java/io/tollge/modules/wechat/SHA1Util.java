package io.tollge.modules.wechat;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA1
 * https://gitee.com/yuquan0405/java_jiami
 * @author ThinkPad
 */
class SHA1Util {

    static String encode(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        return hex(md.digest(message.getBytes("CP1252")));
    }

    private static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            sb.append(Integer.toHexString((anArray & 0xFF) | 0x100)
                    .toUpperCase().substring(1, 3));
        }
        return sb.toString();
    }
}