package org.corps.bi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SimpleDateFormat 非线程安全，建议在每个方法里，自己new")
public class DateUtils {
	
	public static final String DAY_PATTERN="yyyy-MM-dd";
	
	public static final String DAY_SECONDS_PATTERN="yyyy-MM-dd HH:mm:ss";
	
	public static final String SECONDS_PATTERN="HH:mm:ss";

	public static String format(Date date, String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(date);
	}

	public static String formatSeconds(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_SECONDS_PATTERN);
		return sdf.format(date);
	}

	public static String formatDay(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_PATTERN);
		return sdf.format(date);
	}

	public static Map<String, String> convertTimeToDateStr(String timeStr) {
		SimpleDateFormat DATE_FORMAT_DAY_SECONDS = new SimpleDateFormat(DAY_SECONDS_PATTERN);
		
		Map<String, String> map = new HashMap<String, String>();
		String date = "";
		String time = "";
		try {
			String dateTimeStr = DATE_FORMAT_DAY_SECONDS.format(Long.parseLong(timeStr));
			date = dateTimeStr.substring(0, 10);
			time = dateTimeStr.substring(11);
		} catch (Exception e) {
			SimpleDateFormat DATE_FORMAT_DAY = new SimpleDateFormat(DAY_PATTERN);
			SimpleDateFormat DATE_FORMAT_SECONDS = new SimpleDateFormat(SECONDS_PATTERN);
			date = String.valueOf(DATE_FORMAT_DAY.format(new Date()));
			time = String.valueOf(DATE_FORMAT_SECONDS.format(new Date()));
		}
		map.put("date", date);
		map.put("time", time);
		return map;
	}

	public static boolean isValidTimeStr(String timeStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_SECONDS_PATTERN);
		try {
			sdf.format(Long.parseLong(timeStr));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String verifyTime(String timeStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(SECONDS_PATTERN);
		Date time = new Date();
		try {
			time = sdf.parse(timeStr);
		} catch (ParseException e) {
			return sdf.format(new Date());
		}
		return sdf.format(time);
	}

	public static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_PATTERN);
		return sdf.format(new Date());
	}

	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(SECONDS_PATTERN);
		Date date = new Date();
		return sdf.format(date);
	}

	public static String verifyDate(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_PATTERN);
		try {
			sdf.parse(dateStr);
		} catch (ParseException e) {
			return sdf.format(new Date());
		}
		return dateStr;
	}

	public static boolean verifyDateForSendingData(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(DAY_PATTERN);
		try {
			sdf.parse(dateStr);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

}
