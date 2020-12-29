package org.corps.bi.utils;

import java.security.MessageDigest;

public class MD5Utils {
	
	public final static String MD5(String source) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = source.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
          throw new RuntimeException("md5 for "+ source+" meet error!");
        }
    }
	
	public static void main(String[] args) {
		String r="/usr/local/services/apache-tomcat/com/hoolai/service/";
		System.out.println(MD5(r));
		System.out.println(MD5(r+"sango1/"));
		System.out.println(MD5(r+"sango2/"));
		System.out.println(MD5Utils.class.getClassLoader().getResource("").getPath());
		System.out.println(MD5Utils.class.getClassLoader().getResource("").getFile());
		System.out.println(MD5Utils.class.getClassLoader().getResource("").getUserInfo());
		System.out.println(MD5Utils.class.getClassLoader().getResource("/"));
		System.out.println(ClassLoader.getSystemResource(""));  
		 System.out.println(Thread.currentThread().getContextClassLoader().getResource(""));  
	}

}
