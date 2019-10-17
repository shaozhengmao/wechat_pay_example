package example.pay.wechat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 
 *<P>
 * Description:http://mp.weixin.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.
 * html
 * #.E7.AC.AC.E4.B8.80.E6.AD.A5.EF.BC.9A.E7.94.A8.E6.88.B7.E5.90.8C.E6.84.8F
 * .E6.8E.88.E6.9D.83.EF.BC.8C.E8.8E.B7.E5.8F.96code
 * </p>
 * @author shaozhengmao
 * @version 1.0
 * @Date 2016年5月3日
 */
@Controller
@RequestMapping(value = "/wx/api", produces = "application/json;charset=UTF-8", method = {RequestMethod.GET,
        RequestMethod.POST})
public class WechatController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <P>
     * Description:获得code
     * </p>
     * @version 1.0
     * @Date 2016年4月21日下午7:52:50
     * @return
     */
    @RequestMapping(value = "/wx_code")
    @ResponseBody
    public String wxcharge() {
        // 此方法生成的链接，直接放到微信公众号自定义button下
        try {
            return Oauth.me.getCode();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * <P>
     * Description: 获得code之后回调接口
     * </p>
     * @version 1.0
     * @Date 2016年4月21日下午7:53:00
     * @return
     */
    @RequestMapping(value = "/wx_redirect")
    public String wxcallback(@RequestParam("code") String code,
            @RequestParam("state") String state, 
            HttpServletRequest request) {

        try {
            logger.info("=== state:{}, code:{} ===", state, code);
            if (StringUtils.isBlank(code)) {
                return "";
            }

            String tokenJson = Oauth.me.getToken(code);
            logger.info("=== get token:{} ===", tokenJson);
            JSONObject job = JSONObject.fromObject(tokenJson);
            if (job.containsKey("errcode")) {
                return "";
            }
            
            String openid = job.getString("openid");
            String access_token = job.getString("access_token");
            
            request.getSession().setAttribute("openid", openid);
            request.getSession().setAttribute("access_token", access_token);

            logger.info("=== openid:{}, access_token:{} ===", openid, access_token);
            return "wxproductlist";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * <P>
     * Description:点击购买之后 生成预支付账单存储 然后进行h5唤起流程
     * </p>
     * @version 1.0
     * @Date 2016年4月21日下午10:24:14
     * @param request
     * @param response
     */
    @RequestMapping(value = "/wx_prepay", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> wxjspay(HttpServletRequest request,
            HttpServletResponse response) {
        
       
        try {
            Map<String, String> payMap = new TreeMap<String, String>();
            // 获取openid
            String openId = (String) request.getSession().getAttribute("openid");
            
            logger.info("【wx_prepay_inner】 get openid from session, openid:{}", openId);
            if(StringUtils.isBlank(openId)){
                logger.warn("=== create yizhibo order failed! ,openId:{} ===" ,openId);
                return null;
            }
            
            long time = System.currentTimeMillis();
            //======================
            //TODO 业务处理，如自己系统下单
            //======================
            
            logger.info("=== create order cost time:{} ===", (System.currentTimeMillis()-time)+" ms");
            
            Map<String, String> paraMap = new HashMap<String, String>();
            
            paraMap.put("body", "60元人民币");
            paraMap.put("out_trade_no", "商家的订单id");
            paraMap.put("total_fee", "money");//单位分
            paraMap.put("spbill_create_ip", Oauth.getAddrIp(request));
            paraMap.put("openid", openId);
            
            long time1 = System.currentTimeMillis();
            //微信统一下单接口
            String xmlStr = Oauth.me.unifiedOrder(paraMap);
            logger.info("=== wechat create order cost time:{} ===", (System.currentTimeMillis()-time1)+" ms");
            // 预付商品id
            String prepay_id = "", nonce_str = "", sign = "";

            if (xmlStr.indexOf("SUCCESS") != -1) {
                Map<String, String> map = WxUtils.doXMLParse(xmlStr);
                logger.info(" get doXMLParse:{}", map);
                prepay_id = MapUtils.getString(map, "prepay_id");
                nonce_str = MapUtils.getString(map, "nonce_str");
                sign = MapUtils.getString(map, "sign");
            } else {
                logger.warn("=== prepay failed! ===");
            }
            
            payMap.put("appId", Oauth.APPID);
            payMap.put("timeStamp", WxUtils.create_timestamp());
            payMap.put("nonceStr", nonce_str);
            payMap.put("signType", "MD5");
            payMap.put("package", "prepay_id=" +prepay_id);
            String paySign = WxUtils.getSign(payMap, Oauth.MCHSECRET);

            payMap.put("pg", prepay_id);
            payMap.put("paySign", paySign);

            logger.info("=== return values after unifiedOrder :{}  ===", payMap);
            return payMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_7
     * <P>
     * Description:微信支付结果回调接口 用来更新订单状态
     * </p>
     * @author shaozhengmao
     * @version 1.0
     * @Date 2016年4月21日下午10:43:28
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/pay_notify")
    @ResponseBody
    public String appPayNotify(HttpServletRequest request, HttpServletResponse response) {
        // String xml =
        // "<xml><appid><![CDATA[wxb4dc953b356e]]></appid><bank_type><![CDATA[CCB_CREDIT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[1228442802]]></mch_id><nonce_str><![CDATA[1002477130]]></nonce_str><openid><![CDATA[o-HREuJzRr3moMvv990VdfnQ8x4k]]></openid><out_trade_no><![CDATA[10000000001249]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[1269E03E43F2B8C388AAE185CEE]]></sign><time_end><![CDATA[20150324100405]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[1009530574201503246299496]]></transaction_id></xml>";
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/xml");
            InputStream in = request.getInputStream();
            Map<String, String> map = WxUtils.doXMLParseInputStream(in);
            
            String result_code = MapUtils.getString(map,"result_code");
            String return_code = MapUtils.getString(map,"return_code");
            String return_msg = MapUtils.getString(map,"return_msg");
            String out_trade_no = MapUtils.getString(map,"out_trade_no");
            String openid = MapUtils.getString(map,"openid");
            logger.info(" wxshop charge callback, orderid:{}, result_code:{}, MAP:{}", out_trade_no, return_code, map);
            if(StringUtils.isBlank(out_trade_no)){
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[订单不存在]]></return_msg></xml>";
            }
            //======================
            //TODO 商家业务处理 更新订单状态
            //======================
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[解析错误]]></return_msg></xml>";
        }
        
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

}
