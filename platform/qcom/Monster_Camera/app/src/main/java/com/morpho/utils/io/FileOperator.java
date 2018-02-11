package com.morpho.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class FileOperator {

    /**
     * ファイルの有無をチェック
     * @return  true: ファイル有, false: ファイル無
     */
    public static boolean isFileExists(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        return new File(filePath).exists();
    }
    
    /**
     * ファイルコピー
     * @return  true: コピー成功, false: コピー失敗
     */
    public static boolean copyFile(String srcPath, String dstPath) {
        boolean result = false;
        
        if ((srcPath == null) || (dstPath == null)) {
            return result;
        }
        
        File src = new File(srcPath);
        File dst = new File(dstPath);
        
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            
            // コピー実行
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            
            result = true;
        
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * ファイル名変更
     * @return  true: リネーム成功, false: リネーム失敗
     */
    public static boolean renameFile(String srcPath, String dstPath) {
        boolean result = false;
        
        if ((srcPath == null) || (dstPath == null)) {
            return result;
        }
        
        File src = new File(srcPath);
        File dst = new File(dstPath);
        
        if (src.exists()) {
            result = src.renameTo(dst);
        }
        
        return result;
    }
    
    /**
     * ファイル削除
     * @return  true: 削除成功, false: 削除失敗
     */
    public static boolean deleteFile(String filePath) {
        boolean result = false;
        
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                result = file.delete();
            }
        }
        
        return result;
    }

    /**
     * ディレクトリ内のファイル削除
     * @return  true: 削除成功, false: 削除失敗
     */
    static public void cleanDir(File dir) {
        String[] children = dir.list();
        for (int i=0; i<children.length; i++) {  
        File file = new File(dir, children[i]);
            if (file.isFile()) {
                file.delete();
            }
        }
    }
    
    public static void outputData(byte[] data, String path) {
        File file = new File(path);
        try {
            FileOutputStream o_stream = new FileOutputStream(file);
            o_stream.write(data);
            o_stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputData(ByteBuffer data, String path) {
        File file = new File(path);
        try {
            FileOutputStream o_stream = new FileOutputStream(file);
            FileChannel outChannel = o_stream.getChannel();
            outChannel.write(data);
            outChannel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] read(String path) {
        File file = new File(path);
        int size = (int)file.length();
        byte[] buf = new byte[size];
        try {
            FileInputStream fis = new FileInputStream(path);
            fis.read(buf);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
    }
}
