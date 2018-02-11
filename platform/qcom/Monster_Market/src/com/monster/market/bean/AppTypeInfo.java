package com.monster.market.bean;

public class AppTypeInfo {

	private int typeId;
	private String typeName;
	private String typeIcon;

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeIcon() {
		return typeIcon;
	}

	public void setTypeIcon(String typeIcon) {
		this.typeIcon = typeIcon;
	}

	@Override
	public String toString() {
		return "AppTypeInfo [typeId=" + typeId + ", typeName=" + typeName
				+ ", typeIcon=" + typeIcon + "]";
	}

}
