/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-31
 * the tool to set search result span to a textView
 */
public class SearchResultSpanUtil {
    private static final String TAG = SearchResultSpanUtil.class.getSimpleName();
    private static int mViewWidth = 0;

    /**
     * set the color span for string
     *
     * @param content   the total string
     * @param substring the sub string ,if substring is empty or content don't have
     *                  substring, set all string to color
     * @param textView  the textView to set span string
     */
    public static void setSpan(String content, String substring, TextView textView, int colorId) {

        if (TextUtils.isEmpty(substring) || !content.contains(substring)) {
            textView.setText(content);
            if (textView.getId() == R.id.item_meeting_detail_label) {
                textView.setEllipsize(null);
            }
        } else {
            String result = getEllipsizeString(content, substring, textView);

            Context context = textView.getContext();
            int color = context.getResources().getColor(colorId, null);
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(result);
            int start = result.indexOf(substring);
            stringBuilder.setSpan(new ForegroundColorSpan(color), start, start + substring.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(stringBuilder);
        }

    }

    @NonNull
    private static String getEllipsizeString(String content, String substring, TextView textView) {
        textView.setEllipsize(TextUtils.TruncateAt.END);
        int start = content.indexOf(substring);
        if (mViewWidth == 0) {
            Resources resources = textView.getContext().getResources();
            mViewWidth = resources.getDimensionPixelSize(R.dimen.layout_common_340dp) -
                    resources.getDimensionPixelSize(R.dimen.layout_common_15dp) -
                    resources.getDimensionPixelSize(R.dimen.layout_common_15dp);
        }
        float textWid = getTextWidth(textView, substring);
        float allTextWid = getTextWidth(textView, content);
        MeetingLog.d(TAG, "viewWid=" + mViewWidth + "  textWid=" + textWid + "  allTextWid=" + allTextWid);

        //show all text
        if (allTextWid < mViewWidth) {
            return content;
        }

        int startIndex = start;
        int endIndex = start + substring.length();

        //show start text
        float leftWidth = (mViewWidth - textWid) / 2;
        MeetingLog.d(TAG, "leftWidth=" + leftWidth);
        if (startIndex == 0 || getTextWidth(textView, content.substring(0, startIndex)) < leftWidth) {
            MeetingLog.d(TAG, "show  start text");
            return content;
        }
        //show end text
        if (endIndex == content.length() - 1 || getTextWidth(textView, content.substring(endIndex)) < leftWidth) {
            textView.setEllipsize(TextUtils.TruncateAt.START);
            MeetingLog.d(TAG, "show  end text");
            return content;
        }
        MeetingLog.d(TAG, "Before:start index=" + startIndex + "  end index=" + endIndex);
//        int textcount = (int) (leftWidth / textWid * substring.length());
//        startIndex = startIndex - textcount;
//        if (startIndex < 0) {
//            startIndex = 0;
//        }
//        endIndex = endIndex + textcount;
//        if (endIndex > content.length() - 1) {
//            endIndex = content.length() - 1;
//        }
        MeetingLog.d(TAG, "After:start index=" + startIndex + "  end index=" + endIndex);
        while (true) {
            MeetingLog.d(TAG, "While:start index=" + startIndex + "  end index=" + endIndex);
            if (getTextWidth(textView, content.substring(startIndex, endIndex + 1) + "..." + "...") < mViewWidth) {
                if (startIndex > 0) {
                    startIndex--;
                }
                if (endIndex < content.length()) {
                    endIndex++;
                }
                if (startIndex == 0 && endIndex == content.length()) {
                    break;
                }

            } else {
                break;
            }
        }
        String result = content.substring(startIndex, endIndex + 1);
        MeetingLog.d(TAG, "show cut text=" + result);
        if (startIndex > 0) {
            result = "..." + result;
        }
        if (endIndex != content.length()) {
            result = result + "...";
        }
        MeetingLog.d(TAG, "cut text size=" + getTextWidth(textView, result));
        return result;
    }

    private static float getTextWidth(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        float[] width = new float[text.length()];
        paint.getTextWidths(text, width);
        float sum = 0;
        for (float oneWidth : width) {
            sum += oneWidth;
        }
        MeetingLog.d(TAG, "sum=" + sum + "  text=" + text);
        return sum;
    }
}
