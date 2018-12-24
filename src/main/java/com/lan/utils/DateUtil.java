package com.lan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 
 * @author LAN
 * @date 2018年12月24日
 */
public class DateUtil {
	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(DateUtil.class);   
	/*public static final String DATE_FORMAT_STR = "yyyy-MM-dd";
	public static final String DATE_FORMAT_STR1 = "yyyyMMdd";
	public static final String DATE_FORMAT_STR2 = "yyyy年MM月dd日";
	public static final String DATE_FORMST_STR_TX = "yyMMdd";
	public static final String DATE_FORMST_STR_YEAR2 = "yy";
	public static final String DATE_FORMST_STR_YEAR = "yyyy";
	public static final String DATE_FORMST_STR_TIME = "yyyyMMddHH";
	public static final String DATE_FORMST_STR_ALLTIME = "yyyyMMddHHmmss";
	public static final String DATE_FORMST_STR_TEXT_DATE_TIME_MILLIS = "yyyyMMddHHmmssSSS";
	public static final String DATE_FORMST_STR_TEXT_DATE_TIME_MILLIS2 = "yyyyMMddhhmmssSSS";
	public static final String DATE_FORMST_STR_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMST_STR_DATETIMESIMPLE = "yyyy-MM-dd HH:mm";
	public static final String DATE_FORMST_STR_TIME1 = "HH-mm-ss";
	public static final String DATE_FORMST_STR_TIME2 = "HH:mm:ss";
	public static final String DATE_FORMST_STR_TIME3 = "HHmmss";
	public static final String DAtE_FORMST_STR_TIMESTAMP="yyyy/MM/dd HH:mm:ss";*/
	
	
	/**
	 * 
	 *@description: 格式化日期
	 *@param date 要格式化的日期
	 *@param formatStr 格式化格式
	 *@return 格式化后日期字符串
	 *@since
	 */
	public static String formatDate(Date date, String formatStr) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(formatStr);
		return format.format(date);
	}
	
	/**
	 * 
	 *@description:获取日期
	 *@param dateStr 日期字符串
	 *@param formatStr 日期格式
	 *@return 日期
	 *@since
	 */
	public static Date parseDate(String dateStr, String formatStr) {
		if(StringUtils.isEmpty(dateStr)){
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		return date;
	}
	
	/**
	 * 获取日期月初  00:00:00
	 * @author LAN
	 * @date 2017年5月27日
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar firstCal = Calendar.getInstance();
		firstCal.setTime(date);
		firstCal.set(Calendar.DATE, 1);
		firstCal.set(Calendar.HOUR_OF_DAY, 0);
		firstCal.set(Calendar.MINUTE, 0);
		firstCal.set(Calendar.SECOND, 0);
		return firstCal.getTime();
	}
	
	/**
	 * 
	 *@description:获取一个月的第一天
	 *@param date 要获取的时间
	 *@param formatStr 返回的格式
	 *@return 一个月第一天
	 *@since
	 */
	public static String getFirstDayOfMonth(Date date, String formatStr) {
		Calendar firstCal = Calendar.getInstance();
		firstCal.setTime(date);
		firstCal.set(Calendar.DATE, 1);
		return formatDate(firstCal.getTime(), formatStr);
	}
	
	/**
	 * 
	 *@description:获取一个月的第一天
	 *@param dateStr 要获取的时间字符串
	 *@param formatStr 返回的格式
	 *@return 一个月第一天
	 *@since
	 */
	public static String getFirstDayOfMonth(String dateStr, String formatStr){
		return getFirstDayOfMonth(parseDate(dateStr, formatStr), formatStr);
	}
	
	/**
	 * 
	 *@description:获取一个月的最后一天
	 *@param date 要获取的时间
	 *@param formatStr 返回格式
	 *@return 一个月最后一天
	 *@since
	 */
	public static String getLastDayOfMonth(Date date, String formatStr) {
		Calendar lastCal = Calendar.getInstance();
		lastCal.setTime(date); // 设置日期为当前
		lastCal.add(Calendar.MONTH, 1); // 加一个月
		lastCal.set(Calendar.DATE, 1); // 设为下个月的一号
		lastCal.add(Calendar.DAY_OF_YEAR, -1); // 减一天
		return formatDate(lastCal.getTime(), formatStr);
	}
	
	/**
	 * 
	 *@description:获取一个月的最后一天
	 *@param dateStr 要获取的时间字符串
	 *@param formatStr 返回格式
	 *@return 一个月最后一天
	 *@since
	 */
	public static String getLastDayOfMonth(String dateStr, String formatStr) {
		return getLastDayOfMonth(parseDate(dateStr, formatStr), formatStr);
	}
	
	/**
	 * 
	 *@description: 获取指定日期后N天的日期
	 *@param date 指定日期
	 *@param days 天数
	 *@return 指定日期后N天日期
	 *@since
	 */
	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}
	
	/**
	 * 
	 *@description:对日期进行增加月份操作
	 *@param date 指定日期
	 *@param months 增加的月份，如果要减少输入负值
	 *@return 增加后的日期
	 *@since
	 */
	public static Date addMonths(Date date, int months) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}
    
    public static boolean isValidDate(String inDate,String formate) {
        if (inDate == null)
          return false;
        SimpleDateFormat dateFormat = new SimpleDateFormat(formate);

        if (inDate.trim().length() != dateFormat.toPattern().length())
          return false;

        dateFormat.setLenient(false);

        try {
          dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
          return false;
        }
        return true;
      }


}
