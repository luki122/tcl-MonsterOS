package com.android.gallery3d.data;

/*
 * This file is added by ShenQianfeng 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import android.graphics.Point;
import android.text.TextUtils;

import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.ui.SlotView.Spec;
import com.android.gallery3d.util.LogUtil;

public class DateGroupInfos {

    public static final String TAG = "DateGroupInfos";
    private static final long serialVersionUID = 6994626284889969118L;
    
    //private ReentrantLock mLock = new ReentrantLock();
    private static final int INDEX_NONE = -1;
    
    // we use Point.x  to indicate upper of the screen, Point.y to indicate lower of the screen;
    private ArrayList<Point> mDateGroupBounds = new ArrayList<Point>();
    private ArrayList<Point> mMonthGroupBounds = new ArrayList<Point>();

    private ArrayList<String> mDateKeyList;
    private HashMap<String, GroupInfo> mDateValueMap;
    
    private ArrayList<String> mMonthKeyList;
    private HashMap<String, GroupInfo> mMonthValueMap;
    
    public static final int MODE_DATE = 0;
    public static final int MODE_MONTH = 1;
    
    private ArrayList<String> mTmpKeyList;
    private HashMap<String, GroupInfo> mTmpValueMap;
    private ArrayList<Point> mTmpGroupBounds;
    
    private Point mTmpPoint = new Point(); //used to avoid allocating memory.
    
    private LinkedHashMap<String, ArrayList<String>> mDateAndYearMonthMap = new LinkedHashMap<String, ArrayList<String>>();

    public DateGroupInfos() {
        super();
        mDateKeyList = new ArrayList<String>();
        mDateValueMap = new HashMap<String, GroupInfo>();
        
        mMonthKeyList = new ArrayList<String>();
        mMonthValueMap = new HashMap<String, GroupInfo>();
    }
    
    public static class SlotPositionInfo {
        public int mGroupIndex = -1; // which group is the slot in
        public int mRow = -1;                 // which row in its own group
        public int mColumn = -1;          // which column in its own group
    }
    
    private String getYearAndMonthText(String yearMonthDateText) {
        return yearMonthDateText.substring(0, 7);
    }
    
    public boolean isEmpty() {
        //we don't judge mMonthKeyList here
        if(null == mDateKeyList) return true;
        return mDateKeyList.isEmpty();
    }
    
    /*
     * we assume "put" values are already sorted, automatically increase 1.
     */
    public void put(String dateKey, int slotIndex) {
        //It is added and ordered by insertion order.
        //add date first.
        putIntoMap(mDateKeyList, mDateValueMap, dateKey, slotIndex);
        String yyyyMMText = getYearAndMonthText(dateKey);
        putIntoMap(mMonthKeyList, mMonthValueMap, yyyyMMText, slotIndex);
        
        mapDateAndYearMonth(dateKey, yyyyMMText);
    }
    
    private void mapDateAndYearMonth(String dateKey, String yyyyMMText) {
        ArrayList<String> dateKeyList = mDateAndYearMonthMap.get(yyyyMMText);
        if(null == dateKeyList) {
            dateKeyList = new ArrayList<String>();
            dateKeyList.add(dateKey);
            mDateAndYearMonthMap.put(yyyyMMText, dateKeyList);
        } else {
            if( ! dateKeyList.contains(dateKey)) {
                dateKeyList.add(dateKey);
            }
        }
    }
    
    public void putIntoMap(ArrayList<String> keyList, HashMap<String, GroupInfo> map, String key, int slotIndex) {
        if(keyList.isEmpty()) {
            keyList.add(key);
        } else {
            String lastKey = keyList.get(keyList.size() - 1);
            if( ! lastKey.equals(key)) {
                keyList.add(key);
            }
        }
        GroupInfo value = (GroupInfo)map.get(key);
        if(null == value) {
            value = new GroupInfo();
            value.mNumOfCurrentGroup = 1;
            value.mNumBeforeCurrentGroup = slotIndex;
            map.put(key, value);
        } else {
            value.mNumOfCurrentGroup += 1;
        }
    }
    
    /*
    @Override
    public Integer remove(Object key) {
        if(!(key instanceof String)) return null;
        String keyText = (String)key;
        //date 
        if(mDateKeyList.contains(keyText)) {
            mDateKeyList.remove(keyText);
        }
        //month 
        String yyyyMMText = getYearAndMonthText(keyText);
        if(mMonthKeyList.contains(yyyyMMText)) {
            mMonthKeyList.remove(yyyyMMText);
        }
        return super.remove(keyText);
    }
    */
    
    private void switchMode(int mode) {
        switch(mode) {
        case MODE_DATE: {
            mTmpKeyList = mDateKeyList;
            mTmpValueMap = mDateValueMap;
            mTmpGroupBounds = mDateGroupBounds;
            break;
        }
        case MODE_MONTH:
            mTmpKeyList = mMonthKeyList;
            mTmpValueMap = mMonthValueMap;
            mTmpGroupBounds = mMonthGroupBounds;
            break;
        }
    }
    
    public GroupInfo getValueByGroupIndex(int groupIndex, int mode) {
        if(INDEX_NONE == groupIndex) return null;
        switchMode(mode);
        String key = mTmpKeyList.get(groupIndex);
        return mTmpValueMap.get(key);
    }

    public int getTotalSlotNumBeforeGroup(int groupIndex, int mode) {
        switchMode(mode);
        /*
        if(groupIndex < 1) return 0;
        int total = 0;
        for(int i = 0; i < groupIndex; i++) {
            String key = mTmpKeyList.get(i);
            total += mTmpValueMap.get(key);
        }
        LogUtil.d(TAG, "getTotalSlotNumBeforeGroup total: " + total);
        return total;
        */
        String key = mTmpKeyList.get(groupIndex);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        return groupInfo.mNumBeforeCurrentGroup;
    }
    
    public int getSlotsNumInGroup(final int groupIndex, int mode) {
        switchMode(mode);
        String key = mTmpKeyList.get(groupIndex);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        return groupInfo.mNumOfCurrentGroup;
    }
    
    private int getRowNumber(final int slotsNum, final int unitCount) {
        return (slotsNum + unitCount - 1) / unitCount;
    }
    
    public int getRowNumInGroup(final int groupIndex, final int unitCount, int mode) {
        int slotsNum = getSlotsNumInGroup(groupIndex, mode);
        return getRowNumber(slotsNum, unitCount);
    }
    
    public int getGroupNum(int mode) {
        switchMode(mode);
        return mTmpKeyList.size();
    }
    
    /*
    public int getTotalRows(final int unitCount, int mode) {
        switchMode(mode);
        int rows = 0;
        Iterator<Integer> it = mTmpValueMap.values().iterator();
        while(it.hasNext()) {
            Integer num = it.next();
            rows += getRowNumber(num, unitCount);
        }
        return rows;
    }
    */
    
    public int getNumOfCurrentGroupByIndex(int index, int mode) {
        switchMode(mode);
        if(index < 0 || index >= mTmpKeyList.size()) return 0;
        String key = mTmpKeyList.get(index);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        if(groupInfo == null) {
            LogUtil.d(TAG, "DateGroupInfos::getNumOfCurrentGroupByIndex key:" + key + " groupInfo return null");
            return 0;
        }
        return groupInfo.mNumOfCurrentGroup;
    }
    
    /*
    public int getNumOfCurrentGroupByKey(String key, int mode) {
        switchMode(mode);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        return groupInfo.mNumOfCurrentGroup;
    }
    */
    
    public int getNumBeforeCurrentGroupByIndex(int index, int mode) {
        switchMode(mode);
        String key = mTmpKeyList.get(index);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        return groupInfo.mNumBeforeCurrentGroup;
    }
    
    /*
    public int getNumBeforeCurrentGroupByKey(String key, int mode) {
        switchMode(mode);
        GroupInfo groupInfo = mTmpValueMap.get(key);
        return groupInfo.mNumBeforeCurrentGroup;
    }
    */
    
    public int slotIndexInWhichGroup(int slotIndex, int mode) {
        switchMode(mode);
        int size = mTmpKeyList.size();
        if(0 == size) {
            return INDEX_NONE;
        }
        int numOfCurrent = 0;
        int numOfBefore = 0;
        int currentGroupMinSlotIndex = -1;
        int currentGroupMaxSlotIndex = -1;
        int low = 0;
        int high = size - 1;
        int pointTo = 0; // mid pointer 
        while(low <= high) {
            pointTo = (high - low) / 2 + low;
            GroupInfo groupInfo = getValueByGroupIndex(pointTo, mode);
            //modify by liaoah begin
            if (null == groupInfo) return INDEX_NONE;
            //modify end
            numOfCurrent = groupInfo.mNumOfCurrentGroup;
            numOfBefore = groupInfo.mNumBeforeCurrentGroup;
            currentGroupMinSlotIndex = numOfBefore;
            currentGroupMaxSlotIndex = numOfBefore + numOfCurrent - 1;
            if(slotIndex >= currentGroupMinSlotIndex && slotIndex <= currentGroupMaxSlotIndex) {
                return pointTo;
            } else if(slotIndex > currentGroupMaxSlotIndex){
                //on the right side
                low = pointTo + 1;
                continue;
            } else if(slotIndex < currentGroupMinSlotIndex) {
                //on the left side
                high = pointTo - 1;
                continue;
            } else {
                LogUtil.d(TAG, "It's impossible to reach here");
            }
        }
        return INDEX_NONE;
        /*
        while(true) {
            GroupInfo groupInfo = getValueByGroupIndex(pointTo, mode);
            numOfCurrent = groupInfo.mNumOfCurrentGroup;
            numOfBefore = groupInfo.mNumBeforeCurrentGroup;
            
            currentGroupMinSlotIndex = numOfBefore;
            currentGroupMaxSlotIndex = numOfBefore + numOfCurrent - 1;
            
            if(slotIndex >= currentGroupMinSlotIndex && slotIndex <= currentGroupMaxSlotIndex) {
                return pointTo;
            } else if(slotIndex > currentGroupMaxSlotIndex){
                low = pointTo;
                pointTo = (low + high) >> 1;
                continue;
            } else if(slotIndex < currentGroupMinSlotIndex) {
                high = pointTo;
                pointTo = (low + high) >> 1;
                continue;
            } else {
                LogUtil.d(TAG, "It's impossible to reach here");
            }
        }
        */
    }

    public void getSlotPositionInfo(final int slotIndex, SlotPositionInfo outPositionInfo, final int unitCount, int mode) {
        switchMode(mode);
        int rowInGroup = 0;
        int columnInGroup = 0;

        //Iterator<String> it = mTmpKeyList.iterator();
        
        int groupIndex = slotIndexInWhichGroup(slotIndex, mode);
        if(INDEX_NONE == groupIndex) return;
        GroupInfo groupInfo = getValueByGroupIndex(groupIndex, mode);
        int tmpIndex = slotIndex - groupInfo.mNumBeforeCurrentGroup;
        rowInGroup = tmpIndex / unitCount;
        columnInGroup = tmpIndex % unitCount;
        
        /*
        while(it.hasNext()) {
            String key = it.next();
            Integer numInteger = mTmpValueMap.get(key);
            GroupInfo info = mTmpValueMap.get(key);
            if(null == numInteger) continue;
            int numInGroup = numInteger.intValue();
            if(tmpIndex < numInGroup) {
                rowInGroup = tmpIndex / unitCount;
                columnInGroup = tmpIndex % unitCount;
                break;
            } else {
                ++ groupIndex;
                tmpIndex -= numInGroup;
            }
        }
        */
        outPositionInfo.mGroupIndex = groupIndex;
        outPositionInfo.mRow = rowInGroup;
        outPositionInfo.mColumn = columnInGroup;
    }
    
    public void initGroupBounds(final int unitCount, final SlotView.Spec spec, final int slotHeight) {
        synchronized (this) {
            initGroupBounds(unitCount, spec, slotHeight, MODE_DATE);
            initGroupBounds(unitCount, spec, slotHeight, MODE_MONTH);
        }
    }

    public void initGroupBounds(final int unitCount, final SlotView.Spec spec, final int slotHeight, int mode) {
            switchMode(mode);
            //LogUtil.i(TAG, "initGroupBounds !!!!!!!!!!!! ------------");
            mTmpGroupBounds.clear();
            Iterator<String> it = mTmpKeyList.iterator();
            boolean firstGroup = true;
            int top = 0;
            int bottom = 0;
            int extra = 0;
            int slotGap = spec.getSlotGap(unitCount);
            int groupIndex = 0;
            while(it.hasNext()) {
                it.next();
                final int num = getNumOfCurrentGroupByIndex(groupIndex, mode);
                int row = (num + unitCount - 1) / unitCount;
                extra = firstGroup ? spec.slotAreaTopPadding : spec.slotGroupGap;
                bottom = top + row * (slotHeight)  + slotGap * (row - 1) + extra; 
                mTmpGroupBounds.add(new Point(top, bottom));
                /*
                LogUtil.i(TAG, "initGroupBounds top:" + top + " bottom:" + bottom + " row:" + row + " extra:" + extra + " unitCount:" + unitCount + 
                        " num:" + num + 
                        " slotHeight:" + slotHeight);*/
                top = bottom;
                if(firstGroup) {
                    firstGroup = false;
                }
                ++ groupIndex ;
            }
    }
    
    //getContentLength, from mGroupBounds, it's faster the other implementation of getContentLength(....)
    public int getContentLength(final Spec spec, int mode) {
        switchMode(mode);
        if(mTmpGroupBounds.size() < 1) return 0;
        int maxGroupBound = mTmpGroupBounds.get(mTmpGroupBounds.size() - 1).y;
        int contentLength = maxGroupBound + spec.slotAreaBottomPadding;
        return contentLength;
    }

    public int getGroupByPosition(int scrollPosition, int mode) {
            switchMode(mode);
            if(mTmpGroupBounds.isEmpty()) {
                return INDEX_NONE;
            }
            // LogUtil.d(TAG, "getGroupByPosition scrollPosition:" + scrollPosition);
            //we use Integer.MIN_VALUE to fill Point.x,  it is not used.
            //Point point = new Point(Integer.MIN_VALUE, scrollPosition);
            mTmpPoint.set(Integer.MIN_VALUE, scrollPosition);
            int index = Collections.binarySearch(mTmpGroupBounds, mTmpPoint, new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    // p2 is the second argument of Collections.binarySearch
                    int valueToCompare = p2.y;
                    int lower = p1.x; //upper in screen
                    int upper = p1.y; //lower in screen
                    if(upper < valueToCompare) {
                        /*
                        LogUtil.d(TAG, "getGroupByPosition binary　search compare valueToCompare:" + valueToCompare + 
                                " lower:" + p1.x + 
                                " upper:" + p1.y + 
                                " return -1"); */
                        return -1;
                    } else if(valueToCompare >= lower && valueToCompare <= upper) {
                        /*
                        LogUtil.d(TAG, "getGroupByPosition binary　search compare valueToCompare:" + valueToCompare + 
                                " lower:" + p1.x + 
                                " upper:" + p1.y + 
                                " return 0"); */
                        return 0;
                    } else {
                        /*
                        LogUtil.d(TAG, "getGroupByPosition binary　search compare valueToCompare:" + valueToCompare + 
                                " lower:" + p1.x + 
                                " upper:" + p1.y + 
                                " return 1"); */
                        return 1;
                    }
                }
            });
            //LogUtil.d(TAG, "getGroupByPosition: return ---> " + index);
            return index < 0 ? INDEX_NONE : index;
    }
    
    public ArrayList<Point> getGroupBounds(int mode) {
        switchMode(mode);
        return mTmpGroupBounds;
    }

    public Point getGroupBound(int groupIndex, int unitCount, Spec spec, int slotHeight, int mode) {
        switchMode(mode);
        Iterator<String> it = mTmpKeyList.iterator();
        boolean firstGroup = true;
        int top = 0;
        int bottom = 0;
        int extra = 0;
        int index = 0;
        int slotGap = spec.getSlotGap(unitCount);
        GroupInfo groupInfo = null;
        while(it.hasNext()) {
            String key = it.next();
            groupInfo = mTmpValueMap.get(key);
            //modify by liaoah begin
            if (null == groupInfo) return null;
            //modify end
            final int num = groupInfo.mNumOfCurrentGroup;
            int row = (num + unitCount - 1) / unitCount;
            extra = firstGroup ? spec.slotAreaTopPadding : spec.slotGroupGap;
            bottom = top + row * slotHeight + (row - 1) * slotGap + extra;
            if(index == groupIndex) {
                return new Point(top, bottom);
            }
            //LogUtil.i(TAG, "initGroupBounds top:" + top + " bottom:" + bottom);
            top = bottom;
            if(firstGroup) {
                firstGroup = false;
            }
            index ++;
        }
        return null;
    }
    
    public Point getGroupBound(int groupIndex, int mode) {
        switchMode(mode);
        if(null == mTmpGroupBounds || mTmpGroupBounds.isEmpty()) return null;
        return mTmpGroupBounds.get(groupIndex);
    }
    
    public String getGroupDateText(final int groupIndex, int mode) {
        switchMode(mode);
        if(groupIndex >= mTmpKeyList.size() || groupIndex < 0) return "";
        return mTmpKeyList.get(groupIndex);
    }
    
    //it doesn't take date index into consideration
    public int getFirstSlotIndexByPosition(final int scrollPosition, final int unitCount, final Spec spec, final int slotHeight, int mode) {
        switchMode(mode);
        //determine which group is in current position
        int groupIndex = getGroupByPosition(scrollPosition, mode);
        //LogUtil.d(TAG, "getFirstSlotIndexByPosition getGroupByPosition scrollPosition: " + scrollPosition + " return  groupIndex: " + groupIndex);
        if(INDEX_NONE == groupIndex) {
            return 0;
        }
        //traverse the rows of currently found group, and determine which row is at the beginning.
        /*
        for(int i = 0; i < mTmpGroupBounds.size(); i++) {
            LogUtil.d(TAG, "getFirstSlotIndexByPosition mGroupBounds: " + i + " "+ mTmpGroupBounds.get(i));
        }
        */
        
        Point groupBound = getGroupBound(groupIndex, mode);
        //LogUtil.d(TAG, "getFirstSlotIndexByPosition groupBound :" + groupBound);
        if(null == groupBound) return 0;
        int rowTop = 0;
        int rowBottom = 0;
        if(0 == groupIndex) {
            rowTop = groupBound.x + spec.slotAreaTopPadding;
        } else {
            rowTop = groupBound.x + spec.slotGroupGap;
        }
        //It's white padding between [  groupBound.x,   first rowTop  )
        if(scrollPosition >= groupBound.x && scrollPosition < rowTop) {
            /*
            LogUtil.d(TAG, "getFirstSlotIndexByPosition scrollPosition: " + scrollPosition + 
                    " groupBound.x:" + groupBound.x + 
                    " rowTop:" + rowTop); */
            return getTotalSlotNumBeforeGroup(groupIndex, mode);
        }
        int rowNumInThisGroup = getRowNumInGroup(groupIndex, unitCount, mode);
        int slotGap = spec.getSlotGap(unitCount);
        for(int i = 0; i < rowNumInThisGroup; i++) {
            rowBottom = rowTop + (i + 1) * (slotHeight) + i * slotGap;
            /*
            LogUtil.d(TAG, "getFirstSlotIndexByPosition scrollPosition: " + scrollPosition + 
                    " rowTop:" + rowTop + 
                    " rowBottom:" + rowBottom); */
            if(scrollPosition >= rowTop && scrollPosition <= rowBottom) {
                return getTotalSlotNumBeforeGroup(groupIndex, mode) + unitCount * i;
            }
        }
        return INDEX_NONE;
    }
    
    
    private void clearKeyAndMap(ArrayList<String> keyList, HashMap<String, GroupInfo> valueMap) {
        if(null != keyList) {
            keyList.clear();
        }
        if(null != valueMap) {
            valueMap.clear();
        }
    }
    
    public void clear() {
        clearKeyAndMap(mDateKeyList, mDateValueMap);
        clearKeyAndMap(mMonthKeyList, mMonthValueMap);
        mDateAndYearMonthMap.clear();
    }

    public ArrayList<String> getKeyList(int mode) {
        switch(mode) {
            case MODE_DATE: {
                return mDateKeyList;
            }
            case MODE_MONTH: {
                return mMonthKeyList;
            }
        }
        return null;
    }
    
    public HashMap<String, GroupInfo> getValueMap(int mode) {
        switch(mode) {
        case MODE_DATE: {
            return mDateValueMap;
        }
        case MODE_MONTH: {
            return mMonthValueMap;
        }
    }
    return null;
    }
    
    @SuppressWarnings("unchecked")
    public DateGroupInfos cloneFrom(ArrayList<String> srcDateKeyList, ArrayList<String> srcMonthKeyList,
                                            HashMap<String, GroupInfo> srcDateValueMap,  HashMap<String, GroupInfo> srcMonthValueMap, 
                                            LinkedHashMap<String, ArrayList<String>> dateAndYearMonthMap) {
        /*
        Iterator<String> keyIterator = srcDateKeyList.iterator();
        while(keyIterator.hasNext()) {
            String key = keyIterator.next();
            Integer value = tmpValueMap.get(key);
            info.put(key, value);
        }
        */
        mDateKeyList = (ArrayList<String>)srcDateKeyList.clone();
        mMonthKeyList = (ArrayList<String>)srcMonthKeyList.clone();
        mDateValueMap = (HashMap<String, GroupInfo>)srcDateValueMap.clone();
        mMonthValueMap = (HashMap<String, GroupInfo>)srcMonthValueMap.clone();
        mDateAndYearMonthMap = (LinkedHashMap<String, ArrayList<String>>)dateAndYearMonthMap.clone();
        return this;
    }
    
    @Override
    public Object clone() {
        DateGroupInfos info = new DateGroupInfos();
        ArrayList<String> srcDateKeyList = getKeyList(MODE_DATE);
        HashMap<String, GroupInfo> srcDateValueMap = getValueMap(MODE_DATE);
        ArrayList<String> srcMonthKeyList = getKeyList(MODE_MONTH);
        HashMap<String, GroupInfo> srcMonthValueMap = getValueMap(MODE_MONTH);
        info.cloneFrom(srcDateKeyList, srcMonthKeyList, srcDateValueMap, srcMonthValueMap, mDateAndYearMonthMap);
        return info;
    }
    
    
    public class DateInfo {
        public int mode;
        public int fromGroupIndex;
        public int toGroupIndex;
        public String text; //may be 2016.01  or  2016.01.16
        public ArrayList<String> textsMapFromMonthToDate;
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("mode:");
            sb.append(mode);
            sb.append(" ");
            sb.append("fromGroupIndex:");
            sb.append(fromGroupIndex);
            sb.append(" ");
            sb.append("toGroupIndex:");
            sb.append(toGroupIndex);
            sb.append(" ");
            sb.append("text");
            sb.append(text);
            sb.append(" ");
            return sb.toString();
        }
        
        
    }
    
    private static final int MAX_GROUP_INDEX = 10;
    public DateInfo matchDate(int fromMode, int toMode, int fromGroupIndex) {
        if(fromMode == toMode) {
            throw new IllegalArgumentException("should not be the same mode");
        }
        DateInfo dateInfo = new DateInfo();
        switchMode(fromMode);
        String fromKey = mTmpKeyList.get(fromGroupIndex);
        
        switchMode(toMode);
        if(fromMode == MODE_DATE) {
            String yearAndMonthText = getYearAndMonthText(fromKey);
            int index = mTmpKeyList.indexOf(yearAndMonthText);
            dateInfo.fromGroupIndex = index;
            dateInfo.toGroupIndex = index;
            dateInfo.mode = toMode;
            dateInfo.text = fromKey;
        } else if(fromMode == MODE_MONTH) {
            int dateGroupIndex = 0;
            for(int i = 0; i < mMonthKeyList.size(); i++) {
                String monthKey = mMonthKeyList.get(i);
                ArrayList<String> dateList = mDateAndYearMonthMap.get(monthKey);
                if(dateList == null || dateList.isEmpty()) continue;
                if( ! monthKey.equals(fromKey)) {
                    dateGroupIndex += dateList.size();
                } else {
                    dateInfo.fromGroupIndex = dateGroupIndex;
                    dateInfo.toGroupIndex = dateGroupIndex + dateList.size() - 1;
                    dateInfo.textsMapFromMonthToDate = dateList;
                    dateInfo.mode = toMode;
                    dateInfo.text = fromKey;
                    break;
                }
            }
            
            /*
            boolean first = true;
            if(dateInfo.textsMapFromMonthToDate == null) {
                dateInfo.textsMapFromMonthToDate = new ArrayList<String>();
            }
            dateInfo.textsMapFromMonthToDate.clear();
            LogUtil.d(TAG, "dateInfo.textsMapFromMonthToDate --> ------------------------------");
            for(int i=0; i<mTmpKeyList.size(); i++) {
                String key = mTmpKeyList.get(i);
                if(key.startsWith(fromKey)) {
                    if(first) {
                        dateInfo.fromGroupIndex = i;
                        dateInfo.toGroupIndex = i;
                        first = false;
                    } else {
                        dateInfo.toGroupIndex = Math.max(dateInfo.toGroupIndex, i);
                        if(dateInfo.toGroupIndex >= dateInfo.fromGroupIndex + MAX_GROUP_INDEX) {
                            break;
                        }
                    }
                    dateInfo.textsMapFromMonthToDate.add(key);
                    LogUtil.d(TAG, "dateInfo.textsMapFromMonthToDate --> " + key + " i:" + i);
                } else {
                    if(dateInfo.toGroupIndex - dateInfo.fromGroupIndex > 0) {
                        break;
                    }
                }
            }
            LogUtil.d(TAG, "dateInfo.textsMapFromMonthToDate --> ------------------------------===========================");
            dateInfo.mode = toMode;
            dateInfo.text = fromKey;
            */
        }
        return dateInfo;
    }
    /*
    public void dump() {
        LogUtil.d(TAG, "DateGroupInfos::dump  ***　" + mKeyList.toString());
        Set<Entry<String, Integer>> entries = this.entrySet();
        Iterator<Entry<String, Integer>> it = entries.iterator();
        while(it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            LogUtil.d(TAG, "DateGroupInfos::dump  *** :key :" + key + " value:" + value);
        }
        LogUtil.d(TAG, "");
    }
    */
}
