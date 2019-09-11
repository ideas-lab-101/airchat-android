package com.android.crypt.chatapp.utility.Common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DateUtils {

	public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

	public final static String DATETIME_FORMAT_SS = "yyyy-MM-dd HH:mm:ss";

	public final static String DATE_FORMAT_YEAR = "yyyy";
	public final static String DATE_FORMAT_YEAR_MONTH = "yyyy-MM";
	public final static String DATE_FORMAT = "yyyy-MM-dd";

	public final static String DATE_FORMAT_HM = "HH:mm";

	public final static String YYYYMMDD = "yyyyMMdd";

	public final static String YYYYMM = "yyyyMM";

	public final static String DATE_FORMAT_MONTH = "MM";
	public final static String DATE_FORMAT_DD = "dd";

	/**
	 * Get the previous time, from how many days to now.
	 *
	 * @param days
	 *            How many days.
	 * @return The new previous time.
	 */
	public static Date previous(int days) {
		return new Date(System.currentTimeMillis() - days * 3600000L * 24L);
	}

	/**
	 * Convert date and time to string like "yyyyMMdd".
	 */
	public static String formatDateYYYYMMDD(Date d) {
		return new SimpleDateFormat(YYYYMMDD).format(d);
	}

	/**
	 * Convert date and time to string like "yyyyMM".
	 */
	public static String formatDateYYYYMM(Date d) {
		return new SimpleDateFormat(YYYYMM).format(d);
	}

	public static String formatDateYear(Date d) {
		return new SimpleDateFormat(DATE_FORMAT_YEAR).format(d);
	}

	public static String formatDateMonth(Date d) {
		return new SimpleDateFormat(DATE_FORMAT_MONTH).format(d);
	}

	/**
	 * Convert date and time to string like "DD".
	 */
	public static String formatDateDD(Date d) {
		return new SimpleDateFormat(DATE_FORMAT_DD).format(d);
	}

	/**
	 * Convert date and time to string like "yyyy-MM-dd HH:mm".
	 */
	public static String formatDateTime(Date d) {
		return new SimpleDateFormat(DATETIME_FORMAT).format(d);
	}

	public static String formatDateTime(Date d, String formatType) {
		return new SimpleDateFormat(formatType).format(d);
	}

	public static String formatDateTimeSs(Date d) {
		return new SimpleDateFormat(DATETIME_FORMAT_SS).format(d);
	}

	/**
	 * Convert date and time to string like "yyyy-MM-dd HH:mm".
	 */
	public static String formatDateTime(long d) {
		return new SimpleDateFormat(DATETIME_FORMAT).format(d);
	}

	/**
	 * Convert date to String like "yyyy-MM-dd".
	 */
	public static String formatDate(Date d) {
		return new SimpleDateFormat(DATE_FORMAT).format(d);
	}

	/**
	 * Parse date like "yyyy-MM-dd".
	 */
	public static Date parseDate(String d) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(d);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Parse date and time like "yyyy-MM-dd hh:mm".
	 */
	public static Date parseDateTime(String dt) {
		try {
			return new SimpleDateFormat(DATETIME_FORMAT).parse(dt);
		} catch (Exception e) {
		}
		return null;
	}

	public static Date parseDateTimeSs(String dt) {
		try {
			return new SimpleDateFormat(DATETIME_FORMAT_SS).parse(dt);
		} catch (Exception e) {
		}
		return null;
	}

	public static long getLongDateTime()  {
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String s = sdf.format(date);
			Long longDate = Long.parseLong(s);
			return longDate;
		} catch (Exception e) {
			return -1;
		}

	}

	/**
	 * 返回当前是白天还是晚上
	 */
	public static int getCurrentTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String hour= sdf.format(new Date());
		int k  = Integer.parseInt(hour)  ;
		if ((k>=0 && k<6) ||(k >=18 && k<24)){
			return 1; //"evening"
		} else {
			return 2; //"daytime"
		}
	}

	/**
	 * 返回周几
	 * @param date
	 * @return
	 */
	public static String getWeekOfDate(Date date) {
		String[] weekOfDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
		Calendar calendar = Calendar.getInstance();
		if(date != null){
			calendar.setTime(date);
		}
		int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0){
			w = 0;
		}
		return weekOfDays[w];
	}

	/**
	 *
	 * @param begin
	 *            第一个时间
	 * @param end
	 *            第二个时间
	 * @param diffStr
	 *            需要返回时间差精确度,默认为秒,提供以下值(区分大小写)选择"s","m","h","d"
	 * @return
	 */
	public static long dateTimeDiffer(Date begin, Date end, String diffStr) {

		long between = begin.getTime() - end.getTime();
		long day = between/(24*60*60*1000);
		long hour = (between/(60*60*1000)-day*24);
		long min = ((between/(60*1000))-day*24*60-hour*60);
		long second = (between/1000-day*24*60*60-hour*60*60-min*60);

		// System.out.println(""+day1+"天"+hour1+"小时"+minute1+"分"+second1+"秒");
		if (diffStr.equals("m")) {
			return min;
		} else if (diffStr.equals("s")) {
			return second;
		} else if (diffStr.equals("h")) {
			return hour;
		} else if (diffStr.equals("d")) {
			return day;
		} else {
			return second;
		}
	}

	public static long dateDiffer(Date begin, Date end, String diffStr) {

		long between = begin.getTime() - end.getTime();
		long day = between/(24*60*60*1000);
		long hour = (between/(60*60*1000));
		long min = ((between/(60*1000)));
		long second = (between/1000);

		if (diffStr.equals("m")) {
			return min;
		} else if (diffStr.equals("s")) {
			return second;
		} else if (diffStr.equals("h")) {
			return hour;
		} else if (diffStr.equals("d")) {
			return day;
		} else {
			return second;
		}
	}

	public static void main(String[] args) {
		Date date = new Date();
		System.out.println(DateUtils.formatDateTimeSs(date));
		date = DateUtils.offsetDate(date, Calendar.MINUTE, 1);
		System.out.println(DateUtils.formatDateTimeSs(date));

	}

	/*
	 * 返回指定日期相应位移后的日期
	 *
	 * @param date 参考日期
	 *
	 * @param field 位移单位，见 年Calendar.YEAR
	 * ,时，Calendar.HOUR，分，Calendar.MINUTE；秒:Calendar.SECOND
	 *
	 * @param offset 位移数量，正数表示之后的时间，负数表示之前的时间
	 *
	 * @return 位移后的日期
	 */
	public static Date offsetDate(Date date, int field, int offset) {
		Calendar calendar = convert(date);
		calendar.add(field, offset);
		return calendar.getTime();
	}

	private static Calendar convert(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar;
	}

	public static List<Map<String, String>> weekBothMonSun(int dayOfMonth) {
		Calendar calendar = Calendar.getInstance(Locale.CHINESE);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)
				+ dayOfMonth);
		return dateOfMonth(calendar.getTimeInMillis());
	}

	private static List<Map<String, String>> dateOfMonth(long newTime) {
		List<Map<String, String>> mDMList = new ArrayList<Map<String, String>>();
		String dayNames[] = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
		int week1[] = { 7, 1, 2, 3, 4, 5, 6 };
		Calendar c = Calendar.getInstance();// 获得一个日历的实例
		c.setTimeInMillis(newTime);

		int b = week1[c.get(Calendar.DAY_OF_WEEK) - 1];
		if (b != 7) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else {
			c.add(Calendar.WEEK_OF_MONTH, -1);
			c.set(Calendar.DAY_OF_WEEK, 2);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < b; i++) {
			Map<String, String> mMap = new HashMap<String, String>();
			String a = dayNames[c.get(Calendar.DAY_OF_WEEK) - 1];
			mMap.put("week", a);
			mMap.put("ymd", sdf.format(c.getTime()));
			mDMList.add(mMap);
			c.add(Calendar.DATE, 1);
		}
		return mDMList;
	}

	/**
	 * 获得与当前时间的差值，并格式化显示
	 * @param dayTime
	 * @return
	 */
	public static String getDayTime(String dayTime){ //daytime 格式为yyyy-MM-dd HH:mm:ss
		long minutes = 0;
		long days = 0;
		long hours = 0;
		String timeStr = "";
		Date nowDate = null;
		Date createDate = null;
		if (dayTime != null && !"".equals(dayTime)) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				createDate = format.parse(dayTime);
				nowDate  = format.parse(formatDateTime(new Date(),"yyyy-MM-dd HH:mm"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			days = DateUtils.dateTimeDiffer(nowDate, createDate, "d");
			if (days>=1) {
				if (days == 1 ) {
					timeStr = "昨天" + dayTime.substring(10,16);
				}else if (days == 2 ) {
					timeStr = "前天" + dayTime.substring(10,16);
				}else{
					timeStr = dayTime.substring(0,16);
				}
			}else{ //同天之内
				hours = dateTimeDiffer(nowDate, createDate, "h");
				if(hours>=1){
					timeStr = String.valueOf(hours) + "小时前";
				}else { //同小时之内
					minutes = DateUtils.dateTimeDiffer(nowDate, createDate, "m");
					if(minutes>0){
						timeStr = String.valueOf(minutes) + "分钟前";
					}else {
						timeStr = "刚刚";
					}
				}
			}
		}
		return timeStr;
	}

	public static String nowDateTime() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
}
