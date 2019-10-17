package example.pay.wechat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * <P>
 * </p>
 * @author shaozhengmao
 * @version 1.0
 * @Date 2016年5月3日
 */
public class Oauth {

    public static final String CODE_URI = "http://open.weixin.qq.com/connect/oauth2/authorize"; // 用户同意授权，获取code
    public static final String TOKEN_URI = "https://api.weixin.qq.com/sns/oauth2/access_token"; // 通过code换取网页授权access_token
    public static final String REFRESH_TOKEN_URI = "https://api.weixin.qq.com/sns/oauth2/refresh_token";// 刷新token
    public static final String UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder"; // 统一下单接口
    public static Oauth me = new Oauth();
    public static final String APPID = "公众号appid";
    public static final String MCH_ID = "商户号id";
    public static final String APPSECRET = "公众号secret";
    public static final String MCHSECRET = "商户号secret";

    public static final String REDIRECT_URI = "http://yourdomain/wx/api/wx_redirect"; // 微信用户授权后回调接口
    public static final String NOTIFYURL = "http://yourdomain/wx/api/pay_notify";// 微信支付完成之后回调接口

    public Oauth() {
    }

    /**
     * 请求code
     * @return
     * @throws Exception
     */
    public String getCode() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("response_type", "code");
        params.put("redirect_uri", REDIRECT_URI);
        params.put("scope", "snsapi_base"); // snsapi_base（不弹出授权页面，只能拿到用户openid）snsapi_userinfo
        // （弹出授权页面，这个可以通过 openid 拿到昵称、性别、所在地）
        params.put("state", "wx#wechat_redirect");
        String para = HttpKit.map2Url(params, false);
        return CODE_URI + "?" + para;
    }

    /**
     * 通过code 换取 access_token
     * @param code
     * @return
     * @throws Exception
     */
    public String getToken(String code) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("secret", APPSECRET);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        return HttpKit.get(TOKEN_URI, params);
    }

    /**
     * 刷新 access_token
     * @param refreshToken
     * @return
     * @throws Exception
     */
    public String getRefreshToken(String refreshToken) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        return HttpKit.get(REFRESH_TOKEN_URI, params);
    }

    /**
     * <P>
     * Description:微信统一下单
     * </p>
     * @author shaozhengmao
     * @version 1.0
     * @Date 2016年5月3日下午6:28:38
     * @param params
     * @return
     * @throws Exception
     */
    public String unifiedOrder(Map<String, String> params) throws Exception {
        Map<String, String> paraMap = new HashMap<String, String>();

        paraMap.putAll(params);

        paraMap.put("appid", APPID);
        paraMap.put("mch_id", MCH_ID);
        paraMap.put("nonce_str", create_nonce_str());
        paraMap.put("trade_type", "JSAPI");
        paraMap.put("notify_url", NOTIFYURL);// 此路径是微信服务器调用支付结果通知路径
        String sign = WxUtils.getSign(paraMap, MCHSECRET);
        paraMap.put("sign", sign);

        String xml = WxUtils.MapToXml(paraMap);
        String xmlStr = HttpKit.post(UNIFIED_ORDER, xml);
        return xmlStr;
    }

    public static String getTradeNo() {
        String timestamp = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        return "WX" + timestamp;
    }

    private String create_nonce_str() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for (int i = 0; i < 16; i++) {
            Random rd = new Random();
            res += chars.charAt(rd.nextInt(chars.length() - 1));
        }
        return res;
    }

    public static String getAddrIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

}
