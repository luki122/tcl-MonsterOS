package com.monster.netmanage.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.monster.netmanage.R;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * 字符串工具类
 * 
 * @author zhaolaichao
 *
 */
public class StringUtil {

	/**
	 * 字节转换分隔
	 */
	public static final String DATA_DIVIDER_TAG = " ";
	/**
	 * 距离月结日还有几天
	 * 
	 * @param monthEndDay
	 * @return
	 */
	public static int getDaysToMonthEndDay(int monthEndDay) {
		int days = 0;
		Calendar calendar = Calendar.getInstance();
		// 当前日期
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		Log.e("StringUtil", "StringUtil--day>>>>" + currentDay);
		// 当前月最后一天
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(calendar.getTime());
		int lastDay = Integer.parseInt(date.substring(date.lastIndexOf("-") + 1));
		// 月结日
		if (lastDay >= monthEndDay) {
			// 当前最后一天大于或等于月结日
			days = monthEndDay - currentDay - 1;
			if (days < 0) {
				// 计算距下个月的月结日的时间
				days = (lastDay - currentDay) + monthEndDay - 1;
			}
		} else {
			// 当前最后一天小于等于月结日
			days = lastDay - currentDay -1;
		}
		return days;
	}

	/**
	   *从月结日到当前一共多少天
	   * @param day
	   * @return
	   */
	public static int getDaysByCloseDay(int day) {
		 Calendar calendar = Calendar.getInstance();
		 int now = calendar.get(Calendar.DAY_OF_MONTH);
		 if (now < day) {
			 calendar.add(Calendar.MONTH, -1);
		 } else {
			 calendar.add(Calendar.MONTH, 0);
		 }
		 calendar.set(Calendar.DAY_OF_MONTH, day);
		 calendar.set(Calendar.HOUR_OF_DAY, 0);
		 calendar.set(Calendar.MINUTE, 0);
		 long startCloseDayTime = calendar.getTimeInMillis();
		 calendar = Calendar.getInstance();
		 calendar.set(Calendar.HOUR_OF_DAY, 23);
		 calendar.set(Calendar.MINUTE, 59);
		 long endCloseDayTime = calendar.getTimeInMillis();
		 int days = (int) ((endCloseDayTime - startCloseDayTime) / (24 * 60 * 60 * 1000));
		 return (days + 1);
	}
	
	
	/**
	 * 保留两位小数 除法
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public static double getDivisionDouble(int number1, int number2) {
		DecimalFormat df = new DecimalFormat("0.00");// 格式化小数，不足的补0
		String numStr = df.format((double) number1 / number2);// 返回的是String类型的
		double num = Double.parseDouble(numStr);
		return num;
	}

	/**
	 * 将时间戳转为代表"距现在多久之前"的字符串
	 * 
	 * @param timeStr  时间戳
	 * @return
	 */
	public static String getStandardDate(String timeStr) {

		StringBuffer sb = new StringBuffer();

		long t = Long.parseLong(timeStr);
		long time = System.currentTimeMillis() - t;
		long mill = (long) Math.ceil(time / 1000);// 秒前

		long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前

		long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时

		long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

		if (day - 1 > 0) {
			sb.append(day + "天");
		} else if (hour - 1 > 0) {
			if (hour >= 24) {
				sb.append("1天");
			} else {
				sb.append(hour + "小时");
			}
		} else if (minute - 1 > 0) {
			if (minute == 60) {
				sb.append("1小时");
			} else {
				sb.append(minute + "分钟");
			}
		} else if (mill - 1 > 0) {
			if (mill == 60) {
				sb.append("1分钟");
			} else {
				sb.append("刚刚");
			}
		} else {
			sb.append("刚刚");
		}
		if (!sb.toString().equals("刚刚")) {
			sb.append("前");
		}
		return sb.toString();
	}
	
	/**
	 * 字节单位转换
	 * @param context
	 * @param kBytes  KB为单位
	 * @return  
	 */
	  public static String formatDataFlowSize(Context context, float kBytes) {
	        Resources res = context.getResources();
	         float result = kBytes;
	        int suffix = R.string.kilobyte_short;
//	        long mult = 1;
	        if (result > 900) {
	            suffix = R.string.megabyte_short;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = R.string.gigabyte_short;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = R.string.terabyte_short;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = R.string.petabyte_short;
	            result = result / 1024;
	        }
	        // Note we calculate the rounded long by ourselves, but still let String.format()
	        // compute the rounded value. String.format("%f", 0.1) might not return "0.1" due to
	        // floating point errors.
	        DecimalFormat format = new DecimalFormat("#0.##");

	        final String roundedString = format.format(result);

	        // Note this might overflow if result >= Long.MAX_VALUE / 100, but that's like 80PB so
	        // it's okay (for now)...

	        final String units = res.getString(suffix);

	        return roundedString + DATA_DIVIDER_TAG + units;
	  }

