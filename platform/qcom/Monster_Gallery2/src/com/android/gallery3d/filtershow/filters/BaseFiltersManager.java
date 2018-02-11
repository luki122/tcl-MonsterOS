/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.gallery3d.filtershow.filters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.category.Action;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public abstract class BaseFiltersManager implements FiltersManagerInterface {
    protected HashMap<Class, ImageFilter> mFilters = null;
    protected HashMap<String, FilterRepresentation> mRepresentationLookup = null;
    private static final String LOGTAG = "BaseFiltersManager";
    
    // TCL ShenQianfeng Begin on 2016.08.22
    protected ArrayList<FilterRepresentation> mCrops = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mRotate = new ArrayList<FilterRepresentation>();
    // TCL ShenQianfeng End on 2016.08.22
    
    protected ArrayList<FilterRepresentation> mLooks = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mBorders = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mTools = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mEffects = new ArrayList<FilterRepresentation>();
    private static int mImageBorderSize = 4; // in percent

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    private ArrayList<Integer> filtersNormalIconId = new ArrayList<Integer>();
    private ArrayList<Integer> filtersSelectedIconId = new ArrayList<Integer>();
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

    protected void init() {
        mFilters = new HashMap<Class, ImageFilter>();
        mRepresentationLookup = new HashMap<String, FilterRepresentation>();
        Vector<Class> filters = new Vector<Class>();
        addFilterClasses(filters);
        for (Class filterClass : filters) {
            try {
                Object filterInstance = filterClass.newInstance();
                if (filterInstance instanceof ImageFilter) {
                    mFilters.put(filterClass, (ImageFilter) filterInstance);

                    FilterRepresentation rep =
                        ((ImageFilter) filterInstance).getDefaultRepresentation();
                    if (rep != null) {
                        addRepresentation(rep);
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRepresentation(FilterRepresentation rep) {
        mRepresentationLookup.put(rep.getSerializationName(), rep);
    }

    public FilterRepresentation createFilterFromName(String name) {
        try {
            return mRepresentationLookup.get(name).copy();
        } catch (Exception e) {
            Log.v(LOGTAG, "unable to generate a filter representation for \"" + name + "\"");
            e.printStackTrace();
        }
        return null;
    }

    public ImageFilter getFilter(Class c) {
        return mFilters.get(c);
    }

    @Override
    public ImageFilter getFilterForRepresentation(FilterRepresentation representation) {
        return mFilters.get(representation.getFilterClass());
    }

    public FilterRepresentation getRepresentation(Class c) {
        ImageFilter filter = mFilters.get(c);
        if (filter != null) {
            return filter.getDefaultRepresentation();
        }
        return null;
    }

    public void freeFilterResources(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        Vector<ImageFilter> usedFilters = preset.getUsedFilters(this);
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (!usedFilters.contains(filter)) {
                filter.freeResources();
            }
        }
    }

    public void freeRSFilterScripts() {
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (filter != null && filter instanceof ImageFilterRS) {
                ((ImageFilterRS) filter).resetScripts();
            }
        }
    }

    protected void addFilterClasses(Vector<Class> filters) {
        filters.add(ImageFilterTinyPlanet.class);
        filters.add(ImageFilterRedEye.class);
        filters.add(ImageFilterWBalance.class);
        filters.add(ImageFilterExposure.class);
        filters.add(ImageFilterVignette.class);
        filters.add(ImageFilterGrad.class);
        filters.add(ImageFilterContrast.class);
        filters.add(ImageFilterShadows.class);
        filters.add(ImageFilterHighlights.class);
        filters.add(ImageFilterVibrance.class);
        filters.add(ImageFilterSharpen.class);
        filters.add(ImageFilterCurves.class);
        filters.add(ImageFilterDraw.class);
        filters.add(ImageFilterHue.class);
        filters.add(ImageFilterChanSat.class);
        filters.add(ImageFilterSaturated.class);
        filters.add(ImageFilterBwFilter.class);
        filters.add(ImageFilterNegative.class);
        filters.add(ImageFilterEdge.class);
        filters.add(ImageFilterKMeans.class);
        filters.add(ImageFilterFx.class);
        filters.add(ImageFilterBorder.class);
        filters.add(ImageFilterColorBorder.class);
    }
    
    // TCL ShenQianfeng Begin on 2016.08.22
    public ArrayList<FilterRepresentation> getCrops() {
        return mCrops;
    }
    
    public ArrayList<FilterRepresentation> getRotate() {
        return mRotate;
    }
    // TCL ShenQianfeng End on 2016.08.22

    public ArrayList<FilterRepresentation> getLooks() {
        return mLooks;
    }

    public ArrayList<FilterRepresentation> getBorders() {
        return mBorders;
    }

    public ArrayList<FilterRepresentation> getTools() {
        return mTools;
    }

    public ArrayList<FilterRepresentation> getEffects() {
        return mEffects;
    }

    public void addBorders(Context context) {

        // Do not localize
        String[] serializationNames = {
                "FRAME_4X5",
                "FRAME_BRUSH",
                "FRAME_GRUNGE",
                "FRAME_SUMI_E",
                "FRAME_TAPE",
                "FRAME_BLACK",
                "FRAME_BLACK_ROUNDED",
                "FRAME_WHITE",
                "FRAME_WHITE_ROUNDED",
                "FRAME_CREAM",
                "FRAME_CREAM_ROUNDED"
        };

        // The "no border" implementation
        int i = 0;
        FilterRepresentation rep = new FilterImageBorderRepresentation(0);
        mBorders.add(rep);

        // Regular borders
        ArrayList <FilterRepresentation> borderList = new ArrayList<FilterRepresentation>();


        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_4x5);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_brush);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_grunge);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_sumi_e);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_tape);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        int creamColor = Color.argb(255, 237, 237, 227);
        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        for (FilterRepresentation filter : borderList) {
            filter.setSerializationName(serializationNames[i++]);
            addRepresentation(filter);
            mBorders.add(filter);
        }
    }
    // TCL ShenQianfeng Begin on 2016.08.23
    // Annotated Below:
    public void addCrops(Context context) {
        int[] textId = {
                R.string.crop_7_5,
                R.string.crop_4_3,
                R.string.crop_1_1,
                R.string.crop_9_16,
                R.string.crop_5_7,
        };

        int[] overlayId = {
                R.drawable.ic_7_5_normal,
                R.drawable.ic_4_3_normal,
                R.drawable.ic_1_1_normal,
                R.drawable.ic_9_16_normal,
                R.drawable.ic_5_7_normal,
        };
        
        int[] selectedOverlayId = {
                R.drawable.ic_7_5_selected,
                R.drawable.ic_4_3_selected,
                R.drawable.ic_1_1_selected,
                R.drawable.ic_9_16_selected,
                R.drawable.ic_5_7_selected,
        };
        
        boolean hightCategoryText [] = {true, true, true, true, true};

        FilterRepresentation[] geometryFilters = {
                new FilterCropRepresentation(),
                new FilterCropRepresentation(),
                new FilterCropRepresentation(),
                new FilterCropRepresentation(),
                new FilterCropRepresentation(),
        };

        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setSelectedOverlayId(selectedOverlayId[i]);
            geometry.setHighlightCategoryText(hightCategoryText[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mCrops.add(geometry);
        }
    }
    // TCL ShenQianfeng End on 2016.08.23
    
    public void addRotate(Context context) {
        int[] textId = {
                R.string.mst_rotate_left,
                R.string.mst_rotate_right,
                R.string.mst_rotate_vertical,
                R.string.mst_rotate_horizontal,
        };

        int[] overlayId = {
                R.drawable.mst_rotate_left,
                R.drawable.mst_rotate_right,
                R.drawable.mst_rotate_vertically,
                R.drawable.mst_rotate_horizontally,
        };
        
        boolean hightCategoryText [] = {false, false, false, false};

        FilterRotateRepresentation rotateRep = new FilterRotateRepresentation();
        FilterRepresentation[] geometryFilters = {
                new FilterRotateRepresentation(true), //rotate left, mIsCounterClockwise is not set, we will set this when user click CategoryView
                new FilterRotateRepresentation(false),  //rotate right, mIsCounterClockwise is not set, we will set this when user click CategoryView
                new FilterMirrorRepresentation(false),//mirror vertical
                new FilterMirrorRepresentation(true),//mirro horizontal
        };

        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setHighlightCategoryText(hightCategoryText[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mRotate.add(geometry);
        }
    }

    public void addLooks(Context context) {
        int[] drawid = {
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] fxNameid = {
                R.string.ffx_punch,
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_washout,
                R.string.ffx_blue_crush,
                R.string.ffx_washout_color,
                R.string.ffx_x_process
        };
        
        // Do not localize.
        String[] serializationNames = {
                "LUT3D_PUNCH",
                "LUT3D_VINTAGE",
                "LUT3D_BW",
                "LUT3D_BLEACH",
                "LUT3D_INSTANT",
                "LUT3D_WASHOUT",
                "LUT3D_BLUECRUSH",
                "LUT3D_WASHOUT_COLOR",
                "LUT3D_XPROCESS"
        };
        
        boolean hightCategoryText [] = {true, true, true, true, true, true, true, true, true};

        FilterFxRepresentation nullFx = new FilterFxRepresentation(context.getString(R.string.none), 0, R.string.none);
        // TCL ShenQianfeng Begin on 2016.09.05
        nullFx.setSelectedBorderOverlayId(R.drawable.mst_ic_selected_border);
        nullFx.setHighlightCategoryText(true);
        // TCL ShenQianfeng End on 2016.09.05
        mLooks.add(nullFx);

        for (int i = 0; i < drawid.length; i++) {
            FilterFxRepresentation fx = new FilterFxRepresentation(context.getString(fxNameid[i]), drawid[i], fxNameid[i]);
            fx.setSerializationName(serializationNames[i]);
            ImagePreset preset = new ImagePreset();
            preset.addFilter(fx);
            FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(context.getString(fxNameid[i]), preset, -1);
            // TCL ShenQianfeng Begin on 2016.09.05
            rep.setSelectedBorderOverlayId(R.drawable.mst_ic_selected_border);
            rep.setHighlightCategoryText(hightCategoryText[i]);
            // TCL ShenQianfeng End on 2016.09.05
            mLooks.add(rep);
            addRepresentation(fx);
        }
    }

    public void addEffects(Context context) {
        // TCL ShenQianfeng Begin on 2016.08.29
        // Original:
        /*
        mEffects.add(getRepresentation(ImageFilterTinyPlanet.class));
        mEffects.add(getRepresentation(ImageFilterWBalance.class));
        mEffects.add(getRepresentation(ImageFilterExposure.class));
        mEffects.add(getRepresentation(ImageFilterVignette.class));
        mEffects.add(getRepresentation(ImageFilterGrad.class));
        mEffects.add(getRepresentation(ImageFilterContrast.class));
        mEffects.add(getRepresentation(ImageFilterShadows.class));
        mEffects.add(getRepresentation(ImageFilterHighlights.class));
        mEffects.add(getRepresentation(ImageFilterVibrance.class));
        mEffects.add(getRepresentation(ImageFilterSharpen.class));
        mEffects.add(getRepresentation(ImageFilterCurves.class));
        mEffects.add(getRepresentation(ImageFilterHue.class));
        mEffects.add(getRepresentation(ImageFilterChanSat.class));
        mEffects.add(getRepresentation(ImageFilterBwFilter.class));
        mEffects.add(getRepresentation(ImageFilterNegative.class));
        mEffects.add(getRepresentation(ImageFilterEdge.class));
        mEffects.add(getRepresentation(ImageFilterKMeans.class));
         */
        // Modify To:
        Class<?> [] classes = {
                ImageFilterWBalance.class,
                ImageFilterExposure.class,
                ImageFilterContrast.class,
                ImageFilterChanSat.class,
                ImageFilterBwFilter.class,
        };
        
        int overlayId [] = {
                R.drawable.mst_ic_white_balance,
                R.drawable.mst_ic_exposure,
                R.drawable.mst_ic_contrast,
                R.drawable.mst_ic_saturation,
                R.drawable.mst_ic_bwfilter,
        };
        int selectedOverlayId [] = { R.drawable.mst_ic_white_balance_selected, 0, 0, 0, 0 };
        boolean hightCategoryText [] = {true, false, false, false, false};
        
        int index = 0;
        for(Class<?> cls : classes) {
            FilterRepresentation rep = getRepresentation(cls);
            if (rep.getTextId() != 0) {
                rep.setName(context.getString(rep.getTextId()));
            }
            rep.setOverlayId(overlayId[index]);
            rep.setSelectedOverlayId(selectedOverlayId[index]);
            rep.setHighlightCategoryText(hightCategoryText[index]);
            //rep.setSelectedBorderOverlayId(selectedBorderOverlayId[index]);
            mEffects.add(rep);
            index ++;
        }
        // TCL ShenQianfeng End on 2016.08.29
        
    }

    public void addTools(Context context) {

        int[] textId = {
                R.string.crop,
                R.string.straighten,
                R.string.rotate,
                R.string.mirror
        };

        int[] overlayId = {
                R.drawable.ic_edit_crop,
                R.drawable.ic_edit_straighten,
                R.drawable.ic_edit_rotate,
                R.drawable.ic_edit_mirror
        };

        FilterRepresentation[] geometryFilters = {
                new FilterCropRepresentation(),
                new FilterStraightenRepresentation(),
                new FilterRotateRepresentation(),
                new FilterMirrorRepresentation()
        };

        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mTools.add(geometry);
        }

        //mTools.add(getRepresentation(ImageFilterRedEye.class));
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-26, PR910842 begin
        FilterRepresentation filterRepresentation = getRepresentation(ImageFilterDraw.class);
        filterRepresentation.setName(context.getString(R.string.imageDraw));
        mTools.add(filterRepresentation);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-26, PR910842 end
    }

    public void removeRepresentation(ArrayList<FilterRepresentation> list,
                                          FilterRepresentation representation) {
        for (int i = 0; i < list.size(); i++) {
            FilterRepresentation r = list.get(i);
            if (r.getFilterClass() == representation.getFilterClass()) {
                list.remove(i);
                break;
            }
        }
    }

    public void setFilterResources(Resources resources) {
        ImageFilterBorder filterBorder = (ImageFilterBorder) getFilter(ImageFilterBorder.class);
        filterBorder.setResources(resources);
        ImageFilterFx filterFx = (ImageFilterFx) getFilter(ImageFilterFx.class);
        filterFx.setResources(resources);
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
//    public ArrayList<Integer> getFiltersNormalIconId() {
//        
//        // TCL ShenQianfeng Begin on 2016.08.29
//        // Original:
//        /*
//        filtersNormalIconId.add(R.drawable.ic_adjust_autocolor_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_exposure_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_vignette_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_graduated_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_contrast_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_shadow_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_highlights_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_vibrance_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_sharpness_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_curves_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_hue_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_saturation_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_bwfilter_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_negative_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_edges_deselected);
//        filtersNormalIconId.add(R.drawable.ic_adjust_posterize_deselected);
//
//        filtersNormalIconId.add(R.drawable.ic_adjust_autocolor_deselected);
//        */
//        // Modify To:
//        filtersNormalIconId.add(R.drawable.mst_ic_white_balance);
//        filtersNormalIconId.add(R.drawable.mst_ic_exposure);
//        filtersNormalIconId.add(R.drawable.mst_ic_contrast);
//        filtersNormalIconId.add(R.drawable.mst_ic_saturation);
//        filtersNormalIconId.add(R.drawable.mst_ic_bwfilter);
//
//        filtersNormalIconId.add(R.drawable.mst_ic_white_balance);
//        // TCL ShenQianfeng End on 2016.08.29
//
//
//        return filtersNormalIconId;
//    }

//    public ArrayList<Integer> getFiltersSelectedIconId() {
//        
//        // TCL ShenQianfeng Begin on 2016.08.29
//        // Original:
//        /*
//        filtersSelectedIconId.add(R.drawable.ic_adjust_autocolor);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_exposure);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_vignette);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_graduated);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_contrast);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_shadow);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_highlights);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_vibrance);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_sharpness);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_curves);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_hue);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_saturation);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_bwfilter);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_negative);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_edges);
//        filtersSelectedIconId.add(R.drawable.ic_adjust_posterize);
//
//        filtersSelectedIconId.add(R.drawable.ic_adjust_autocolor);
//        */
//        // Modify To:
//        filtersSelectedIconId.add(R.drawable.mst_ic_white_balance_selected);
//        filtersSelectedIconId.add(R.drawable.mst_ic_exposure_selected);
//        filtersSelectedIconId.add(R.drawable.mst_ic_contrast_selected);
//        filtersSelectedIconId.add(R.drawable.mst_ic_saturation_selected);
//        filtersSelectedIconId.add(R.drawable.mst_ic_bwfilter_selected);
//
//        filtersSelectedIconId.add(R.drawable.mst_ic_white_balance_selected);
//        // TCL ShenQianfeng End on 2016.08.29
//
//
//        return filtersSelectedIconId;
//    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end
}
