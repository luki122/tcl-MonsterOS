/*
 * Copyright (c) 2013-2014, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

//import android.text.MeasuredText;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;

/**
 * An utility class for managing messages.
 */
//tangyisen copy from TextUtils
public class MessageTextUtils {
    public static final char REG_CHAR = '、';
    public static final String REG_STRING = "、";
    public static final CharSequence APPEND_STRING = "...";//"···";

    private static final Object[] sCached = new Object[3];
    private static final Object[] sLock = new Object[0];

    static Object obtain() {
        Object mtObject = null;
        try {
        Class<?> mt = Class.forName("android.text.MeasuredText");
        //Object mtObject = mt.newInstance();
        /*synchronized (sLock) {
            for (int i = sCached.length; --i >= 0;) {
                if (sCached[i] != null) {
                    mtObject = sCached[i];
                    sCached[i] = null;
                    return mtObject;
                }
            }
        }*/
        Constructor< ?> mtConstructor = mt.getDeclaredConstructor(null);
        mtConstructor.setAccessible(true);
        mtObject = mtConstructor.newInstance(null);
        }  catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return mtObject;
        }
    }

    static Object recycle(Object mt) {
        Class<?> mtClass = mt.getClass();
        try {
            Method finishFun = mtClass.getDeclaredMethod("finish");
            finishFun.setAccessible(true);
            finishFun.invoke(mt);
            // mt.finish();
            /*synchronized (sLock) {
                for (int i = 0; i < sCached.length; ++i) {
                    if (sCached[i] == null) {
                        sCached[i] = mt;
                        Field mTextField = mtClass.getDeclaredField("mText");
                        mTextField.set(mt, null);
                        // mt.mText = null;
                        break;
                    }
                }
            }*/
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return null;
        }
    }

    public static CharSequence commaEllipsize(CharSequence text, TextPaint p,
        float avail, String more, TextDirectionHeuristic textDir) {

        SpannableStringBuilder out = null;
        Object mtObject = obtain();
        try {
           Class<?> mt = Class.forName("android.text.MeasuredText");
           //Object mtObject = mt.newInstance();
           
           //MeasuredText mt = MeasuredText.obtain();
           int len = text.length();
           float width = setPara(mtObject, p, text, 0, len, textDir);
           if (width <= avail) {
               return text;
           }

           char[] buf = null;//mt.mChars;
           Field chars = mt.getDeclaredField("mChars");
           chars.setAccessible(true);
           Object ss = chars.get(mtObject);
           if (ss.getClass().isArray()) { // 判断是否是数组
               buf = (char[]) ss;
           }

           int commaCount = 0;
           for (int i = 0; i < len; i++) {
               if (buf[i] == REG_CHAR) {
                   commaCount++;
               }
           }

           int remaining = commaCount + 1;

           int ok = 0;
           String okFormat = "";

           int w = 0;
           float wNoLastRegChar = 0;
           int count = 0;
           Field widths1 = mt.getDeclaredField("mWidths");
           widths1.setAccessible(true);
           float[] widths = (float[])widths1.get(mtObject);//mt.mWidths;

           //MeasuredText tempMt = MeasuredText.obtain();
           //Object tempMt = mt.newInstance();
           Object tempMt = obtain();
           for (int i = 0; i < len; i++) {
               w += widths[i];

               if (buf[i] == REG_CHAR) {
                   wNoLastRegChar = w - widths[i];
                   count++;

                   String format;
                   // XXX should not insert spaces, should be part of string
                   // XXX should use plural rules and not assume English plurals
                   --remaining;
                   format = String.format(more, remaining);
                   //format = String.format(more, remaining);

                   // XXX this is probably ok, but need to look at it more
                   //tempMt.setPara(format, 0, format.length(), textDir, null);
                   Method setParaFun = mt.getDeclaredMethod("setPara",CharSequence.class,int.class,int.class,TextDirectionHeuristic.class,StaticLayout.Builder.class);
                   setParaFun.setAccessible(true);
                   setParaFun.invoke(tempMt, format, 0, format.length(), textDir, null);
                   //float moreWid = tempMt.addStyleRun(p, tempMt.mLen, null);
                   Method moreWidFun = mt.getDeclaredMethod("addStyleRun",TextPaint.class,int.class,Paint.FontMetricsInt.class);
                   moreWidFun.setAccessible(true);
                   Field mLenField = mt.getDeclaredField("mLen");
                   mLenField.setAccessible(true);
                   int tempMtMlen = (int)mLenField.get(tempMt);
                   float moreWid = (float)moreWidFun.invoke(tempMt, p, tempMtMlen, null);

                   //tangyisen w to wNoLastRegChar
                   if (wNoLastRegChar + moreWid <= avail) {
                       ok = i + 1;
                       okFormat = format;
                   }
               }
           }
           //MeasuredText.recycle(tempMt);
           recycle(tempMt);
           if(ok == 0) {
               int wid = 0;
               okFormat = APPEND_STRING + String.format(more, remaining);
               Method setParaFun = mt.getDeclaredMethod("setPara",CharSequence.class,int.class,int.class,TextDirectionHeuristic.class,StaticLayout.Builder.class);
               setParaFun.setAccessible(true);
               setParaFun.invoke(tempMt, okFormat, 0, okFormat.length(), textDir, null);
               Method moreWidFun = mt.getDeclaredMethod("addStyleRun",TextPaint.class,int.class,Paint.FontMetricsInt.class);
               moreWidFun.setAccessible(true);
               Field mLenField = mt.getDeclaredField("mLen");
               mLenField.setAccessible(true);
               int tempMtMlen = (int)mLenField.get(tempMt);
               float moreWid = (float)moreWidFun.invoke(tempMt, p, tempMtMlen, null);
               for (int i = 0; i < len; i++) {
                   wid += widths[i];
                   if (wid + moreWid <= avail) {
                       ok = i + 1;
                   }
               }
           }
           out = new SpannableStringBuilder(okFormat);
           out.insert(0, text, 0, ok - 1);//delete REG_CHAR
           return out;
       } catch (ClassNotFoundException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (IllegalAccessException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (NoSuchFieldException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (NoSuchMethodException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (IllegalArgumentException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (InvocationTargetException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } finally {
           //MeasuredText.recycle(mt);
           recycle(mtObject);
           return out;
       }
   }

   private static float setPara(Object mt, TextPaint paint,
           CharSequence text, int start, int end, TextDirectionHeuristic textDir) {

       //mt.setPara(text, start, end, textDir, null);
       float widthrtn = 0f;
       try {
            Class<?> mtClass = mt.getClass();
            Method setParaFun =
                mtClass.getDeclaredMethod("setPara",
                    CharSequence.class,
                    int.class,
                    int.class,
                    TextDirectionHeuristic.class, StaticLayout.Builder.class);
            setParaFun.setAccessible(true);
            setParaFun.invoke(mt, text, start, end, textDir, null);
            
            float width;
            Spanned sp = text instanceof Spanned ? (Spanned)text : null;
            int len = end - start;
            if (sp == null) {
                // width = mt.addStyleRun(paint, len, null);
                Method addStyleRunFun =
                    mtClass.getDeclaredMethod("addStyleRun", TextPaint.class, int.class, Paint.FontMetricsInt.class);
                addStyleRunFun.setAccessible(true);
                width = (float)addStyleRunFun.invoke(mt, paint, len, null);
            } else {
                width = 0;
                int spanEnd;
                for (int spanStart = 0; spanStart < len; spanStart = spanEnd) {
                    spanEnd = sp.nextSpanTransition(spanStart, len, MetricAffectingSpan.class);
                    MetricAffectingSpan[] spans = sp.getSpans(spanStart, spanEnd, MetricAffectingSpan.class);
                    spans = TextUtils.removeEmptySpans(spans, sp, MetricAffectingSpan.class);
                    // width += mt.addStyleRun(paint, spans, spanEnd - spanStart, null);
                    Method addStyleRunFun =
                        mtClass.getDeclaredMethod("addStyleRun",
                            TextPaint.class,
                            MetricAffectingSpan[].class,
                            int.class,
                            Paint.FontMetricsInt.class);
                    addStyleRunFun.setAccessible(true);
                    width += (float)addStyleRunFun.invoke(mt, paint, spans, spanEnd - spanStart, null);
                }
            }
            widthrtn = width;
            return width;
       } catch (IllegalAccessException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (NoSuchMethodException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (IllegalArgumentException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (InvocationTargetException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } finally {
           //MeasuredText.recycle(mt);
           recycle(mt);
           return widthrtn;
       }
   }

   public static CharSequence commaEllipsize(
       final String text,
       final TextPaint paint,
       final int width,
       final String more) {
   CharSequence ellipsized = commaEllipsize(
           text,
           paint,
           width,
           more,
           TextDirectionHeuristics.FIRSTSTRONG_LTR);
       if (TextUtils.isEmpty(ellipsized)) {
           ellipsized = text;
       }
       return ellipsized;
   }
}
