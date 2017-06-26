package weixin.Utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ΢��֧��ǩ��
 */
public class WXSignUtils {
	//http://mch.weixin.qq.com/wiki/doc/api/index.php?chapter=4_3
	//�̻�Key���ĳɹ�˾����ļ���
	//32λ�������õ�ַ��http://www.sexauth.com/  jdex1hvufnm1sdcb0e81t36k0d0f15nc
	private static String aaKey = "";

	/**
	 * ΢��֧��ǩ���㷨sign
	 * @param characterEncoding
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String createSign(String characterEncoding,SortedMap<Object,Object> parameters,String Key){
		StringBuffer sb = new StringBuffer();
		Set es = parameters.entrySet();//���в��봫�εĲ�����accsii��������
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			Object v = entry.getValue();
			if(null != v && !"".equals(v) 
					&& !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + Key);
		System.out.println("�ַ�ƴ�Ӻ��ǣ�"+sb.toString());
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		return sign;
	}

	

}