	/**
	 * 字节单位转换　返回整数
	 * @param context
	 * @param kBytes
     * @return
     */
	public static String formatIntDataFlowSize(Context context, float kBytes) {
		Resources res = context.getResources();
		float result = kBytes;
		int suffix = R.string.kilobyte_short;
//	        long mult = 1;
		if (result > 900) {
			suffix = R.string.megabyte_short;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = R.string.gigabyte_short;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = R.string.terabyte_short;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = R.string.petabyte_short;
			result = result / 1024;
		}

		final String roundedString = "" + (int)result;
		final String units = res.getString(suffix);
		return roundedString + DATA_DIVIDER_TAG + units;
	}
	
	/**
	 * 字节单位转换　float
	 * @param context
	 * @param Bytes
     * @return
     */
	public static String formatFloatDataFlowSize(Context context, long sizeBytes) {
		 float result = sizeBytes;
	        int suffix = com.android.internal.R.string.byteShort;
	        if (result > 900) {
	            suffix = com.android.internal.R.string.kilobyteShort;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = com.android.internal.R.string.megabyteShort;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = com.android.internal.R.string.gigabyteShort;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = com.android.internal.R.string.terabyteShort;
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = com.android.internal.R.string.petabyteShort;
	            result = result / 1024;
	        }

	    DecimalFormat format = new DecimalFormat("#0.#");
	    final String roundedString = format.format(result);
		final String units = context.getString(suffix);
		return roundedString + DATA_DIVIDER_TAG + units;
	}
	
	/**
	 * 字节单位转换　返回float
	 * @param context
	 * @param kBytes
     * @return
     */
	public static String formatFloatDataFlowSizeByKB(Context context, float kBytes) {
		Resources res = context.getResources();
		float result = kBytes;
		int suffix = R.string.kilobyte_short;
//	        long mult = 1;
		if (result > 1000) {
			suffix = R.string.megabyte_short;
			result = result / 1024;
		}
		if (result > 1000) {
			suffix = R.string.gigabyte_short;
			result = result / 1024;
		}
		if (result > 1000) {
			suffix = R.string.terabyte_short;
			result = result / 1024;
		}
		if (result > 1000) {
			suffix = R.string.petabyte_short;
			result = result / 1024;
		}
	    // Note we calculate the rounded long by ourselves, but still let String.format()
        // compute the rounded value. String.format("%f", 0.1) might not return "0.1" due to
        // floating point errors.
        DecimalFormat format = new DecimalFormat("#0.##");
        String roundedString = format.format(result).trim();
        if (roundedString.contains(".")) {
        	//最多显示3位有效值
        	String split = roundedString.substring(0, roundedString.indexOf("."));
        	String split1 = roundedString.substring(roundedString.indexOf(".") + 1);
        	 System.out.println("结果::11:::" + split + "<>>>"+split1);
        	if (split.length() >= 3) {
        		roundedString = split.substring(0, 3);
        	} else {
        		if ((split1 + split ).length() > 3 ) {
        			int end = 3 - split.length();
        			roundedString = split + "." + split1.substring(0, end);
        		}
        	}
        }
		final String units = res.getString(suffix);
		return roundedString + DATA_DIVIDER_TAG + units;
	}
	
	/**
	 * 当前月天数
	 * @return
	 */
	public static int getMonthDays() {
		Calendar calendar = Calendar.getInstance();  
		int day = calendar.getActualMaximum(Calendar.DATE);
		return day;
	}
	
	/**
	 * 获得当前天的起始时间
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	  public static  long getStartTime(int hour, int minute, int second){  
	        Calendar todayStart = Calendar.getInstance();  
	        todayStart.set(Calendar.HOUR_OF_DAY, hour);  
	        todayStart.set(Calendar.MINUTE, minute);  
	        todayStart.set(Calendar.SECOND, second);  
	        todayStart.set(Calendar.MILLISECOND, 0);  
	        return todayStart.getTimeInMillis();  
	  } 
	  
	  /**
	   * 获得当前天的结束时间
	   * @param hour
	   * @param minute
	   * @param second
	   * @return
	   */
	  public static long getEndTime(int hour, int minute, int second){  
	        Calendar todayEnd = Calendar.getInstance();  
	        todayEnd.set(Calendar.HOUR_OF_DAY, hour);  
	        todayEnd.set(Calendar.MINUTE, minute);  
	        todayEnd.set(Calendar.SECOND, second);  
	        todayEnd.set(Calendar.MILLISECOND, 999);  
	        return todayEnd.getTimeInMillis();  
	 }
	  
