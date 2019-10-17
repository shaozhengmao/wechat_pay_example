package example.pay.wechat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * 
 *<P>
 * Description:TODO
 * </p>
 * @author shaozhengmao
 * @version 1.0
 * @Date 2016年5月3日
 */
public class WxUtils {
    /**
     * 
     * <P>
     * Description:TODO
     * </p>
     * @version 1.0
     * @Date 2016年4月22日下午6:00:24
     * @return
     */
    public static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
    
    /**
     * 
     * <P>
     * Description:获得微信签名方法
     * </p>
     * @version 1.0
     * @Date 2016年4月22日下午6:03:25
     * @param params
     * @param appkey
     * @return
     */
    public static String getSign(Map<String, String> params, String appkey){
        Set<String> keysSet = params.keySet();
        Object[] keys = keysSet.toArray();
        Arrays.sort(keys);
        StringBuffer temp = new StringBuffer();
        boolean first = true;
        for (Object key : keys) {
            if (first) {
                first = false;
            } else {
                temp.append("&");
            }
            temp.append(key).append("=");
            Object value = params.get(key);
            String valueString = "";
            if (null != value) {
                valueString = value.toString();
            }
            temp.append(valueString);
        }
        String stringSignTemp = temp.toString() + "&key=" + appkey;
        String signValue = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
        return  signValue;
    }
    

    /**
     * map转成xml
     * 
     * @param arr
     * @return
     */
    public static String MapToXml(Map<String, String> map) {
        return MapToXmlWithTag(map, "xml");
    }
    
    public static String MapToXmlWithTag(Map<String, String> map, String tag) {
        String xml = "<"+tag+">";

        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String val = entry.getValue();
            xml += "<" + key + ">" + val + "</" + key + ">";
        }

        xml += "</"+tag+">";
        return xml;
    }
    
    /**
     * 
     * <P>
     * Description:TODO
     * </p>
     * @version 1.0
     * @Date 2016年4月22日下午6:00:29
     * @param xml
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static Map<String, String> doXMLParse(String xml)
            throws XmlPullParserException, IOException {

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());

        Map<String, String> map = null;

        XmlPullParser pullParser = XmlPullParserFactory.newInstance()
                .newPullParser();

        pullParser.setInput(inputStream, "UTF-8"); // 为xml设置要解析的xml数据

        int eventType = pullParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                map = new HashMap<String, String>();
                break;

            case XmlPullParser.START_TAG:
                String key = pullParser.getName();
                if (key.equals("xml"))
                    break;

                String value = pullParser.nextText();
                map.put(key, value);

                break;

            case XmlPullParser.END_TAG:
                break;

            }

            eventType = pullParser.next();

        }

        return map;
    }
    
    /**
     * 
     * <P>
     * Description:TODO
     * </p>
     * @version 1.0
     * @Date 2016年4月22日下午6:00:29
     * @param xml
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static Map<String, String> doXMLParseInputStream(InputStream inputStream)
            throws XmlPullParserException, IOException {

        Map<String, String> map = null;

        XmlPullParser pullParser = XmlPullParserFactory.newInstance()
                .newPullParser();

        pullParser.setInput(inputStream, "UTF-8"); // 为xml设置要解析的xml数据

        int eventType = pullParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                map = new HashMap<String, String>();
                break;

            case XmlPullParser.START_TAG:
                String key = pullParser.getName();
                if (key.equals("xml"))
                    break;

                String value = pullParser.nextText();
                map.put(key, value);

                break;

            case XmlPullParser.END_TAG:
                break;

            }

            eventType = pullParser.next();

        }

        return map;
    }
}

