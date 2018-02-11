package com.mst.thememanager.utils;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DensityUtils {

	/**
     * Standard quantized DPI for low-density screens.
     */
    public static final int DENSITY_LOW = 120;

    /**
     * Standard quantized DPI for medium-density screens.
     */
    public static final int DENSITY_MEDIUM = 160;


    /**
     * Standard quantized DPI for high-density screens.
     */
    public static final int DENSITY_HIGH = 240;

    /**
     * Standard quantized DPI for extra-high-density screens.
     */
    public static final int DENSITY_XHIGH = 320;


    /**
     * Standard quantized DPI for extra-extra-high-density screens.
     */
    public static final int DENSITY_XXHIGH = 480;


    /**
     * Standard quantized DPI for extra-extra-extra-high-density screens.  Applications
     * should not generally worry about this density; relying on XHIGH graphics
     * being scaled up to it should be sufficient for almost all cases.  A typical
     * use of this density would be 4K television screens -- 3840x2160, which
     * is 2x a traditional HD 1920x1080 screen which runs at DENSITY_XHIGH.
     */
    public static final int DENSITY_XXXHIGH = 640;
	
    public static final int DENSITY_DIR_COUNT = 6;
    
    private static HashMap<Integer, String> mDrawableDirForDensity = new HashMap<Integer, String>();
    private static HashMap<Integer,Integer> mDensityIndex = new HashMap<Integer, Integer>();
    
    private static DensityInfo[] densityInfos = new DensityInfo[DENSITY_DIR_COUNT];
    public static class DensityInfo{
    	public int index;
    	public int density;
    	public String dir;
    	public DensityInfo(int index,int density,String dir){
    		this.index = index;
    		this.density = density;
    		this.dir = dir;
    	}
    }
    
    
    static{
    	
    	densityInfos[0] = new DensityInfo(0,DENSITY_HIGH,"drawable-hdpi/");
    	densityInfos[1] = new DensityInfo(1,DENSITY_LOW,"drawable-ldpi/");
    	densityInfos[2] = new DensityInfo(2,DENSITY_MEDIUM,"drawable-mdpi/");
    	densityInfos[3] = new DensityInfo(3,DENSITY_XHIGH,"drawable-xhdpi/");
    	densityInfos[4] = new DensityInfo(4,DENSITY_XXHIGH,"drawable-xxhdpi/");
    	densityInfos[5] = new DensityInfo(5,DENSITY_XXXHIGH,"drawable-xxxhdpi/");
    	
    }
	
    public static int getBestDensityIndex(int density){
    	for(int i = DENSITY_DIR_COUNT -1;i >= 0;i--){
			DensityInfo info = densityInfos[i];
			if(info.density == density){
				return info.index;
			}
		}
		return-1;
    }
    
    public static int getDensityByIndex(int index){
    	return densityInfos[index].density;
    }
    
    
    
	public static String getMatchedDrawableDir(int density){
		for(int i = DENSITY_DIR_COUNT -1;i >= 0;i--){
			DensityInfo info = densityInfos[i];
			if(info.density == density){
				return info.dir;
			}
		}
		return "";
	}
	
	

	public static int getBestDensity() {
		// TODO Auto-generated method stub
		return DisplayMetrics.DENSITY_DEVICE_STABLE;
	}
	
	/**
	 * parse dimen from value.xml
	 * @param dimen
	 * @param res
	 * @return
	 */
	public static float parseDimen(String dimen,Resources res){
		int length = dimen.length();
		int index  = -1;
		for(int i = 0 ;i<length;i++){
			char ch = dimen.charAt(i);
			if((ch > 'a' && ch < 'z') || (ch > 'A' && ch < 'Z')){
				index = i;
				break;
			}
		}
		
		String unitStr = dimen.substring(index, length);
		String value = dimen.substring(0,index);
		int unit = -1;
		if("px".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_PX;
		}else if("dp".equals(unitStr) || "dip".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_DIP;
		}else if("sp".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_SP;
		}else if("pt".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_PT;
		}else if("in".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_IN;
		}else if("mm".equals(unitStr)){
			unit = TypedValue.COMPLEX_UNIT_MM;
		}
		
		return TypedValue.applyDimension(unit, Float.parseFloat(value), res.getDisplayMetrics());
		
		
	}
	
	/**
	 * parse color from value.xml
	 * @param color
	 * @return
	 */
	public static int parseColor(String color){
		try{
			return Color.parseColor(color);
		}catch(Exception ex){
			return 0;
		}
		
	}
	
	
	public static Integer parseInteger(String integer){
		try{
			return Integer.parseInt(integer);
		}catch(Exception ex){
			return 0;
		}
	}
	
	public static boolean parseBool(String bool){
		try{
			return Boolean.parseBoolean(bool);
		}catch(Exception ex){
			return false;
		}
	}
	
	
	

}
