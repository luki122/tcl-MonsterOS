package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-7-20.
 */
public class RankingRequestData extends BasePageInfoData {

    private int rankType;   // 1.游戏 2.应用

    public int getRankType() {
        return rankType;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }
}
