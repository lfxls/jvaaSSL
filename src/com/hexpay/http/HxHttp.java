package com.hexpay.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hexing.pay.oss.common.json.JSONUtils;
import com.hexpay.http.client.conn.HttpRequestSimple;
import com.hexpay.http.client.utils.VendingUtil;
import com.hexpay.http.client.utils.XmlToMap;

import java.io.PrintStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import tangdi.engine.context.Etf;
import tangdi.engine.context.Log;
import weixin.Utils.HttpXmlUtils;
import weixin.Utils.ParseXMLUtils;
import weixin.Utils.RandCharsUtils;
import weixin.Utils.WXSignUtils;
import weixin.Utils.WeixinConfigUtils;
import weixin.entity.Unifiedorder;

public class HxHttp
{
	private static String TempVari="";

  public static int String2Json(@Named("params") String params,@Named("paraval") String paraval, @Named("variable") String variable, @Named("lu") String lu)
  {
    try
    {
      if (StringUtils.isEmpty(params)||StringUtils.isEmpty(paraval)) {
        Log.info("参数内容不能为空", new Object[0]);
        return 2;
      }
      String[] arr = params.split("\\|");
      String[] arrval = paraval.split("\\|");
      JSONObject riskItemObj = new JSONObject();
      String varname = "";
      StringBuffer sb = new StringBuffer();
      sb.append("");
      for (int i = 0; i < arr.length; i++) {
        String name = arr[i];
        if (!StringUtils.isNotEmpty(name))
          continue;
        if ("1".equals(lu))
          varname = name.toLowerCase();
        else if ("2".equals(lu))
          varname = name.toUpperCase();
        else {
          varname = name;
        }
        riskItemObj.put(varname, StringUtils.isEmpty(
        		arrval[i]) ? 
          "" : 
        	  arrval[i]);
      }

      TempVari = riskItemObj.toString();
      return 0;
    } catch (Exception e) {
      Log.info("String转Json失败：%s", new Object[] { e.toString() });
    }return -1;
  }

  @Named("HxHttpReq")
  public static int HxHttpReq(@Named("params") String params,@Named("paraval") String paraval, @Named("url") String url, @Named("nodeName") String nodeName)
  {
	 /* String nonce_str = RandCharsUtils.getRandomString(16);
		System.out.println("随机字符串是："+nonce_str);
		String body = "Hexpay Test0.01_2";
		String attach = "backup";
		String myTradeno = RandCharsUtils.timeStart()+RandCharsUtils.getRandomString(14);
		String out_trade_no = myTradeno;
		String total_fee = "1";//单位是分，即是0.01元
		String spbill_create_ip = "127.0.0.1";
		String time_start = RandCharsUtils.timeStart();
		System.out.println(time_start);
		String time_expire = RandCharsUtils.timeExpire();
		System.out.println(time_expire);
		String trade_type = "APP";
	  weChatPay("wx5316d5461a19c5dd","1402600302",body,attach,
			 total_fee,spbill_create_ip,"10","127.0.0.1:8888/pay", "node","WeChatHxingHexpay141805144144144");
	  return 0;*/
	  
    try
    {
      int i = String2Json(params,paraval, "SEND_VENDING_DATE", "0");
      if ((i != 0) || 
        (StringUtils.isEmpty(TempVari)))
        Log.info("组发报文失败", new Object[0]);

      JSONObject reqObj = JSON.parseObject(TempVari);
      System.out.println("发送报文：" + reqObj.toString());
      List nv = new ArrayList();
      nv.add(new BasicNameValuePair("INPUT", reqObj.toString()));

      String resJSON = HttpRequestSimple.getInstance().postPramaList(nv, url);

      JSONObject resObj = JSON.parseObject(resJSON);
      System.out.println(resObj.toJSONString());
//      Log.info("响应报文", new Object[] { resObj.toJSONString() });
      String ret_code = resObj.getString("RESPONSECODE");
//      if (!"000000".equals(ret_code)) {
//        String ret_msg = resObj.getString("RESPONSECONTENT");
//        Log.info("vending响应处理失败，错误码：--->%s", new Object[] { ret_code });
//        Log.info("vending响应处理失败，错误信息：--->%s", new Object[] { ret_msg });
//        Etf.setChildValue("ret_code", ret_code);
//        Etf.setChildValue("ret_msg", ret_msg);
//        return 3;
//      }
      String output = resObj.getString("OUTPUT");
//      i = checkSign(output);
//      if (i != 0) {
//        Log.info("vending-响应结果验证签名失败", new Object[0]);
//        return 1;
//      }
//      Log.info("vending-响应结果验证签名成功", new Object[0]);
      String xml = XmlToMap.json2Xml(resJSON, nodeName);
      paringXml(xml);
      return 0;
    }
    catch (NullPointerException e)
    {
      e.printStackTrace();
      Log.info("连接超时", new Object[0]);
      return 9;
    }
    catch (Exception e) {
      e.printStackTrace();
      return -1;
    } finally {
//      Etf.deleteChild("SEND_VENDING_DATE");
//      Etf.deleteChild("SEND_DATE");
    }
  }

