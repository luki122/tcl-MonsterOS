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
			put("com.intsig.camcard.chat.RecentChatList$Activity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.OpenInterfaceActivity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.infoflow.PreOptionCreateInfoFlowActivity", com.mst.R.drawable.s_namecard);
			put("com.intsig.camcard.EmailSignatureRecognizeActivity", com.mst.R.drawable.s_namecard);
			put("com.alipay.mobile.quinox.splash.ShareDispenseActivity", com.mst.R.drawable.s_alipay);
			put("com.tencent.mobileqq.activity.qfileJumpActivity", com.mst.R.drawable.s_topc);
			put("com.UCMobile.share", com.mst.R.drawable.s_editshare);
		}
	};
//	put("com.tencent.mobileqq.activity.qfileJumpActivity",	com.mst.R.drawable.s_qq_music);

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
}