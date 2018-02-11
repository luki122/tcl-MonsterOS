/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.ColorTexture;
import com.android.gallery3d.glrenderer.CustomStringTexture;
import com.android.gallery3d.glrenderer.FadeInTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.glrenderer.TiledTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.LogUtil;

public class AlbumSlotRenderer extends AbstractSlotRenderer {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumView";

    public interface SlotFilter {
        public boolean acceptSlot(int index);
    }

    private final int mPlaceholderColor;
    public static final int CACHE_SIZE = 96;//original private , ShenQianfeng modify this to public.

    private AlbumSlidingWindow mDataWindow;
    private final AbstractGalleryActivity mActivity;
    private final ColorTexture mWaitLoadingTexture;
    private final SlotView mSlotView;
    private final SelectionManager mSelectionManager;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;

    private SlotFilter mSlotFilter;

    private int mDateToSlotMargin = 24;
    
    private String [] mLanguageMonthMap = new String[12];
    
    public AlbumSlotRenderer(AbstractGalleryActivity activity, SlotView slotView,
            SelectionManager selectionManager, int placeholderColor) {
        super(activity);
        mActivity = activity;
        mSlotView = slotView;
        mSelectionManager = selectionManager;
        mPlaceholderColor = placeholderColor;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
        
        // TCL ShenQianfeng Begin on 2016.08.19
        Resources res = mActivity.getResources();
        mDateToSlotMargin = res.getDimensionPixelSize(R.dimen.mst_date_to_slot_margin);
        formLanguageMonthMap(mActivity);
        // TCL ShenQianfeng End on 2016.08.19
    }

    private void formLanguageMonthMap(Context context) {
        int [] monthIds = {R.string.month_1, R.string.month_2, R.string.month_3, R.string.month_4, R.string.month_5, R.string.month_6, 
                R.string.month_7, R.string.month_8, R.string.month_9, R.string.month_10, R.string.month_11, R.string.month_12};
        for(int i = 0; i < monthIds.length; i++) {
            mLanguageMonthMap[i] = context.getString(monthIds[i]);
        }
    }
    

    public void setPressedIndex(int index) {
        if (mPressedIndex == index) return;
        mPressedIndex = index;
        mSlotView.invalidate();
    }
    
    // TCL ShenQianfeng Begin on 2016.07.19
    public int getPressedIndex() {
        return mPressedIndex;
    }
    // TCL ShenQianfeng End on 2016.07.19

    public void setPressedUp() {
        if (mPressedIndex == -1) return;
        mAnimatePressedUp = true;
        mSlotView.invalidate();
    }

    public void setHighlightItemPath(Path path) {
        if (mHighlightItemPath == path) return;
        mHighlightItemPath = path;
        mSlotView.invalidate();
    }

