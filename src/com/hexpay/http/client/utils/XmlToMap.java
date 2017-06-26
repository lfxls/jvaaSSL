package com.hexpay.http.client.utils;

import com.alibaba.fastjson.JSON;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XmlToMap
{
  public static String json2Xml(String sJson, String nodeName)
  {
    Map json = null;
    try {
      json = JSON.parseObject(sJson);
    } catch (Exception e) {
      e.printStackTrace();
      return "<?xml version=\"1.0\" encoding=\"utf-8\"?><" + nodeName + "></" + nodeName + ">";
    }

    Element root = DocumentHelper.createElement(nodeName);
    map2Xml(json, root);
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + root.asXML();
  }

  private static void map2Xml(Map<String, Object> map, Element root) {
    for (Map.Entry obj : map.entrySet()) {
      Object o = obj.getValue();
      if ((o instanceof Map)) {
        Map map2 = (Map)o;
        Element mapElement = root.addElement((String)obj.getKey());
        map2Xml(map2, mapElement);
      } else if ((o instanceof List)) {
        List lists = (List)o;
        for (int i=0;i<lists.size();i++) {
        	Object listObj=lists.get(i);
        	if ((listObj instanceof Map)) {
		          Element listE = root.addElement((String)obj.getKey());
		          map2Xml((Map)listObj, listE);
        	}else {  
        		 root.addElement((String)obj.getKey()).setText(String.valueOf(obj.getValue()));
            }  
        }
      } else {
        root.addElement((String)obj.getKey()).setText(String.valueOf(obj.getValue()));
      }
    }
  }
}