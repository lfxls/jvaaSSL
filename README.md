# jvaaSSL
java客户端https（SSL） 单向认证 测试程序

## 1、keytool生成证书：
生成服务端证书：
keytool -genkey -v -alias tomcat -keyalg RSA -keystore tomcat.keystore -dname "CN=127.0.0.1,OU=zlj,O=zlj,L=Peking,ST=Peking,C=CN" -validity 3650
生成客户端证书：
keytool -genkey -v -alias client -keyalg RSA -keystore client.keystore -dname "CN=client,OU=zlj,O=zlj,L=Peking,ST=Peking,C=CN" -validity 3650
导出客户端证书：
keytool -export -alias client -keystore client.keystore  -storepass 123456 -rfc -file client.cer
把客户端证书加入服务端证书信任列表：
keytool -import -alias client -v -file client.cer -keystore tomcat.keystore
导出服务端证书 ：
keytool -export -alias tomcat -keystore tomcat.keystore -storepass 123456 -rfc -file tomcat.cer
生成客户端信任列表 ：
keytool -import -file tomcat.cer -storepass 123456 -keystore client.truststore -alias tomcat -noprompt

## 2、生成证书后服务端配置：
tomcat服务器 server.xml添加（修改路径、密码）：
<Connector SSLEnabled="true" acceptCount="100" clientAuth="false" disableUploadTimeout="true" enableLookups="true" 
keystoreFile="D:/cer/tomcat/server.keystore" keystorePass="123456" maxSpareThreads="75" maxThreads="200" minSpareThreads="5"
port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" scheme="https" secure="true" sslProtocol="TLS"/>
重启服务 启用端口8443

## 3、客户端配置
证书加载：
      KeyStore trustStore = KeyStore.getInstance("JKS");
      InputStream inputStream = new FileInputStream("E:/keytool/client.truststore");
      trustStore.load(inputStream, "123456".toCharArray());
      sf = new MySSLSocketFactory(trustStore);
      sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
