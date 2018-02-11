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

package com.android.camera.util;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import com.android.camera.debug.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class to read/write xmp from a jpeg image file. It only supports jpeg
 * image format, and doesn't support extended xmp now.
 * To use it:
 * XMPMeta xmpMeta = XmpUtil.extractOrCreateXMPMeta(filename);
 * xmpMeta.setProperty(PanoConstants.GOOGLE_PANO_NAMESPACE, "property_name", "value");
 * XmpUtil.writeXMPMeta(filename, xmpMeta);
 * <p/>
 * Or if you don't care the existing XMP meta data in image file:
 * XMPMeta xmpMeta = XmpUtil.createXMPMeta();
 * xmpMeta.setPropertyBoolean(PanoConstants.GOOGLE_PANO_NAMESPACE, "bool_property_name", "true");
 * XmpUtil.writeXMPMeta(filename, xmpMeta);
 */
public class XmpUtil {
    private static final Log.Tag TAG = new Log.Tag("XmpUtil");
    private static final int XMP_HEADER_SIZE = 29;
    private static final String XMP_HEADER = "http://ns.adobe.com/xap/1.0/\0";
    private static final int MAX_XMP_BUFFER_SIZE = 65502;

    private static final String GOOGLE_PANO_NAMESPACE = "http://ns.google.com/photos/1.0/panorama/";
    private static final String PANO_PREFIX = "GPano";

    private static final String XMP_EXTENSION_HEADER = "http://ns.adobe.com/xmp/extension/\0";  //35
    private static final int XMP_EXTENSION_HEADER_GUID_SIZE =
            XMP_EXTENSION_HEADER.length() + 32; // 32 byte GUID
    private static final int XMP_EXTENSION_HEADER_OFFSET = 8; //4 all len + 4 all offset.
    private static final int MAX_XMP_EXTENDED_SIZE = 65400;

    private static final String GOOGLE_AUDIO_NAMESPACE = "http://ns.google.com/photos/1.0/audio/";
    private static final String AUDIO_PREFIX = "GAudio";
    private static final String MIME = "Mime";
    private static final String AUDIO_MINI = "audio/mp4a-latm";

    private static final String ADOBE_NOTE_NAMESPACE = "http://ns.adobe.com/xmp/note/";
    private static final String NOTE_PREFIX = "xmpNote";

    private static final String XMP_HAS_EXTENSION = "HasExtendedXMP";

    private static final int M_SOI = 0xd8; // File start marker.
    private static final int M_APP1 = 0xe1; // Marker for Exif or XMP.
    private static final int M_SOS = 0xda; // Image data marker.

