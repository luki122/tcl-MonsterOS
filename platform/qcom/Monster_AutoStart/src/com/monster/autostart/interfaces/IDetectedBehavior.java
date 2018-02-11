package com.monster.autostart.interfaces;

import java.util.List;

import com.monster.autostart.bean.AppInfo;

public interface IDetectedBehavior {
	public List<AppInfo> detected(String packageName);
}
