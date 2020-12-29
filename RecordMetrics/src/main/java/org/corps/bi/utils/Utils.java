package org.corps.bi.utils;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static String getDate() {
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		return sf.format(date);
	}

	public static String getTime() {
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("hh:mm:ss");
		return sf.format(date);
	}

	public static long IP2Long(String IP) {
		long f1, f2, f3, f4;
		String tokens[] = IP.split("\\.");
		if (tokens.length != 4)
			return -1;
		try {
			f1 = Long.parseLong(tokens[0]) << 24;
			f2 = Long.parseLong(tokens[1]) << 16;
			f3 = Long.parseLong(tokens[2]) << 8;
			f4 = Long.parseLong(tokens[3]);
			return f1 + f2 + f3 + f4;
		} catch (Exception e) {
			return -1;
		}
	}

	public static String numToIP(long ip) {
		StringBuilder sb = new StringBuilder();
		for (int i = 3; i >= 0; i--) {
			sb.append((ip >>> (i * 8)) & 0x000000ff);
			if (i != 0) {
				sb.append('.');
			}
		}
		// System.out.println(sb);
		return sb.toString();
	}

	public static String char_replace(String chars) {
		return chars != null && chars.length() > 0 ? chars.toLowerCase()
				.replaceAll("[\"\'\\\\\\s#&]", "") : "";
	}

	public static String CutDownCreative(String creative) {
		return creative.length() >= 100 ? creative.substring(0, 100) : creative;
	}

	public static String convertUid(String openId) {
		String uid = new BigInteger(openId, 16).toString(10);
		System.out.println(uid);
		return null;
	}

	public static String convert16Uid(String id) {
		String uid = new BigInteger(id, 10).toString(16);
		System.out.println(uid.toUpperCase());
		return null;
	}

}
