package cn.tcl.music.widget.lyric;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * describe the lyric line
 * @author douzifly
 *
 */
public class LrcRow implements Comparable<LrcRow>{
    public final static String TAG = DefaultLrcBuilder.TAG;
    
    /** begin time of this lrc row */
    public long time;
    /** content of this lrc */
    public String content;
    
    public String strTime;
    
    public LrcRow(){}
    
    public LrcRow(String strTime,long time,String content){
        this.strTime = strTime;
        this.time = time;
        this.content = content;
    }
    
    /**
     *  create LrcRows by standard Lrc Line , if not standard lrc line,
     *  return false<br />
     *  [00:00:20] balabalabalabala
     */
    public static List<LrcRow> createRows(String standardLrcLine){
        try{
            int leftIndex = standardLrcLine.indexOf("[") ;
            int rightIndex = standardLrcLine.indexOf("]") ;
            //用的歌词格式是[02:22.033]  有的是[02:22.03]  也就是说最后一个]有的是9 有的是10
            if(leftIndex != 0 || (rightIndex != 9 && rightIndex != 10)){
                NLog.e(TAG, "no [ and ]");
                return null;
            }
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length());
            String times = standardLrcLine.substring(0,lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");

            String arrTimes[] = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for(String temp : arrTimes){
                if(temp.trim().length() == 0){
                    continue;
                }
                long time = timeConvert(temp);
                if (time != -1){
                    LrcRow lrcRow = new LrcRow(temp, timeConvert(temp), content);
                    listTimes.add(lrcRow);
                }
            }
            return listTimes;
        }catch(Exception e){
            NLog.e(TAG,"createRows exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static long timeConvert(String timeString){
//        NLog.d(TAG, "timeConvert timeString = " +timeString);
        timeString = timeString.replace('.', ':');
        String[] times = timeString.split(":");
        try {
            if (times.length == 3) {
                return Integer.valueOf(times[0]) * 60 * 1000 +
                        Integer.valueOf(times[1]) * 1000 +
                        Integer.valueOf(times[2]);
            }
        }catch (Exception e){
            //代表解析错误
            NLog.e(TAG,"timeConvert exception:" + e.getMessage());
            e.printStackTrace();
        }
        return -1;
        // mm:ss:SS

    }

    public int compareTo(LrcRow another) {
        return (int)(this.time - another.time);
    }

    @Override
    public String toString() {
        return "LrcRow{" +
                "time=" + time +
                ", content='" + content + '\'' +
                ", strTime='" + strTime + '\'' +
                '}';
    }
}