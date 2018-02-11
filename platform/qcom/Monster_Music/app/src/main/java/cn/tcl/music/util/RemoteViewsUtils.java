package cn.tcl.music.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.PreloadTarget;
import com.bumptech.glide.request.target.Target;
import com.tcl.framework.notification.NotificationCenter;
import com.xiami.sdk.utils.ImageUtil;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.service.MusicPlayBackService;

public final class RemoteViewsUtils {
    private static final String TAG = RemoteViewsUtils.class.getSimpleName();
    private static final String UPDATE_NOTIFICATION = "updateNotification";
    private static final String UNKNOWN_ARTIST = "<unknown>";
    private static final String START_WITH_HTTP = "http";
    private static final int IMAGE_QUALITY_SIZE = 300;
    public static String sArtworkPath;
    public static Bitmap sCurrentBitmap;
    private static Bitmap sCurrentBitmapRing;

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;

        if (srcAspect > dstAspect) {
            final int srcRectWidth = (int) (srcHeight * dstAspect);
            final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
            return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
        } else {
            final int srcRectHeight = (int) (srcWidth / dstAspect);
            final int scrRectTop = (srcHeight - srcRectHeight) / 2;
            return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
        }
    }

    public final static void updateMusic5RemoteViews(final Notification n, final RemoteViews views, final Context context, MediaInfo mediaInfo, int requestOffset, boolean isWidget) {
        //Update media metadata display
        if (null != mediaInfo) {
            views.setTextViewText(R.id.title_text_view, MixUtil.getSongNameWithNoSuffix(mediaInfo.title));
            String artistName = mediaInfo.artist;
            if (null != artistName && artistName.equals(UNKNOWN_ARTIST)) {
                artistName = context.getResources().getString(R.string.unknown);
            }
            views.setTextViewText(R.id.subtitle_text_view, artistName);
            final String artworkPath = mediaInfo.artworkPath;
            final String artistPath = mediaInfo.artistPortraitPath;
            if (isWidget) {
                if (TextUtils.isEmpty(artworkPath) && TextUtils.isEmpty(artistPath)) {
                    views.setImageViewResource(R.id.artwork_image_view_ring, R.drawable.default_cover_menu);
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            int size = context.getResources().getDimensionPixelSize(R.dimen.width_image_widget);
                            final PreloadTarget<Bitmap> target = PreloadTarget.obtain(size, size);
                            String imagePath = TextUtils.isEmpty(artworkPath) ? artistPath : artworkPath;
                            if (imagePath.startsWith(START_WITH_HTTP)) {
                                imagePath = ImageUtil.transferImgUrl(imagePath, IMAGE_QUALITY_SIZE);
                            }
                            try {
                                Glide.with(context).load(imagePath).asBitmap().listener(new RequestListener<String, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        views.setImageViewBitmap(R.id.artwork_image_view_ring, resource);
                                        NotificationCenter.defaultCenter().publish(UPDATE_NOTIFICATION, n);
                                        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                                                .notify(MusicPlayBackService.ONGOING_NOTIFICATION_ID, n);
                                        return false;
                                    }
                                }).into(target);
                            } catch (IllegalArgumentException e) {

                            }
                        }
                    });
                }
            } else {
                if (TextUtils.isEmpty(artworkPath) && TextUtils.isEmpty(artistPath)) {
                    views.setImageViewResource(R.id.artwork_image_view, R.drawable.default_cover_menu);
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            int size = context.getResources().getDimensionPixelSize(R.dimen.width_image_widget);
                            final PreloadTarget<Bitmap> target = PreloadTarget.obtain(size, size);
                            String imagePath = TextUtils.isEmpty(artworkPath) ? artistPath : artworkPath;
                            if (imagePath.startsWith("http")) {
                                imagePath = ImageUtil.transferImgUrl(imagePath, 300);
                            }
                            Glide.with(context).load(imagePath).asBitmap().listener(new RequestListener<String, Bitmap>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    views.setImageViewBitmap(R.id.artwork_image_view, resource);
                                    NotificationCenter.defaultCenter().publish(UPDATE_NOTIFICATION, n);
                                    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                                            .notify(MusicPlayBackService.ONGOING_NOTIFICATION_ID, n);
                                    return false;
                                }
                            }).into(target);
                        }
                    });
                }
            }
        } else {
            views.setTextViewText(R.id.title_text_view, "");
            views.setTextViewText(R.id.subtitle_text_view, "");
            views.setImageViewResource(R.id.artwork_image_view, R.drawable.default_cover_menu);
            if (!isWidget) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(context, com.mixvibes.mvlib.R.string.the_track_cannot_be_played);
                    }
                });
            }
        }
        if (MusicPlayBackService.isPlaying()) {
            views.setImageViewResource(R.id.track_play_pause_image_btn, isWidget ? R.drawable.ic_media_pause : R.drawable.picto_pause_off);
            views.setOnClickPendingIntent(R.id.track_play_pause_image_btn,
                    MusicPlayBackService.createPendingAction(CommonConstants.COMMAND_PAUSE, context,
                            requestOffset + MusicPlayBackService.REQUESTCODE_PLAYPAUSE_BUTTON, MusicPlayBackService.class));
        } else {
            views.setImageViewResource(R.id.track_play_pause_image_btn, isWidget ? R.drawable.ic_media_play : R.drawable.picto_play_off);
            views.setOnClickPendingIntent(R.id.track_play_pause_image_btn,
                    MusicPlayBackService.createPendingAction(CommonConstants.COMMAND_PLAY, context,
                            requestOffset + MusicPlayBackService.REQUESTCODE_PLAYPAUSE_BUTTON, MusicPlayBackService.class));
        }
        views.setOnClickPendingIntent(R.id.track_next_image_btn,
                MusicPlayBackService.createPendingAction(CommonConstants.COMMAND_NEXT, context,
                        requestOffset + MusicPlayBackService.REQUESTCODE_NEXT_BUTTON, MusicPlayBackService.class));
        views.setOnClickPendingIntent(R.id.track_prev_image_btn,
                MusicPlayBackService.createPendingAction(CommonConstants.COMMAND_PREV, context,
                        requestOffset + MusicPlayBackService.REQUESTCODE_PREV_BUTTON, MusicPlayBackService.class));
        views.setOnClickPendingIntent(R.id.close_back_btn,
                MusicPlayBackService.createPendingAction(CommonConstants.COMMAND_CLOSE_NOTIFICAITON, context,
                        MusicPlayBackService.REQUESTCODE_QUIT, MusicPlayBackService.class));
    }

    private static void computeArtworkForWidget(RemoteViews views, Context context, String artworkPath) {
        views.setImageViewBitmap(R.id.artwork_image_view_ring, null);
        if (null == sCurrentBitmapRing || sCurrentBitmapRing.isRecycled()) {
            sCurrentBitmapRing = createBitmapRing(context);
        }
        if (!TextUtils.equals(sArtworkPath, artworkPath) || null == sCurrentBitmap) {
            sArtworkPath = artworkPath;
            if (TextUtils.isEmpty(artworkPath)) {
                sCurrentBitmap = null;
                views.setImageViewResource(R.id.artwork_image_view, R.drawable.default_cover_menu);
            } else {
                int size = context.getResources().getDimensionPixelSize(R.dimen.width_image_widget);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                Bitmap unscaledBmp = null;
                if (null == unscaledBmp) {
                    sCurrentBitmap = null;
                    views.setImageViewResource(R.id.artwork_image_view, R.drawable.default_cover_menu);
                    return;
                }
                Rect srcRect = calculateSrcRect(unscaledBmp.getWidth(), unscaledBmp.getHeight(), size, size);
                sCurrentBitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
                Canvas canvas = new Canvas(sCurrentBitmap);
                Path path = new Path();
                path.addCircle(size / 2, size / 2, size / 2, Direction.CW);
                path.close();
                canvas.clipPath(path);
                canvas.drawBitmap(unscaledBmp, srcRect, new Rect(0, 0, size, size), new Paint(Paint.FILTER_BITMAP_FLAG));

                views.setImageViewBitmap(R.id.artwork_image_view, sCurrentBitmap);
                views.setImageViewBitmap(R.id.artwork_image_view_ring, sCurrentBitmapRing);

            }
        } else if (null != sCurrentBitmap) {
            views.setImageViewBitmap(R.id.artwork_image_view, sCurrentBitmap);
            views.setImageViewBitmap(R.id.artwork_image_view_ring, sCurrentBitmapRing);
        } else {
            views.setImageViewResource(R.id.artwork_image_view, R.drawable.default_cover_menu);
        }
    }

    private static Bitmap createBitmapRing(Context context) {
        int ringSize = context.getResources().getDimensionPixelSize(R.dimen.width_image_widget);
        float ringStrokeSize = context.getResources().getDimension(R.dimen.stroke_circle_size);
        Bitmap bmp = Bitmap.createBitmap(ringSize, ringSize, Config.ARGB_8888);
        if (null != bmp) {
            Canvas ringCanvas = new Canvas(bmp);
            Path ringPath = new Path();
            ringPath.addCircle(ringSize / 2, ringSize / 2, ringSize + ringStrokeSize / 2, Direction.CW);
            ringPath.close();
            ringCanvas.clipPath(ringPath);

            Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaint.setStyle(Style.STROKE);
            ringPaint.setColor(context.getColor(R.color.white_overlay));
            ringPaint.setStrokeWidth(ringStrokeSize);
            ringCanvas.drawCircle(ringSize / 2, ringSize / 2, (ringSize - ringStrokeSize) / 2, ringPaint);
        }
        return bmp;
    }
}
