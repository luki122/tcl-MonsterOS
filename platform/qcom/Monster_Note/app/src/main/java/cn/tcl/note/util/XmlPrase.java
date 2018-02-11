/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import cn.tcl.note.R;
import cn.tcl.note.data.CommonData;
import cn.tcl.note.data.NoteAudioData;
import cn.tcl.note.data.NotePicData;
import cn.tcl.note.data.NoteTextData;
import cn.tcl.note.db.DBData;

/**
 * XML example:
 * <notes>
 * <note>
 * <text>this is a text note!</text>
 * <flag>1</flag>
 * </note>
 * <note>
 * <image>img name</image>
 * </note>
 * <note>
 * <audio>audio name</audio>
 * <duration>00:20:30</duration>
 * </note>
 * </notes>
 */
public class XmlPrase {
    private static String TAG = XmlPrase.class.getSimpleName();
    private final static String TAG_NOTES_TYPE = "notes";
    public final static String TAG_NOTE_TYPE = "note";
    public final static String TAG_TEXT_TYPE = "text";
    public final static String TAG_TEXT_FLAG = "flag";
    public final static String TAG_IMG_TYPE = "image";
    public final static String TAG_AUDIO_TYPE = "audio";
    public final static String TAG_AUDIO_DURA = "duration";
    private final static String[][] SPECIAL_CHAR = new String[][]{
            {"&", "&amp;"},
            {"<", "&lt;"},
            {">", "&gt;"}
    };

    private static String replaceSpecialChar(String data, int before, int after) {
        for (int i = 0; i < SPECIAL_CHAR.length; i++) {
            data = data.replaceAll(SPECIAL_CHAR[i][before], SPECIAL_CHAR[i][after]);
        }
        return data;
    }

    public static String replaceSpecailCharBefore(String data) {
        return replaceSpecialChar(data, 0, 1);
    }

    public static String replaceSpecailCharAfter(String data) {
        return replaceSpecialChar(data, 1, 0);
    }