	  /**
	   * {@link Description} 用于月结日 
	   * 获得当前月某一天
	   * @return
	   */
	  public static long getDayByMonth(int day){  
		  Calendar calendar = Calendar.getInstance();
		  calendar.add(Calendar.MONTH, 0);
		  calendar.set(Calendar.DAY_OF_MONTH, day);
		  return calendar.getTimeInMillis();
	 }
	  
	  /**
	   * {@link Description} 用于月结日 
	   * 获得下个月某一天的前一天
	   * @return
	   */
	  public static long getDayByNextMonth(int day){  
		  Calendar calendar = Calendar.getInstance();
		  calendar.add(Calendar.MONTH, 1);
		  calendar.set(Calendar.DAY_OF_MONTH, day - 1);
		  return calendar.getTimeInMillis();
	 }
	  
	  /**
	   *{@link Description} 用于月结日 
	   * 获得当前月某一天的某一时刻
	   * @param day
	   * @param hour
	   * @param minute
	   * @return
	   */
	  public static long getDayByMonth(int day, int hour, int minute){  
		  Calendar calendar = Calendar.getInstance();
		  calendar.add(Calendar.MONTH, 0);
		  calendar.set(Calendar.DAY_OF_MONTH, day);
		  calendar.set(Calendar.HOUR_OF_DAY, hour);
		  calendar.set(Calendar.MINUTE, minute);
		  return calendar.getTimeInMillis();
	 }
	  
	  /**
	   * {@link Description} 用于月结日 
	   * 获得下个月某一天的前一天的某一时刻
	   * @param day
	   * @param hour
	   * @param minute
	   * @return
	   */
	  public static long getDayByNextMonth(int day, int hour, int minute){  
		  Calendar calendar = Calendar.getInstance();
		  calendar.add(Calendar.MONTH, 1);
		  calendar.set(Calendar.DAY_OF_MONTH, day - 1);
		  calendar.set(Calendar.HOUR_OF_DAY, hour);
		  calendar.set(Calendar.MINUTE, minute);
		  return calendar.getTimeInMillis();
	 }
	  /**
	   * 获得当前月最后一天
	   * @return
	   */
	  public static long getLastDayByMonth() {
		  Calendar calendar = Calendar.getInstance();
		  calendar = Calendar.getInstance();
		  calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		  return calendar.getTimeInMillis();
	  }
	  
	  /**
	   * 根据月结日来获得一天中的某时刻
	   * @param day
	   * @param hour
	   * @param minute
	   * @param millisecond
	   * @return
	   */
	  public static long getDayByCloseDay(int day, int hour, int minute, int millisecond){  
		  Calendar calendar = Calendar.getInstance();
		  int now = calendar.get(Calendar.DAY_OF_MONTH);
		   if (now < day) {
		        calendar.add(Calendar.MONTH, -1);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String date = format.format(calendar.getTime());
				int lastDay = Integer.parseInt(date.substring(date.lastIndexOf("-") + 1));
				if (day > lastDay) {
					calendar = Calendar.getInstance();
					calendar.add(Calendar.MONTH, 0);
					day = day - lastDay;
				}
		   } else {
		      calendar.add(Calendar.MONTH, 0);
		   }
		  calendar.set(Calendar.DAY_OF_MONTH, day);
		  calendar.set(Calendar.HOUR_OF_DAY, hour);
		  calendar.set(Calendar.MINUTE, minute);
		  calendar.set(Calendar.MILLISECOND, millisecond);
		  return calendar.getTimeInMillis();
	 }
	  /**
	   * 是否为整数
	   * @param context 输入内容
	   * @return
	   */
	  public static boolean matchNumber(String context) {
		  Pattern p = Pattern.compile("[0-9]*"); 
		  Matcher m = p.matcher(context);
		  return m.matches();
	  }
	  
	  /**
	   * 保留一位小数
	   * @param data
	   * @return
	   */
	  public static String formatString(float data) {
		  return String .format("%.1f", data);
	  }
}
