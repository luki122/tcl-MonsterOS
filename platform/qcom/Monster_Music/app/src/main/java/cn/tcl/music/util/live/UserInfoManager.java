package cn.tcl.music.util.live;

import android.content.Context;

import cn.tcl.music.model.live.XiamiMemberInfo;
import cn.tcl.music.util.PreferenceUtil;

/**
 * single instance to get xiami userinfo
 */
public class UserInfoManager {
    private static UserInfoManager mInstance;
    private Context mContext;

    private UserInfoManager(Context context) {
        mContext = context;
    }

    public static UserInfoManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UserInfoManager(context);
        }
        return mInstance;
    }

    public XiamiMemberInfo getmMemberInfo() {
        XiamiMemberInfo memberInfo = new XiamiMemberInfo();
        memberInfo.user_id = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_USER_ID,0l);
        memberInfo.nick_name = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_NICK_NAME,"");
        memberInfo.avatar = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_AVATAR,"");
        memberInfo.gender = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_GENDER,"");
        memberInfo.description = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_DESCRIPTION,"");
        memberInfo.gmt_create = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_GMT_CREATE,0l);
        memberInfo.signature = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_SIGNATURE,"");
        memberInfo.fans = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FANS,0);
        memberInfo.followers = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FOLLOWERS,0);
        memberInfo.listens = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_LISTENS,0);
        memberInfo.collect_count = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_COLLECT_COUNT,0);
        memberInfo.is_vip = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_IS_VIP,false);
        memberInfo.vip_begin = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_VIP_BEGIN,0l);
        memberInfo.vip_finish = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_VIP_FINISH,0l);
        memberInfo.is_self = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_IS_SELF,false);
        memberInfo.friendship = PreferenceUtil.getValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FRIENDSHIP,false);
        return memberInfo;
    }

    public void setmMemberInfo(XiamiMemberInfo memberInfo) {
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_USER_ID,memberInfo.user_id);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_NICK_NAME,memberInfo.nick_name);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_AVATAR,memberInfo.avatar);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_GENDER,memberInfo.gender);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_DESCRIPTION,memberInfo.description);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_GMT_CREATE,memberInfo.gmt_create);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_SIGNATURE,memberInfo.signature);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FANS,memberInfo.fans);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FOLLOWERS,memberInfo.followers);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_LISTENS,memberInfo.listens);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_COLLECT_COUNT,memberInfo.collect_count);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_IS_VIP,memberInfo.is_vip);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_VIP_BEGIN,memberInfo.vip_begin);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_VIP_FINISH,memberInfo.vip_finish);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_IS_SELF,memberInfo.is_self);
        PreferenceUtil.saveValue(mContext,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_FRIENDSHIP,memberInfo.friendship);
    }
}