    static public LinkedList<CommonData> prase(String xmlString) {
        LinkedList<CommonData> result = null;
        CommonData data = null;
        if (xmlString == null) {
            return result;
        }
        InputStream is = new ByteArrayInputStream(xmlString.getBytes());
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, "UTF-8");
            int evenType = parser.getEventType();
            String getName;
            while (evenType != XmlPullParser.END_DOCUMENT) {
                switch (evenType) {
                    case XmlPullParser.START_DOCUMENT:
                        result = new LinkedList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        getName = parser.getName();
                        if (getName.equals(TAG_TEXT_TYPE)) {
                            data = new NoteTextData();
                            String str = replaceSpecailCharAfter(parser.nextText());
                            ((NoteTextData) data).setText(str);
                        } else if (getName.equals(TAG_TEXT_FLAG)) {
                            ((NoteTextData) data).setFlag(Integer.parseInt(parser.nextText()));
                        } else if (getName.equals(TAG_IMG_TYPE)) {
                            String str = replaceSpecailCharAfter(parser.nextText());
                            data = new NotePicData(str);
                        } else if (getName.equals(TAG_AUDIO_TYPE)) {
                            String str = replaceSpecailCharAfter(parser.nextText());
                            data = new NoteAudioData(str, 0);
                        } else if (getName.equals(TAG_AUDIO_DURA)) {
                            ((NoteAudioData) data).setDuration(Long.parseLong(parser.nextText()));
                        } else if (getName.equals("note")) {

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("note")) {
                            if (data instanceof NoteAudioData) {
                                final NoteAudioData audioData = (NoteAudioData) data;
                                if (audioData.getDuration() < 0) {
                                    String fileName = FileUtils.getAudioWholePath(audioData.getFileName());
                                    WriteWav.writeWaveFile(new File(fileName));
                                    long time = WriteWav.getAudioDura(fileName);
                                    audioData.setDuration(time);
                                }
                            }
                            result.add(data);
                        }
                        break;
                }
                evenType = parser.next();
            }

        } catch (XmlPullParserException e) {
            NoteLog.e(TAG, "XmlPullParserException error:", e);
        } catch (IOException e) {
            NoteLog.e(TAG, "IOException error:", e);
        }
        if (NoteLog.DEBUG) {
            for (int i = 0; i < result.size(); i++) {
                NoteLog.d(TAG, "" + i + ":" + result.get(i).toString());
            }

        }
        return result;
    }

    public static ContentValues toContentValues(LinkedList<CommonData> allData) {
        String[] result = toXml(allData);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBData.COLUMN_XML, result[0]);
        contentValues.put(DBData.COLUMN_FIRSTLINE, result[1]);
        contentValues.put(DBData.COLUMN_SECOND_LINE, result[2]);
        contentValues.put(DBData.COLUMN_WILL, result[3]);
        contentValues.put(DBData.COLUMN_IMG, result[4]);
        contentValues.put(DBData.COLUMN_AUDIO, result[5]);
        return contentValues;
    }

    public static String[] toXml(LinkedList<CommonData> allData) {
        String firstLine = new String();
        String secondLine = new String();
        int willdo = 0;
        int imgNum = 0;
        int audioNum = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(startTag(TAG_NOTES_TYPE));
        for (CommonData data : allData) {
            //get first line string ,second line string ,is have will do ,img num,audio num
            if (data instanceof NoteTextData) {
                NoteTextData textData = (NoteTextData) data;
                String text = textData.getText();
                if (isBlankStr(text)) {
                    if (firstLine.length() == 0) {
                        firstLine = textData.getText();
                    } else if (secondLine.length() == 0) {
                        secondLine = textData.getText();
                    }
                }
                if (willdo == 0) {
                    if (textData.getFlag() == NoteTextData.FLAG_WILLDO_UN
                            || textData.getFlag() == NoteTextData.FLAG_WILLDO_CK) {
                        willdo = 1;
                    }
                }
            } else if (data instanceof NotePicData) {
                imgNum++;
            } else if (data instanceof NoteAudioData) {
                audioNum++;
            }
            sb.append(data.toXmlString());
        }
        sb.append(endTag(TAG_NOTES_TYPE));
        String context = sb.toString();
        NoteLog.d(TAG, "build xml is " + context);
        NoteLog.d(TAG, "first line is" + firstLine + "  second line is" + secondLine);
        NoteLog.d(TAG, "willDo=" + willdo + "  imgNum=" + imgNum + "  audioNum=" + audioNum);
        String[] result = new String[]{context, firstLine, secondLine, "" + willdo, "" + imgNum, "" + audioNum};
        return result;
    }

    public static boolean isBlankStr(String str) {
        String after = str.replaceAll(" ", "");
//        NoteLog.d(TAG,"isBlankStr before="+str);
//        NoteLog.d(TAG,"isBlankStr after="+after);
        if (after.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String buildXml(String tag, String data) {
        StringBuilder sb = new StringBuilder();


        sb.append(startTag(tag)).append(data).append(endTag(tag));
        return sb.toString();
    }

    private static String startTag(String tag) {
        return "<" + tag + ">";
    }

    private static String endTag(String tag) {
        return "</" + tag + ">";
    }

    public static void presetNote(Context context, SQLiteDatabase db) {
        String title = context.getString(R.string.dialog_back_title);
        String firstNote = context.getString(R.string.preset_note_first);
        String secondNote = context.getString(R.string.preset_note_second);
        for (String line : new String[]{firstNote, secondNote}) {
            ContentValues contentValues = createNote(title, line);
            contentValues.put(DBData.COLUMN_TIME, TimeUtils.formatCurrentTime());
            db.insert(DBData.TABLE_NAME, null, contentValues);
        }
    }

    private static ContentValues createNote(String firstLine, String secondLine) {
        NoteTextData firstData = new NoteTextData(firstLine, NoteTextData.FLAG_NO);
        NoteTextData secondData = new NoteTextData(secondLine, NoteTextData.FLAG_NO);
        LinkedList<CommonData> allData = new LinkedList<>();
        allData.add(firstData);
        allData.add(secondData);
        return toContentValues(allData);
    }
}