    public void setModel(AlbumDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mSlotView.setSlotCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new AlbumSlidingWindow(mActivity, model, CACHE_SIZE);
            mDataWindow.setListener(new MyDataModelListener());
            mSlotView.setSlotCount(model.size());

            // TCL ShenQianfeng Begin on 2016.07.12
            mDataWindow.setSlotViewStatusGetter(mSlotView);
            mSlotView.setUserInteractionListener(mDataWindow);
            //mSlotView.setOverscrollEffect(SlotView.OVERSCROLL_NONE);
            // TCL ShenQianfeng End on 2016.07.12
        }
    }

    private static Texture checkTexture(Texture texture) {
        return (texture instanceof TiledTexture)
                && !((TiledTexture) texture).isReady()
                ? null
                : texture;
    }
    
    @Override
    public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        if (mSlotFilter != null && !mSlotFilter.acceptSlot(index)) return 0;
        //LogUtil.d(TAG, "AlbumSlotRenderer::renderSlot");

        AlbumSlidingWindow.AlbumEntry entry = mDataWindow.get(index);

        int renderRequestFlags = 0;
        
        // TCL ShenQianfeng Begin on 2016.07.26
        if(entry == null) return 0;
        // TCL ShenQianfeng End on 2016.07.26

        Texture content = checkTexture(entry.content);
        if (content == null) {
            content = mWaitLoadingTexture;
            entry.isWaitDisplayed = true;
        } else if (entry.isWaitDisplayed) {
            entry.isWaitDisplayed = false;
            content = new FadeInTexture(mPlaceholderColor, entry.bitmapTexture);
            entry.content = content;
        }
        drawContent(canvas, content, width, height, entry.rotation);
        if ((content instanceof FadeInTexture) &&
                ((FadeInTexture) content).isAnimating()) {
            renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
        }

        if (entry.mediaType == MediaObject.MEDIA_TYPE_VIDEO) {
            drawVideoOverlay(canvas, width, height);
        }

        if (entry.isPanorama) {
            drawPanoramaIcon(canvas, width, height);
        }

        renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);

        return renderRequestFlags;
    }
    
    public int getLanguageCode() {
        boolean isChinese = GalleryUtils.isChineseLocale(mActivity);
        int languageCode = isChinese ? CustomStringTexture.LANG_CODE_CHINESE : CustomStringTexture.LANG_CODE_ENGLISH;
        return languageCode;
    }

    /**
     * @param yyyyMMText e.g. July in 2016 is displayed as 16/07
     * @param ddText which day the of month, ranges from 1 to 31
     */
    @Override
    public void renderString(GLCanvas canvas, String text, boolean dateMode) {
        int langCode = getLanguageCode();
        if(dateMode) {
            String yyyyMMText = text.substring(2, 7);//get "16/07"
            String ddText = text.substring(7, 9);//get "28"
            CustomStringTexture texture = DateGroupStringTextureCache.getStringTexture(mActivity, 
                    yyyyMMText, ddText, 
                    "", "", 
                    dateMode, 
                    langCode);
            int leftPadding = mSlotView.getSpec().slotDateAreaWidth - texture.getWidth() - mDateToSlotMargin;
            texture.draw(canvas, leftPadding , 0);
        } else {
            String yearText = text.substring(0, 4);//get "2016"
            String monthText = text.substring(5, 7);//get "06" 
            if(langCode == CustomStringTexture.LANG_CODE_CHINESE) {
                monthText = mapMonthDigitToChinese(monthText);
            }
            CustomStringTexture texture = DateGroupStringTextureCache.getStringTexture(mActivity, 
                    "", "", 
                    yearText, monthText, 
                    dateMode, 
                    langCode);
            int leftPadding = mSlotView.getSpec().slotDateAreaWidth - texture.getWidth() - mDateToSlotMargin;
            texture.draw(canvas, leftPadding , 0);
        }
    }
    
    /**
      * @param text always date-mode text 2016.07.21
      */
    @Override
    public void renderDateModeStringWhenScale(GLCanvas canvas, String text, boolean fromDateToMonth ,float progress) {
        String yyyyMMText = text.substring(2, 7);//get "16/07"
        String ddText = text.substring(7, 9);//get "28"
        int langCode = getLanguageCode();
        CustomStringTexture texture = DateGroupStringTextureCache.getStringTexture(mActivity, yyyyMMText, ddText, "", "", true, langCode);
        float alpha = fromDateToMonth ? progress : (1 - progress); 
        //LogUtil.d(TAG, "alpha:" + alpha + " progress:" + progress);
        int leftPadding = mSlotView.getSpec().slotDateAreaWidth - texture.getWidth() - mDateToSlotMargin;
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
        canvas.setAlpha(alpha);
        texture.draw(canvas, leftPadding , 0);
        canvas.restore();
    }
    
    //@param text always date-mode text 2016.07.21
    @Override
    public void renderMonthModeStringWhenScale(GLCanvas canvas, String text, boolean fromDateToMonth ,float progress) {
        String yearText = text.substring(0, 4);//get "2016"
        String monthText = text.substring(5, 7);//get "06" 
        int langCode = getLanguageCode();
        if(langCode == CustomStringTexture.LANG_CODE_CHINESE) {
            monthText = mapMonthDigitToChinese(monthText);
        }
        CustomStringTexture texture = DateGroupStringTextureCache.getStringTexture(mActivity, "", "", yearText, monthText, false, langCode);
        int leftPadding = mSlotView.getSpec().slotDateAreaWidth - texture.getWidth() - mDateToSlotMargin;
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
        canvas.setAlpha(progress);
        texture.draw(canvas, leftPadding , 0);
        canvas.restore();
    }

    private int renderOverlay(GLCanvas canvas, int index,
            AlbumSlidingWindow.AlbumEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                // TCL ShenQianfeng Begin on 2016.09.26
                // Annotated Below:
                /*
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
                */
                // TCL ShenQianfeng End on 2016.09.26
            } else {
                drawPressedFrame(canvas, width, height);
            }
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode && mSelectionManager.isItemSelected(entry.path)) {
            drawSelectedFrame(canvas, width, height);
        }
        return renderRequestFlags;
    }

    private class MyDataModelListener implements AlbumSlidingWindow.Listener {
        @Override
        public void onContentChanged() {
            mSlotView.invalidate();
        }

        @Override
        public void onSizeChanged(int size) {
            mSlotView.setSlotCount(size);
        }
    }

    public void resume() {
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = mSelectionManager.inSelectionMode();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            //LogUtil.i2(TAG, "onVisibleRangeChanged visibleStart:" + visibleStart + " visibleEnd:" + visibleEnd);
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onSlotSizeChanged(int width, int height) {
        // Do nothing
    }

    public void setSlotFilter(SlotFilter slotFilter) {
        mSlotFilter = slotFilter;
    }
    // TCL ShenQianfeng Begin on 2016.10.08

    public String mapMonthDigitToChinese(String digit) {
        int index = Integer.valueOf(digit) -1;
        return mLanguageMonthMap[index];
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        formLanguageMonthMap(mActivity);
        mSlotView.invalidate();
    }
    // TCL ShenQianfeng End on 2016.10.08
}