    // Jpeg file is composed of many sections and image data. This class is used
    // to hold the section data from image file.
    private static class Section {
        public int marker;
        public int length;
        public byte[] data;
    }

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace(
                    GOOGLE_PANO_NAMESPACE, PANO_PREFIX);
            XMPMetaFactory.getSchemaRegistry().registerNamespace(
                    GOOGLE_AUDIO_NAMESPACE, AUDIO_PREFIX);
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts XMPMeta from JPEG image file.
     *
     * @param filename JPEG image file name.
     * @return Extracted XMPMeta or null.
     */
    public static XMPMeta extractXMPMeta(String filename) {
        if (!filename.toLowerCase().endsWith(".jpg")
                && !filename.toLowerCase().endsWith(".jpeg")) {
            Log.d(TAG, "XMP parse: only jpeg file is supported");
            return null;
        }

        try {
            return extractXMPMeta(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not read file: " + filename, e);
            return null;
        }
    }

    /**
     * Extracts XMPMeta from a JPEG image file stream.
     *
     * @param is the input stream containing the JPEG image file.
     * @return Extracted XMPMeta or null.
     */
    public static XMPMeta extractXMPMeta(InputStream is) {
        List<Section> sections = parse(is, true);
        if (sections == null) {
            return null;
        }
        // Now we don't support extended xmp.
        for (Section section : sections) {
            if (hasXMPHeader(section.data)) {
                int end = getXMPContentEnd(section.data);
                byte[] buffer = new byte[end - XMP_HEADER_SIZE];
                System.arraycopy(
                        section.data, XMP_HEADER_SIZE, buffer, 0, buffer.length);
                try {
                    XMPMeta result = XMPMetaFactory.parseFromBuffer(buffer);
                    return result;
                } catch (XMPException e) {
                    Log.d(TAG, "XMP parse error", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Creates a new XMPMeta.
     */
    public static XMPMeta createXMPMeta() {
        return XMPMetaFactory.create();
    }

    /**
     * Tries to extract XMP meta from image file first, if failed, create one.
     */
    public static XMPMeta extractOrCreateXMPMeta(String filename) {
        XMPMeta meta = extractXMPMeta(filename);
        return meta == null ? createXMPMeta() : meta;
    }

    /**
     * Writes the XMPMeta to the jpeg image file.
     */
    public static boolean writeXMPMeta(String filename, XMPMeta meta) {
        if (!filename.toLowerCase().endsWith(".jpg")
                && !filename.toLowerCase().endsWith(".jpeg")) {
            Log.d(TAG, "XMP parse: only jpeg file is supported");
            return false;
        }
        List<Section> sections = null;
        try {
            sections = parse(new FileInputStream(filename), false);
            sections = insertXMPSection(sections, meta);
            if (sections == null) {
                return false;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not read file: " + filename, e);
            return false;
        }
        FileOutputStream os = null;
        try {
            // Overwrite the image file with the new meta data.
            os = new FileOutputStream(filename);
            writeJpegFile(os, sections);
        } catch (IOException e) {
            Log.d(TAG, "Write file failed:" + filename, e);
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
        return true;
    }

    /**
     * Updates a jpeg file from inputStream with XMPMeta to outputStream.
     */
    public static boolean writeXMPMeta(InputStream inputStream, OutputStream outputStream,
                                       XMPMeta meta) {
        List<Section> sections = parse(inputStream, false);
        sections = insertXMPSection(sections, meta);
        if (sections == null) {
            return false;
        }
        try {
            // Overwrite the image file with the new meta data.
            writeJpegFile(outputStream, sections);
        } catch (IOException e) {
            Log.d(TAG, "Write to stream failed", e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
        return true;
    }

    public static boolean writeXMPMeta(String filename, XMPMeta meta,
                                       XMPMeta audioMeta) {
        if (!filename.toLowerCase().endsWith(".jpg")
                && !filename.toLowerCase().endsWith(".jpeg")) {
            Log.d(TAG, "XMP parse: only jpeg file is supported");
            return false;
        }
        List<Section> sections = null;
        try {
            sections = parse(new FileInputStream(filename), false);
            sections = insertXMPSection(sections, meta, audioMeta);
            if (sections == null) {
                return false;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not read file: " + filename, e);
            return false;
        }
        FileOutputStream os = null;
        try {
            // Overwrite the image file with the new meta data.
            os = new FileOutputStream(filename);
            writeJpegFile(os, sections);
        } catch (IOException e) {
            Log.d(TAG, "Write file failed:" + filename, e);
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
        return true;
    }

    /**
     * Write a list of sections to a Jpeg file.
     */
    private static void writeJpegFile(OutputStream os, List<Section> sections)
            throws IOException {
        // Writes the jpeg file header.
        os.write(0xff);
        os.write(M_SOI);
        for (Section section : sections) {
            os.write(0xff);
            os.write(section.marker);
            if (section.length > 0) {
                // It's not the image data.
                int lh = section.length >> 8;
                int ll = section.length & 0xff;
                os.write(lh);
                os.write(ll);
            }
            os.write(section.data);
        }
    }

    private static List<Section> insertXMPSection(
            List<Section> sections, XMPMeta meta) {
        if (sections == null || sections.size() <= 1) {
            return null;
        }
        byte[] buffer;
        try {
            SerializeOptions options = new SerializeOptions();
            options.setUseCompactFormat(true);
            // We have to omit packet wrapper here because
            // javax.xml.parsers.DocumentBuilder
            // fails to parse the packet end <?xpacket end="w"?> in android.
            options.setOmitPacketWrapper(true);
            buffer = XMPMetaFactory.serializeToBuffer(meta, options);
        } catch (XMPException e) {
            Log.d(TAG, "Serialize xmp failed", e);
            return null;
        }
        if (buffer.length > MAX_XMP_BUFFER_SIZE) {
            // Do not support extended xmp now.
            return null;
        }
        // The XMP section starts with XMP_HEADER and then the real xmp data.
        byte[] xmpdata = new byte[buffer.length + XMP_HEADER_SIZE];
        System.arraycopy(XMP_HEADER.getBytes(), 0, xmpdata, 0, XMP_HEADER_SIZE);
        System.arraycopy(buffer, 0, xmpdata, XMP_HEADER_SIZE, buffer.length);
        Section xmpSection = new Section();
        xmpSection.marker = M_APP1;
        // Adds the length place (2 bytes) to the section length.
        xmpSection.length = xmpdata.length + 2;
        xmpSection.data = xmpdata;

        for (int i = 0; i < sections.size(); ++i) {
            // If we can find the old xmp section, replace it with the new one.
            if (sections.get(i).marker == M_APP1
                    && hasXMPHeader(sections.get(i).data)) {
                // Replace with the new xmp data.
                sections.set(i, xmpSection);
                return sections;
            }
        }
        // If the first section is Exif, insert XMP data before the second section,
        // otherwise, make xmp data the first section.
        List<Section> newSections = new ArrayList<Section>();
        int position = (sections.get(0).marker == M_APP1) ? 1 : 0;
        newSections.addAll(sections.subList(0, position));
        newSections.add(xmpSection);
        newSections.addAll(sections.subList(position, sections.size()));
        return newSections;
    }

    private static List<Section> insertXMPSection(
            List<Section> sections, XMPMeta meta, XMPMeta audioMeta) {
        if (sections == null || sections.size() <= 1) {
            return null;
        }
        byte[] buffer;
        byte[] extendedBuffer = null;
        String extendedString = "";
        try {
            SerializeOptions options = new SerializeOptions();
            options.setUseCompactFormat(true);
            // We have to omit packet wrapper here because
            // javax.xml.parsers.DocumentBuilder
            // fails to parse the packet end <?xpacket end="w"?> in android.
            options.setOmitPacketWrapper(true);

            if (audioMeta != null) {
                extendedBuffer = XMPMetaFactory.serializeToBuffer(audioMeta, options);
                extendedString = calExtendedString(extendedBuffer);
                insertExtendedString(meta, extendedString);
            }

            buffer = XMPMetaFactory.serializeToBuffer(meta, options);
        } catch (XMPException e) {
            Log.d(TAG, "Serialize xmp failed", e);
            return null;
        }
        if (buffer.length > MAX_XMP_BUFFER_SIZE) {
            // Do not support extended xmp now.
            return null;
        }
        // The XMP section starts with XMP_HEADER and then the real xmp data.
        byte[] xmpdata = new byte[buffer.length + XMP_HEADER_SIZE];
        System.arraycopy(XMP_HEADER.getBytes(), 0, xmpdata, 0, XMP_HEADER_SIZE);
        System.arraycopy(buffer, 0, xmpdata, XMP_HEADER_SIZE, buffer.length);
        Section xmpSection = new Section();
        xmpSection.marker = M_APP1;
        // Adds the length place (2 bytes) to the section length.
        xmpSection.length = xmpdata.length + 2;
        xmpSection.data = xmpdata;

        for (int i = 0; i < sections.size(); ++i) {
            // If we can find the old xmp section, replace it with the new one.
            if (sections.get(i).marker == M_APP1
                    && hasXMPHeader(sections.get(i).data)) {
                // Replace with the new xmp data.
                // sections.set(i, xmpSection);
                List<Section> newSections = new ArrayList<Section>();
                newSections.addAll(sections.subList(0, i));
                newSections.add(xmpSection);
                if (audioMeta != null) {
                    newSections = insertExtendedSection(
                            newSections, extendedBuffer, 0, extendedString);
                }
                newSections.addAll(sections.subList(i, sections.size()));
                return sections;
            }
        }
        // If the first section is Exif, insert XMP data before the second section,
        // otherwise, make xmp data the first section.
        List<Section> newSections = new ArrayList<Section>();
        int position = (sections.get(0).marker == M_APP1) ? 1 : 0;
        newSections.addAll(sections.subList(0, position));
        newSections.add(xmpSection);
        if (audioMeta != null) {
            newSections = insertExtendedSection(newSections, extendedBuffer, 0, extendedString);
        }
        newSections.addAll(sections.subList(position, sections.size()));
        return newSections;
    }

    private static List<Section> insertExtendedSection(
            List<Section> sections, byte[] buffer, int offset, String extendedString) {
        int allLen = buffer.length;
        int len = allLen - offset;
        int sectionLen = len;
        if (len > MAX_XMP_EXTENDED_SIZE) {
            sectionLen = MAX_XMP_EXTENDED_SIZE;
        }

        String extendHeader = XMP_EXTENSION_HEADER + extendedString + "\0";

        byte[] xmpdata = new byte[
                sectionLen + XMP_EXTENSION_HEADER_GUID_SIZE + XMP_EXTENSION_HEADER_OFFSET];
        byte[] lenData = new byte[8];
        lenData[3] = (byte) (0xff & allLen);
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-18,BUG-2694254*/
        lenData[2] = (byte) ((0xff00 & allLen) >> 8);
        lenData[1] = (byte) ((0xff0000 & allLen) >> 16);
        lenData[0] = (byte) ((0xff000000 & allLen) >> 24);

        lenData[7] = (byte) (0xff & offset);
        lenData[6] = (byte) ((0xff00 & offset) >> 8);
        lenData[5] = (byte) ((0xff0000 & offset) >> 16);
        lenData[4] = (byte) ((0xff000000 & offset) >> 24);
        /* MODIFIED-END by jianying.zhang,BUG-2694254*/

        System.arraycopy(extendHeader.getBytes(), 0, xmpdata, 0, XMP_EXTENSION_HEADER_GUID_SIZE);
        System.arraycopy(lenData, 0, xmpdata, XMP_EXTENSION_HEADER_GUID_SIZE, 8);
        System.arraycopy(buffer, offset, xmpdata, XMP_EXTENSION_HEADER_GUID_SIZE + 8, sectionLen);
        Section xmpSection = new Section();
        xmpSection.marker = M_APP1;
        // Adds the length place (2 bytes) to the section length.
        xmpSection.length = xmpdata.length + 2;
        xmpSection.data = xmpdata;

        sections.add(xmpSection);

        if (len > MAX_XMP_BUFFER_SIZE) {
            sections = insertExtendedSection(sections, buffer, offset + sectionLen, extendedString);
        }
        return sections;
    }

    private static String calExtendedString(byte[] audioBuffer) {
        if (audioBuffer == null || audioBuffer.length == 0) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(audioBuffer);
            return convertToHexString(messageDigest.digest());
        } catch (Exception e) {
            Log.e(TAG, "calExtendedString Exception: " + e);
        }
        return null; //TODO this need chang to the md5 sum of the extend content.
    }

    private static String convertToHexString(byte data[]) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            strBuffer.append(Integer.toHexString(0xff & data[i]));
        }
        return strBuffer.toString();
    }

    private static void insertExtendedString(XMPMeta meta, String audioExtendedString) {
        try {
            meta.setProperty(GOOGLE_AUDIO_NAMESPACE, MIME, AUDIO_MINI);
            meta.setProperty(ADOBE_NOTE_NAMESPACE, XMP_HAS_EXTENSION, audioExtendedString);
        } catch (XMPException e) {
            Log.d(TAG, "insertExtendedString Exception: " + e);
        }
    }

    /**
     * Checks whether the byte array has XMP header. The XMP section contains
     * a fixed length header XMP_HEADER.
     *
     * @param data Xmp metadata.
     */
    private static boolean hasXMPHeader(byte[] data) {
        if (data.length < XMP_HEADER_SIZE) {
            return false;
        }
        try {
            byte[] header = new byte[XMP_HEADER_SIZE];
            System.arraycopy(data, 0, header, 0, XMP_HEADER_SIZE);
            if (new String(header, "UTF-8").equals(XMP_HEADER)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return false;
    }

    /**
     * Gets the end of the xmp meta content. If there is no packet wrapper,
     * return data.length, otherwise return 1 + the position of last '>'
     * without '?' before it.
     * Usually the packet wrapper end is "<?xpacket end="w"?> but
     * javax.xml.parsers.DocumentBuilder fails to parse it in android.
     *
     * @param data xmp metadata bytes.
     * @return The end of the xmp metadata content.
     */
    private static int getXMPContentEnd(byte[] data) {
        for (int i = data.length - 1; i >= 1; --i) {
            if (data[i] == '>') {
                if (data[i - 1] != '?') {
                    return i + 1;
                }
            }
        }
        // It should not reach here for a valid xmp meta.
        return data.length;
    }

    /**
     * Parses the jpeg image file. If readMetaOnly is true, only keeps the Exif
     * and XMP sections (with marker M_APP1) and ignore others; otherwise, keep
     * all sections. The last section with image data will have -1 length.
     *
     * @param is           Input image data stream.
     * @param readMetaOnly Whether only reads the metadata in jpg.
     * @return The parse result.
     */
    private static List<Section> parse(InputStream is, boolean readMetaOnly) {
        try {
            if (is.read() != 0xff || is.read() != M_SOI) {
                return null;
            }
            List<Section> sections = new ArrayList<Section>();
            int c;
            while ((c = is.read()) != -1) {
                if (c != 0xff) {
                    return null;
                }
                // Skip padding bytes.
                while ((c = is.read()) == 0xff) {
                }
                if (c == -1) {
                    return null;
                }
                int marker = c;
                if (marker == M_SOS) {
                    // M_SOS indicates the image data will follow and no metadata after
                    // that, so read all data at one time.
                    if (!readMetaOnly) {
                        Section section = new Section();
                        section.marker = marker;
                        section.length = -1;
                        section.data = new byte[is.available()];
                        is.read(section.data, 0, section.data.length);
                        sections.add(section);
                    }
                    return sections;
                }
                int lh = is.read();
                int ll = is.read();
                if (lh == -1 || ll == -1) {
                    return null;
                }
                int length = lh << 8 | ll;
                if (!readMetaOnly || c == M_APP1) {
                    Section section = new Section();
                    section.marker = marker;
                    section.length = length;
                    section.data = new byte[length - 2];
                    is.read(section.data, 0, length - 2);
                    sections.add(section);
                } else {
                    // Skip this section since all exif/xmp meta will be in M_APP1
                    // section.
                    is.skip(length - 2);
                }
            }
            return sections;
        } catch (IOException e) {
            Log.d(TAG, "Could not parse file.", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }

    private XmpUtil() {
    }
}
