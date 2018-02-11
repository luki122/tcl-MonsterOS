/**
 * @author huliang
 */

package com.android.internal.app;

import java.util.HashMap;
import java.util.Map;

public class iconsMap {

	private static iconsMap  mInstance;
	
	private Map<String , Integer> iconMap = new HashMap<String , Integer>() {
		{
			put("com.tencent.mm.ui.tools.ShareToTimeLineUI",			com.mst.R.drawable.s_mm_moments);
			put("com.tencent.mm.ui.tools.ShareImgUI",						com.mst.R.drawable.s_mm);
			put("com.tencent.mm.ui.tools.AddFavoriteUI",					com.mst.R.drawable.s_mm_collect);
			put("cooperation.qqfav.widget.QfavJumpActivity",				com.mst.R.drawable.s_qq_zone);
			put("com.tencent.mobileqq.activity.JumpActivity", 			com.mst.R.drawable.s_qq);
			put("cooperation.qlink.QlinkShareJumpActivity", 				com.mst.R.drawable.s_t);
			put("com.android.messaging.ui.conversationlist.ShareIntentActivity", com.mst.R.drawable.s_instagram);
			put("com.etao.feimagesearch.FEISImageEditorActivity",	com.mst.R.drawable.s_t);
			put("com.tmall.wireless.splash.TMSplashActivity",				com.mst.R.drawable.s_mail);
			put("com.mediatek.contacts.ShareContactViaSMSActivity",		com.mst.R.drawable.s_mms);
			put("com.android.mms.ui.ComposeMessageActivity",		com.mst.R.drawable.s_mms);
			put("com.android.nfc.BeamShareActivity",							com.mst.R.drawable.s_android_beam);
			put("com.android.bluetooth.opp.BluetoothOppLauncherActivity", com.mst.R.drawable.s_bluetooth);
			put("com.sina.weibo.composerinde.ComposerDispatchActivity", com.mst.R.drawable.s_weibo);			
//			put("com.intsig.camcard.chat.RecentChatList$Activity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.OpenInterfaceActivity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.infoflow.PreOptionCreateInfoFlowActivity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.EmailSignatureRecognizeActivity", com.mst.R.drawable.s_namecard);
			put("com.alipay.mobile.quinox.splash.ShareDispenseActivity", com.mst.R.drawable.s_alipay);
			put("com.tencent.mobileqq.activity.qfileJumpActivity", com.mst.R.drawable.s_topc);
			put("com.UCMobile.share", com.mst.R.drawable.s_editshare);
			put("com.uc.browser.FavoriteActivity",  com.mst.R.drawable.s_uc); // uc 浏览器收藏
			put("cn.tcl.transfer.zxing.client.android.encode.EncodeActivity",  com.mst.R.drawable.s_tcl_hj); // 一键换机
			put("com.zhihu.android.ui.activity.ShareToPeopleActivity",  com.mst.R.drawable.s_zhihu); // 知乎
			
			// 以下是打开方式
			put("com.UCMobile.edit_image_and_share", com.mst.R.drawable.s_editshare); 
			put("com.tencent.mm.ui.tools.ShareScreenToTimeLineUI", com.mst.R.drawable.s_mm_moments); // 打开的朋友圈
			put("com.tencent.mm.ui.tools.ShareScreenImgUI", com.mst.R.drawable.s_mm); // 打开的朋友圈
			put("com.UCMobile.image", com.mst.R.drawable.s_uc); // uc 浏览器查看图片
			put("com.android.gallery3d.app.GalleryActivity", com.mst.R.drawable.s_gallery); // 图库查看图片
			put("com.intsig.camcard.cardholder.UnZipCardFileActivity", com.mst.R.drawable.s_namecard); //打开引荐联系人
			put("com.UCMobile.main.UCMobile",  com.mst.R.drawable.s_uc); // uc 浏览器收藏
			put("com.tencent.mm.plugin.accountsync.ui.ContactsSyncUI", com.mst.R.drawable.s_mm); //微信
			put("com.sina.weibo.weiyouinterface.WeiyouDispatchActivity", com.mst.R.drawable.s_weibo); // 微博
			put("com.sina.weibo.page.ProfileInfoActivity", com.mst.R.drawable.s_weibo); // 微博
			put("com.sina.weibo.page.CardPicListActivity", com.mst.R.drawable.s_weibo); // 微博

		}
	};
//	put("com.tencent.mobileqq.activity.qfileJumpActivity",	com.mst.R.drawable.s_qq_music);

	private Map<String , Integer> UnSupportedActivity = new HashMap<String , Integer>() {
		{
			put("com.intsig.camcard.chat.RecentChatList$Activity", com.mst.R.drawable.s_namecard); //  名片全能王:分享给朋友
		}
	};
	
	private iconsMap () {
	}
	
	public synchronized static iconsMap getInstance() {
		if (null == mInstance) {
			mInstance = new iconsMap();
		}
		return mInstance;
	}
	
	public int getIcon(String pkgandactivity) {
		if (iconMap.containsKey(pkgandactivity)) {
			return iconMap.get(pkgandactivity);
		}
		else 
			return 0; 
	}
	
	// 查是否支持该activity
	public Boolean isSupportedActivity(String activity) {
		return !UnSupportedActivity.containsKey(activity);
	}
}