package com.monster.market.http.data;

import com.monster.market.bean.TopicInfo;

import java.util.List;

/**
 * Created by xiaobin on 16-8-20.
 */
public class TopicResultData {

    private List<TopicInfo> topicList;

    public List<TopicInfo> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<TopicInfo> topicList) {
        this.topicList = topicList;
    }

    @Override
    public String toString() {
        return "TopicResultData{" +
                "topicList=" + topicList +
                '}';
    }

}
