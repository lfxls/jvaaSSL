package com.hexpay.http.client.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class VendingUtil
{
  public static boolean isnull(String str)
  {
    return (str == null) || (str.equalsIgnoreCase("null")) || (str.equals(""));
  }

  public static String getCurrentDateTimeStr()
  {
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    Date date = new Date();
    String timeString = dataFormat.format(date);
    return timeString;
  }

  public static String getIpAddr(HttpServletRequest request)
  {
    String ip = request.getHeader("x-forwarded-for");
    if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
    {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
    {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
    {
      ip = request.getRemoteAddr();
    }
    if ((!isnull(ip)) && (ip.contains(",")))
    {
      String[] ips = ip.split(",");
      ip = ips[(ips.length - 1)];
    }
    return ip;
  }

  public static String genSignData(JSONObject jsonObject)
  {
    StringBuffer content = new StringBuffer();

    List keys = new ArrayList(jsonObject.keySet());
    Collections.sort(keys);
    for (int i = 0; i < keys.size(); i++)
    {
      String key = (String)keys.get(i);
      if ("sign".equals(key))
      {
        continue;
      }
      String value = jsonObject.getString(key);

      if (isnull(value))
      {
        continue;
      }
      content.append((i == 0 ? "" : "&") + key + "=" + value);
    }

    String signSrc = content.toString();
    if (signSrc.startsWith("&"))
    {
      signSrc = signSrc.replaceFirst("&", "");
    }
    return signSrc;
  }

  public static String Json2String(JSONObject jsonObject)
  {
    StringBuffer content = new StringBuffer();

    List keys = new ArrayList(jsonObject.keySet());
    Collections.sort(keys);
    for (int i = 0; i < keys.size(); i++)
    {
      String key = (String)keys.get(i);
      String value = jsonObject.getString(key);

      if (isnull(value))
      {
        continue;
      }
      content.append((i == 0 ? "" : "&") + key + "=" + value);
    }

    String signSrc = content.toString();
    if (signSrc.startsWith("&"))
    {
      signSrc = signSrc.replaceFirst("&", "");
    }
    return signSrc;
  }


  public static String readReqStr(HttpServletRequest request)
  {
    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();
    try
    {
      reader = new BufferedReader(
        new InputStreamReader(request
        .getInputStream(), "utf-8"));
      String line = null;

      while ((line = reader.readLine()) != null)
      {
        sb.append(line);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      try
      {
        if (reader != null)
        {
          reader.close();
        }
      }
      catch (IOException localIOException1)
      {
      }
    }
    finally
    {
      try
      {
        if (reader != null)
        {
          reader.close();
        }
      }
      catch (IOException localIOException2)
      {
      }
    }
    return sb.toString();
  }
}