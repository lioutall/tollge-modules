package com.tollge.modules.wechat;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ResourcePojo {
    private String transaction_id;
    private Amount amount;
    private String mchid;
    private String trade_state;
    private String bank_type;
    private List<PromotionDetail> promotion_detail;
    private String success_time;
    private Payer payer;
    private String out_trade_no;
    private String appid;
    private String trade_state_desc;
    private String trade_type;
    private String attach;
    private SceneInfo scene_info;

    @Data
    @NoArgsConstructor
    public static class Amount {
        private Integer payer_total;
        private Integer total;
        private String currency;
        private String payer_currency;
    }

    @Data
    @NoArgsConstructor
    public static class Payer {
        private String openid;
    }

    @Data
    @NoArgsConstructor
    public static class SceneInfo {
        private String device_id;
    }

    @Data
    @NoArgsConstructor
    public static class PromotionDetail {
        private Integer amount;
        private Integer wechatpay_contribute;
        private String coupon_id;
        private String scope;
        private Integer merchant_contribute;
        private String name;
        private Integer other_contribute;
        private String currency;
        private String stock_id;
        private List<GoodsDetail> goods_detail;

        @Data
        @NoArgsConstructor
        public static class GoodsDetail {
            private String goods_remark;
            private Integer quantity;
            private Integer discount_amount;
            private String goods_id;
            private Integer unit_price;
        }
    }
}
