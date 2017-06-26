package com.hexpay.http;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import tangdi.engine.context.Etf;
import tangdi.engine.context.Log;
import weixin.Utils.HttpXmlUtils;
import weixin.Utils.RandCharsUtils;
import weixin.Utils.WXSignUtils;
import weixin.entity.Unifiedorder;

import com.hexing.pay.oss.common.json.JSONUtils;
import com.hexpay.http.client.utils.XmlToMap;

public class WeChatPay {
	
	/*public static void main(String args[]){
		String appid="wx3968a063225dc510";
		String mch_id="1429362702";
		String body="Electricity recharge";
		String attach="备用字段";
		String total_fee="100";
		String spbill_create_ip="192.168.5.252";
		String expire="10";
		String notify_url="127.0.0.1:8888/pay";
		String nodeName="CHAT_PAY";
		String ChatKey="WeChatHxingHexpay141805144144144";
		String out_trade_no = "D017022300007264";

		weChatPay(appid,mch_id,body,attach,total_fee,spbill_create_ip
				  ,expire,notify_url, nodeName
				  ,out_trade_no,ChatKey);
		
	}*/
	
	@Named("WeChatMD5")
	public static int weChatMD5(@Named("params") String params,@Named("ChatKey") String ChatKey,@Named("nodeName") String nodeName){
		if(params == ""){
			Log.info("参数不能为空");
			return 1;
		}
		String str[] = params.split("\\|");
		SortedMap<Object,Object> CParam = new TreeMap<Object,Object>();
		String key="";
		String value="";
		String debugstr = "";
		String debugstrkey = "";
		for(int i = 0; i<str.length;i++){
			key = str[i];
			value = Etf.getChildValue(key);
			debugstrkey += key;
			debugstr+=value;
			if(value == "" || value==null){
				key="";
				value="";
				continue;
			}else{
				CParam.put(key, value);
				key="";
				value="";
			}
		}
		Log.info("名"+debugstrkey);
		Log.info("值"+debugstr);
		String sign2 = WXSignUtils.createSign("UTF-8", CParam,ChatKey);
		
		Log.info("签名"+sign2);
		Etf.setChildValue(nodeName, sign2); 
		
		return 0;
	}

  @Named("WeChatPay")
  public static int weChatPay(@Named("appid") String appid,@Named("mch_id") String mch_id,
		  @Named("body") String body,@Named("attach") String attach,
		  @Named("total_fee") String total_fee,@Named("spbill_create_ip") String spbill_create_ip
		  ,@Named("expire") String expire,@Named("notify_url") String notify_url, @Named("nodeName") String nodeName
		  , @Named("out_trade_no") String out_trade_no,@Named("ChatKey") String ChatKey){
//  public static int weChatPay(String appid,String mch_id,
//		  String body,String attach,
//		  String total_fee,String spbill_create_ip
//		  ,String expire,String notify_url, String nodeName
//		  ,String ChatKey){
//		WeixinConfigUtils config = new WeixinConfigUtils();
		
//		String appid = appid;
//		String mch_id = mch_id;
		String nonce_str = RandCharsUtils.getRandomString(16);
//		String body = "Hexpay Test0.01_2";
//		String detail = "0.01Test";
//		String attach = "backup";
//		String myTradeno = RandCharsUtils.timeStart()+RandCharsUtils.getRandomString(14);//商户系统内部的订单号
//		String out_trade_no = myTradeno;
//		int total_fee = 1;//单位为分
//		String spbill_create_ip = "127.0.0.1";
//		String time_start = RandCharsUtils.timeStart();
//		String time_expire = RandCharsUtils.timeExpire();//订单失效时间，格式为yyyyMMddHHmmss
		Pattern pattern = Pattern.compile("^[0-9]+$");
		if(!pattern.matcher(total_fee).matches() || !pattern.matcher(expire).matches()){
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
			return 1;
		}
		
		int totalfee = Integer.parseInt(total_fee);
		if(totalfee <= 0){
			return 1;
		}
		String recieveMsg = "";
		
		try{
			SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
			parameters.put("appid", appid);
			parameters.put("mch_id", mch_id);
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
			
			String sign1 = WXSignUtils.createSign("UTF-8", parameters,ChatKey);
			
	
			Unifiedorder unifiedorder = new Unifiedorder();
			unifiedorder.setAppid(appid);
			unifiedorder.setMch_id(mch_id);
			unifiedorder.setNonce_str(nonce_str);
			unifiedorder.setSign(sign1);
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
			
//			String wxUrl = "https://101.226.129.200:443/pay/unifiedorder";
			String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			
			String method = "POST";
			
			String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();
			
			Map<String, String> outputMap = jdomParseXml(weixinPost);
			if(outputMap == null){
				return 3;
			}
			if(!outputMap.get("return_code").equals("SUCCESS")){
				Etf.setChildValue("chat_code", outputMap.get("return_code"));
				Etf.setChildValue("chat_msg", outputMap.get("return_msg"));
				return 4;
			}
			if(!outputMap.get("result_code").equals("SUCCESS")){
				Etf.setChildValue("chat_code", outputMap.get("err_code"));
				Etf.setChildValue("chat_msg", outputMap.get("err_code_des"));
				return 4;
			}
			//验证签名
			SortedMap<Object,Object> CParam = new TreeMap<Object,Object>();
			CParam.put("appid", outputMap.get("appid"));
			CParam.put("mch_id", outputMap.get("mch_id"));
			CParam.put("nonce_str", outputMap.get("nonce_str"));
			CParam.put("result_code", outputMap.get("result_code"));
			CParam.put("return_msg", outputMap.get("return_msg"));
			CParam.put("prepay_id", outputMap.get("prepay_id"));
			CParam.put("return_code", outputMap.get("return_code"));
			CParam.put("trade_type", outputMap.get("trade_type"));
			
			String sign2 = WXSignUtils.createSign("UTF-8", CParam,ChatKey);
			System.out.println("验证签名是："+sign2);
			if(!sign2.equalsIgnoreCase(outputMap.get("sign"))){
				return 5;
			}
			Date date = new Date();
			long timeStamp= (date.getTime())/1000;
			String newnonceStr = RandCharsUtils.getRandomString(32).toLowerCase();
			
			Map<String,String> realMap = new HashMap<String, String>();
			realMap.put("appId",outputMap.get("appid") );
			realMap.put("partnerId", outputMap.get("mch_id"));
			realMap.put("prepayId", outputMap.get("prepay_id"));
			realMap.put("nonceStr", newnonceStr);
			realMap.put("timeStamp", timeStamp+"");
			realMap.put("packageValue","Sign=WXPay");
//			realMap.put("extData","app data");
			
			SortedMap<Object,Object> realParam = new TreeMap<Object,Object>();
			realParam.put("appid", realMap.get("appId"));
			realParam.put("partnerid", realMap.get("partnerId"));
			realParam.put("prepayid", realMap.get("prepayId"));
			realParam.put("noncestr", realMap.get("nonceStr"));
			realParam.put("timestamp", realMap.get("timeStamp"));
			realParam.put("package", realMap.get("packageValue"));
//			realParam.put("extData", realMap.get("extData"));
			String sign = WXSignUtils.createSign("UTF-8", realParam,ChatKey);
			realMap.put("sign", sign);
			
			recieveMsg = JSONUtils.toJsonString(realMap);
			String xml = XmlToMap.json2Xml(recieveMsg, nodeName);
			System.out.println(xml);
		    paringXml(xml);
			return 0;
		}catch(Exception e){
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
//	        Etf.setChildValue("RSPCOD", "999999");
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
}
