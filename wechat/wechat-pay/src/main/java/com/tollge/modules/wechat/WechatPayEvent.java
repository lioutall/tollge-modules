package com.tollge.modules.wechat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付回调事件实体
 */
@Data
@NoArgsConstructor
public class WechatPayEvent {
    private String id;
    private String createTime;
    private String resourceType;
    private String eventType;
    private String summary;
    private Resource resource;

    @Data
    @NoArgsConstructor
    public static class Resource {
        private String originalType;
        private String algorithm;
        private String ciphertext;
        private String associatedData;
        private String nonce;
    }
}