  public static void paringXml(String sStr) throws Exception
  {
    sStr = strToUp(sStr);
    if ((sStr != null) && (sStr.length() > 0)) {
      Element eRoot = null; Element eEtf = null;
      try {
        eRoot = DocumentHelper.parseText(sStr).getRootElement();
        eEtf = Etf.peek();
        eEtf.add(eRoot);
      } catch (DocumentException e) {
//        Etf.setChildValue("RSPCOD", "999999");
        Log.info("解析XML 数据失败%s", new Object[] { e.toString() });
      }
    }
  }

  public static String strToUp(String s) throws Exception {
    String strheah = s.substring(0, s.indexOf("?>") + 2);
    Element node = DocumentHelper.parseText(s).getRootElement();
    String strlast = node.asXML().toString();
    Pattern pattern = Pattern.compile("<.+?>");
    StringBuilder res = new StringBuilder();
    int lastIdx = 0;
    Matcher matchr = pattern.matcher(strlast);
    while (matchr.find()) {
      String str = matchr.group();
      res.append(strlast.substring(lastIdx, matchr.start()));
      res.append(str.toUpperCase());
      lastIdx = matchr.end();
    }
    res.append(strlast.substring(lastIdx));
    return strheah + res.toString();
  }
  
  
//  @Named("WeChatPay")
//  public static int weChatPay(@Named("appid") String appid,@Named("mch_id") String mch_id,
//		  @Named("body") String body,@Named("attach") String attach,
//		  @Named("total_fee") String total_fee,@Named("spbill_create_ip") String spbill_create_ip
//		  ,@Named("expire") String expire,@Named("notify_url") String notify_url, @Named("nodeName") String nodeName
//		  ,@Named("ChatKey") String ChatKey){
  public static int weChatPay(String appid,String mch_id,
		  String body,String attach,
		  String total_fee,String spbill_create_ip
		  ,String expire,String notify_url, String nodeName
		  ,String ChatKey){
//		WeixinConfigUtils config = new WeixinConfigUtils();
		
//		String appid = appid;
//		String mch_id = mch_id;
		String nonce_str = RandCharsUtils.getRandomString(16);
//		String body = "Hexpay Test0.01_2";
//		String detail = "0.01Test";
//		String attach = "backup";
		String myTradeno = RandCharsUtils.timeStart()+RandCharsUtils.getRandomString(14);//商户系统内部的订单号
		String out_trade_no = myTradeno;
//		int total_fee = 1;//单位为分
//		String spbill_create_ip = "127.0.0.1";
//		String time_start = RandCharsUtils.timeStart();
//		String time_expire = RandCharsUtils.timeExpire();//订单失效时间，格式为yyyyMMddHHmmss
		Pattern pattern = Pattern.compile("^[0-9]+$");
		if(!pattern.matcher(total_fee).matches() || !pattern.matcher(expire).matches()){
			Log.info("参数格式错误");
			return 1;
		}
	   Date d=new Date();   
	   SimpleDateFormat df=new SimpleDateFormat("yyyyMMddHHmmss");   
	   String time_start = df.format(d);   
	   int temExpire = Integer.parseInt(expire);
	   String time_expire = df.format(new Date(d.getTime() + temExpire * 60 * 1000));  
//		String notify_url = config.notify_url;
		String trade_type = "APP";
		if(temExpire < 5){
			Log.info("订单失效时间必须大于5分钟");
			return 1;
		}
		
		int totalfee = Integer.parseInt(total_fee);
		if(totalfee <= 0){
			Log.info("参数格式错误");
			return 1;
		}
		String recieveMsg = "";
		
		try{
			SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
			parameters.put("appid", appid);
			parameters.put("mch_id", mch_id);
			parameters.put("nonce_str", nonce_str);
			parameters.put("body", body);
			parameters.put("nonce_str", nonce_str);
	//		parameters.put("detail", detail);
			parameters.put("attach", attach);
			parameters.put("out_trade_no", out_trade_no);
			parameters.put("total_fee", totalfee);
			parameters.put("time_start", time_start);
			parameters.put("time_expire", time_expire);
			parameters.put("notify_url", notify_url);
			parameters.put("trade_type", trade_type);
			parameters.put("spbill_create_ip", spbill_create_ip);
			
			String sign = WXSignUtils.createSign("UTF-8", parameters,ChatKey);
			
	
			Unifiedorder unifiedorder = new Unifiedorder();
			unifiedorder.setAppid(appid);
			unifiedorder.setMch_id(mch_id);
			unifiedorder.setNonce_str(nonce_str);
			unifiedorder.setSign(sign);
			unifiedorder.setBody(body);
	//		unifiedorder.setDetail(detail);
			unifiedorder.setAttach(attach);
			unifiedorder.setOut_trade_no(out_trade_no);
			unifiedorder.setTotal_fee(totalfee);
			unifiedorder.setSpbill_create_ip(spbill_create_ip);
			unifiedorder.setTime_start(time_start);
			unifiedorder.setTime_expire(time_expire);
			unifiedorder.setNotify_url(notify_url);
			unifiedorder.setTrade_type(trade_type);
	
			
			String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);
			
			String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			
			String method = "POST";
			
			String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();
			Log.info("返回数据："+weixinPost);
			
			Map<String, String> outputMap = jdomParseXml(weixinPost);
			if(outputMap == null){
				Log.info("返回错误");
				return 3;
			}
			if(!outputMap.get("return_code").equals("SUCCESS")){
				Log.info("签名失败");
				Etf.setChildValue("chat_code", outputMap.get("return_code"));
				Etf.setChildValue("chat_msg", outputMap.get("return_msg"));
				return 4;
			}
			if(!outputMap.get("result_code").equals("SUCCESS")){
				Log.info("请求失败");
				Etf.setChildValue("chat_code", outputMap.get("err_code"));
				Etf.setChildValue("chat_msg", outputMap.get("err_code_des"));
				return 4;
			}
			recieveMsg = JSONUtils.toJsonString(outputMap);
			Log.info("返回数据："+recieveMsg);
			String xml = XmlToMap.json2Xml(recieveMsg, nodeName);
			Log.info("返回数据："+xml);
		    paringXml(xml);
			return 0;
		}catch(Exception e){
			Log.info("错误："+e);
			return 2;
		}
  }
  
  /**
	 * 3、JDOM解析XML
	 * 解析的时候自动去掉CDMA
	 * @param xml
	 */
	@SuppressWarnings("unchecked")
	public static Map jdomParseXml(String xml){
		Map<String, String> outputMap = new HashMap<String, String>();
		try { 
			StringReader read = new StringReader(xml);
			// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
			InputSource source = new InputSource(read);
			// 创建一个新的SAXBuilder
			SAXBuilder sb = new SAXBuilder();
			// 通过输入源构造一个Document
			org.jdom.Document doc;
			doc = (org.jdom.Document) sb.build(source);

			org.jdom.Element root = doc.getRootElement();// 指向根节点
			List<org.jdom.Element> list = root.getChildren();

			if(list!=null&&list.size()>0){
				for (org.jdom.Element element : list) {
					System.out.println("key是："+element.getName()+"，值是："+element.getText());
					outputMap.put(element.getName(), element.getText());
				}
				return outputMap;
			}else{
				return null;
			}
			

		} catch (JDOMException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
  
  
  

  public static void main(String[] args)
  {
	  HxHttp http = new HxHttp();
//	  String params = "test";
//	  String paraval = "test";
//	  String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";
//	  String nodeName="root";
//	  http.HxHttpReq(params,paraval,url, nodeName);
	  
//		String nonce_str = RandCharsUtils.getRandomString(16);
//		System.out.println("随机字符串是："+nonce_str);
//		String body = "Hexpay Test0.01_2";
//		String attach = "backup";
//		String myTradeno = RandCharsUtils.timeStart()+RandCharsUtils.getRandomString(14);
//		String out_trade_no = myTradeno;
//		String total_fee = "1";//单位是分，即是0.01元
//		String spbill_create_ip = "127.0.0.1";
//		String time_start = RandCharsUtils.timeStart();
//		System.out.println(time_start);
//		String time_expire = RandCharsUtils.timeExpire();
//		System.out.println(time_expire);
//		String trade_type = "APP";
//	  http.weChatPay("wx5316d5461a19c5dd","1402600302",body,attach,
//			 total_fee,spbill_create_ip,"10","127.0.0.1:8888/pay", "node","WeChatHxingHexpay141805144144144");
	  
	  HttpRequestSimple httpRequestSimple = HttpRequestSimple.getInstance();
	  String url = "https://sandbox.surecashbd.com/api/payment/process/status/hxpy/7051545894";
	  String body = "";
//	  String resp = httpRequestSimple.postSendHttp(url, body);
	  String resp = httpRequestSimple.getSendHttp(url);
	  System.out.println(resp);
	  
	  
	  
	  
	  
	  
  }
}
