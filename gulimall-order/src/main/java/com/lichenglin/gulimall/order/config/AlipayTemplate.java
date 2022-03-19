package com.lichenglin.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.lichenglin.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000119640255";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC9wsfJnUJKAcTpD5BJEarZ8R3tJ3r7IgBtzlqxS7zsH4veR4vq5ja7tpYn/RjO6IhlnETh6H9VvxHnUL3L4B2D6Vrk0kYHGVNOY/vHsQReGOmIt3dGriRwHMzagzq5ZcT2buktxxeCMfBBTVv7HfFuC+RunLNSvFxY4AHcFvFyD8wUyAwkNGQsFs4ldQuunVbU0P2NIkNttmP81G87sQG0heXtbQLAz9Bs+m+OyiRyfGW4ryCNgnWQEKUYSVBdHh5FIn5kWd9/R0y9l8HwRV7mSKKblFJNhotZq1S53i10v9QYD2L2uZhBcNg2JN2mB4uYg//mNMUh9rgf+iFQ7uitAgMBAAECggEAX5+Ez9s4KVUE5jDak4ecoeC4JOatOVnj/3FqEEfLNu2arPM+1asWz7btqc8HrlRNT27U40YmzI3RNtBgJBmWpf90tzQi57biXPFTUkiom8ZQAH37VBmy8PACZuaUNBLNkyVGybO26/vARxf1jvCC1eKWRQsf7kRPANY4+bByEPh4Vg/jazlw569nzOx2hZwLT4vQJo3hgO1qFzHDC4Sg3ChFHZaa1N2ncBeUPyN/LcTVCBQmG5HOvGVhDFG9bbZd6RS8d+NhBeCRT63ZbrbKjSmAYJrLH5fmFFnjXYTCVnsM+cVGrNEsUzWFIKs9gF5SzOeKUQ0KrbSW1cKR+PkNXQKBgQDrBfY4f5Fz78ouJJRD4ucfExEQ9q0HtxYQlHl9z5pRXzY7XHfin42uEWV53FjY9cv+XSn5ErmxLrtHc3jPwoMru1LFqvIzspmqwSjZUUGefaOXrl7DdKBmBH/RKFtLN88cZuabUBYyQXxq41Nmi1r9/MnvD8PssXqo3XsbnV6n/wKBgQDOsp5/vOCMRr8HVMHXBvwTpw9D5G6c4+WYStSE14ma2OE9u8HAcHIrTh/x9oEnA2PtTsBuF5IjdAmU6nEgmnOqKjpXjUKcYxxpL/sW8OLry6AzkWWPA8n8hppTpOuqJRXWQCkoAP5JOvRG83wkFCf9br+xGXuFi21tjnes/JmPUwKBgQDUCF7o/f5F8JIhKxMwY+dMG7GL8mCM01ruvmUVYlTBEPsmdvgZ1wTFhCBe3rKxmp/d7kNN2Nz6w0Rm5ACcv8oDjE7S0rUMexcz/GxUE4D18jfDQqdwAyoqn+iI1LX4mHMcFqH6gThlQrty24CExTjkaEnuifSi+EPHUXodoc6iFQKBgHKRozUelkBqvlvr6P2tC3BBOz7gkUc5Mvv1DlMLvWQtLrwvJDfsm/vQ8/gEMJZVVMAm1JaxTfmEygLW6BmwQshhW7FHxVI1xXuwcrbGkqEsXwH2Z5qb+QuV+FjAozfOEoqNScK4ujoRCXGCaJ7+5SOejXY2r7bYD7GYNFGYOe2tAoGBANamPIAqPaBiMmWqjsGwxVQ6BpATbbcVtcbcrGcoDkn2oErdHwKd00/Wz/d4XHXZYCzGpYBOgFDnklecMwQqG9ibIrsE9JHzrOLUSqWRi5x6HenGiJbOzIFZN65Rph1sH9gkdgqopYgbH01vv2/csP9I2eHgp47XKuo8LZiApm9c";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVrjlpanqGrb/CD2zZA0vmfE1RCRPMuXPN+wBeaL4V/8m8zjfCvz71IMQsGKy/IeHoJw/DPgRySP8JkLkcOxD+oHHiU8YBrnfXknxDvZT1RXCTgEY1QzHBae788rDDvYTs22TLPa0/oFuve6bydoopA1/12ImiuFtaIdDGeoGvlPmN1F/RnOmOfKI36XeKPqHlONFzBSUnzkB4grVPHvUeN63VhG9mMQjyyopHM/u9e9YfwwudB1EYb63RdUAPFATKOYYZ6jGlYVbYmoU96N7lYYuN1WAIEwOa/eqcWEVeZQx48ESeBdkY1QaCSQiDKtb0ZeamNDQdUkGSl7sJBVTQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    //异步通知：间隔时间：4m、10m、10m、1h、2h、6h、15h
    private  String notify_url = "http://rtodwpc536.51xd.pub/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://user.gulimall.com/userOrderList.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
