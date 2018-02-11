package com.mst.tms;

//import com.mst.tms.MarkManager;

import android.os.Parcel;
import android.os.Parcelable;

public class MarkResult implements Parcelable {
	// Field descriptor #51 I
	public int property;

	public int getProperty() {
		return property;
	}

	public void setProperty(int property) {
		this.property = property;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String number;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	// Field descriptor #51 I
	public int tagType;

	public int getTagType() {
		return tagType;
	}

	public void setTagType(int tagType) {
		this.tagType = tagType;
	}

	// Field descriptor #51 I
	public int tagCount;

	public int getTagCount() {
		return tagCount;
	}

	public void setTagCount(int tagCount) {
		this.tagCount = tagCount;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String warning;

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	// Field descriptor #51 I
	public int usedFor;
	
	public int getUserFor() {
		return usedFor;
	}

	public void setUserFor(int usedFor) {
		this.usedFor = usedFor;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String location;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String eOperator;

	public String getEoperator() {
		return eOperator;
	}

	public void setEoperator(String eOperator) {
		this.eOperator = eOperator;
	}

	// Field descriptor #51 I
	public static final int PROP_Tag = 1;

	// Field descriptor #51 I
	public static final int PROP_Yellow = 2;

	// Field descriptor #51 I
	public static final int PROP_Tag_Yellow = 3;

	// Field descriptor #51 I
	public static final int USED_FOR_Common = 16;

	// Field descriptor #51 I
	public static final int USED_FOR_Calling = 17;

	// Field descriptor #51 I
	public static final int USED_FOR_Called = 18;

	//property， number,name,tagType,tagCount,warning,usedFor,location,eOperator
	public MarkResult(int property, String number, String name, int tagType, int tagCount, String warning, int usedFor, String location, String eOperator) {
		this.property = property;
		this.number = number;
		this.name = name;
		this.tagType = tagType;
		this.tagCount = tagCount;
		this.warning = warning;
		this.usedFor = usedFor;
		this.location = location;
		this.eOperator = eOperator;
	}

	@Override
	public int describeContents() {
		return 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(property);
		dest.writeString(number);
		dest.writeString(name);
		dest.writeInt(tagType);
		dest.writeInt(tagCount);
		dest.writeString(warning);
		dest.writeInt(usedFor);
		dest.writeString(location);
		dest.writeString(eOperator);
	}

	public static final Parcelable.Creator<MarkResult> CREATOR = new Parcelable.Creator<MarkResult>() {

		@Override
		public MarkResult createFromParcel(Parcel source) {

			return new MarkResult(source.readInt(), source.readString(), source.readString(), source.readInt(), source.readInt(), source.readString(), source.readInt(), source.readString(), source.readString());
		}

		@Override
		public MarkResult[] newArray(int size) {

			return new MarkResult[size];
		}
	};

	@Override
     public String toString() {
    	StringBuilder strBuilder = new StringBuilder();
    	strBuilder.append("property:");
    	strBuilder.append(property);
    	if(property == PROP_Tag){
    		strBuilder.append("=标记\n");
    	}else if(property == PROP_Yellow){
    		strBuilder.append("=黄页\n");
    	}else if(property == PROP_Tag_Yellow){
    		strBuilder.append("=标记黄页\n");
    	}else {
    		strBuilder.append("\n");
    	}
    	strBuilder.append("号码:[" + number + "]\n");
    	strBuilder.append("名称:[" + name + "]\n");
//    	strBuilder.append("标记类型:[" + tagType+"="+MarkManager.getInstance().getTagName(tagType) + "]\n");//tagtype从mNumMarkerManager.getTagName()获取名字字符串
    	strBuilder.append("标记类型:[" + tagType + "]\n");
    	strBuilder.append("标记数量:[" + tagCount + "]\n");
    	strBuilder.append("警告信息:[" + warning + "]\n");
    	strBuilder.append("usedFor:");
    	strBuilder.append(usedFor);
    	if(usedFor == USED_FOR_Common){
    		strBuilder.append("=通用\n");
    	}else if(usedFor == USED_FOR_Calling){
    		strBuilder.append("=主叫\n");
    	}else if(usedFor == USED_FOR_Called){
    		strBuilder.append("=被叫\n");
    	}else {
    		strBuilder.append("\n");
    	}
    	strBuilder.append("归属地:[" + location + "]\n");
    	strBuilder.append("虚拟运营商:[" + eOperator + "]\n");
    	
    	return strBuilder.toString();
     }
}