package com.monster.market.bean;

/**
 * Created by xiaobin on 16-8-20.
 */
public class TopicInfo {

    private int id; // 专题ID
    private String name;     // 专题名
    private String icon;    // 图标
    private String intro;   // 简介
    private int appNum;  // 数量

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getAppNum() {
        return appNum;
    }

    public void setAppNum(int appNum) {
        this.appNum = appNum;
    }

    @Override
    public String toString() {
        return "TopicInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", intro='" + intro + '\'' +
                ", appNum='" + appNum + '\'' +
                '}';
    }
}
