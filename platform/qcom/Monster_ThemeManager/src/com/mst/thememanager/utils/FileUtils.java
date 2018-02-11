package com.mst.thememanager.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class FileUtils extends android.os.FileUtils{
	private static final String TAG = "FileUtlis";
	

    private static final int FILE_BUFFER_SIZE = 51200;
	
    private static final boolean DEBUG = true;
	
	public static boolean fileExists(File file){
		return file.exists();
	}
	
	public static boolean fileExists(String path){
		return new File(path).exists();
	}
	
	public static InputStream getStreamFromFile(String filePath, boolean isLocal) {
		InputStream is = null;
		try {
			if (isLocal) {
				is = new FileInputStream(filePath);
			}else {
				//is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), data));
				is = new FileInputStream(new File(filePath));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}
	
	
	
	
	public static boolean storageMounted(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	private static  class log{
		public static void d(String msg){
			if(DEBUG)
			Log.d(TAG, msg);
		}
		public static void e(String msg){
			if(DEBUG)
			Log.e(TAG, msg);
		}
		
		public static void i(String msg){
			if(DEBUG)
			Log.i(TAG, msg);
		}
	}


    public static boolean fileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            log.e("param invalid, filePath: " + filePath);
            return false;
        }

        File f = new File(filePath);
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public static InputStream readFile(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return null;
        }

        InputStream is = null;

        try {
            if (fileIsExist(filePath)) {
                File f = new File(filePath);
                is = new FileInputStream(f);
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
            return null;
        }
        return is;
    }

    public static boolean createDirectory(String filePath) {
        if (null == filePath) {
            return false;
        }

        File file = new File(filePath);

        if (file.exists()) {
            return true;
        }

        return file.mkdirs();

    }

    public static boolean deleteDirectory(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }
        File oldFile = new File(filePath);
        File file = new File(oldFile.getAbsolutePath() + System.currentTimeMillis());
        oldFile.renameTo(file);
        if (file == null || !file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] list = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                log.d("delete filePath: " + list[i].getAbsolutePath());
                if (list[i].isDirectory()) {
                    deleteDirectory(list[i].getAbsolutePath());
                } else {
                    list[i].delete();
                }
            }
        }

        log.d("delete filePath: " + file.getAbsolutePath());
        file.delete();
        return true;
    }

    public static boolean deleteDirectoryChildren(String filePath){
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }
        File file = new File(filePath);
        Log.d("apply", "delete dir-->"+file.getAbsolutePath());
        if (file == null || !file.exists()) {
            return false;
        }
        if(file.isDirectory()){
        	File[] files = file.listFiles();
        	for(File f:files){
        		if(f.isDirectory()){
        			deleteDirectoryChildren(f.getAbsolutePath());
        		}else{
        			boolean success = f.delete();
        			 Log.d("apply", "delete file success--->"+success);
        		}
        	}
        }
        return true;
    }
    
    public static boolean writeFile(String filePath, InputStream inputStream) {

        if (null == filePath || filePath.length() < 1) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }

        try {
            File file = new File(filePath);
            if (file.exists()) {
                deleteDirectory(filePath);
            }

            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            boolean ret = createDirectory(pth);
            if (!ret) {
                log.e("createDirectory fail path = " + pth);
                return false;
            }

            boolean ret1 = file.createNewFile();
            if (!ret) {
                log.e("createNewFile fail filePath = " + filePath);
                return false;
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int c = inputStream.read(buf);
            while (-1 != c) {
                fileOutputStream.write(buf, 0, c);
                c = inputStream.read(buf);
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean writeFile(String filePath, String fileContent) {
        return writeFile(filePath, fileContent, false);
    }

    public static boolean writeFile(String filePath, String fileContent, boolean append) {
        if (null == filePath || fileContent == null || filePath.length() < 1 || fileContent.length() < 1) {
            log.e("Invalid param. filePath: " + filePath + ", fileContent: " + fileContent);
            return false;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            BufferedWriter output = new BufferedWriter(new FileWriter(file, append));
            output.write(fileContent);
            output.flush();
            output.close();
        } catch (IOException ioe) {
            log.e("writeFile ioe: " + ioe.toString());
            return false;
        }

        return true;
    }

    public static long getFileSize(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return 0;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }

        return file.length();
    }

    public static long getFileModifyTime(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return 0;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }

        return file.lastModified();
    }

    public static boolean setFileModifyTime(String filePath, long modifyTime) {
        if (null == filePath || modifyTime < 0) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }

        return file.setLastModified(modifyTime);
    }

    public static boolean copyFile(ContentResolver cr, String fromPath, String destUri) {
        if (null == cr || null == fromPath || fromPath.length() < 1 || null == destUri
                || destUri.length() < 1) {
            log.e("copyFile Invalid param. cr=" + cr + ", fromPath=" + fromPath + ", destUri=" + destUri);
            return false;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(fromPath);
            if (null == is) {
                log.e("Failed to open inputStream: " + fromPath + "->" + destUri);
                return false;
            }

            // check output uri
            String path = null;
            Uri uri = null;

            String lwUri = destUri.toLowerCase();
            if (lwUri.startsWith("content://")) {
                uri = Uri.parse(destUri);
            } else if (lwUri.startsWith("file://")) {
                uri = Uri.parse(destUri);
                path = uri.getPath();
            } else {
                path = destUri;
            }

            // open output
            if (null != path) {
                File fl = new File(path);
                String pth = path.substring(0, path.lastIndexOf("/"));
                File pf = new File(pth);

                if (pf.exists() && !pf.isDirectory()) {
                    pf.delete();
                }

                pf = new File(pth + File.separator);

                if (!pf.exists()) {
                    if (!pf.mkdirs()) {
                        log.e("Can't make dirs, path=" + pth);
                    }
                }

                pf = new File(path);
                if (pf.exists()) {
                    if (pf.isDirectory())
                        deleteDirectory(path);
                    else
                        pf.delete();
                }

                os = new FileOutputStream(path);
                try {
    				long time = System.currentTimeMillis();
    				if (time > 0) {
    					fl.setLastModified(time);
    				}
    			} catch (Exception e) {
    			}
            } else {
                os = new ParcelFileDescriptor.AutoCloseOutputStream(cr.openFileDescriptor(uri, "w"));
            }

            // copy file
            byte[] dat = new byte[1024];
            int i = is.read(dat);
            while (-1 != i) {
                os.write(dat, 0, i);
                i = is.read(dat);
            }

            is.close();
            is = null;

            os.flush();
            os.close();
            os = null;

            return true;

        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                };
            }
            if (null != os) {
                try {
                    os.close();
                } catch (Exception ex) {
                };
            }
        }
        return false;
    }
    
    
    public static boolean chmodThemeApplyDir(){
    	return false;
    }
    

	public static boolean copyFile(String fromPath, String toPath, Context mContext) {
		
		if (fromPath == null) {
			Log.d(TAG, "copyFile: fromPath = " + fromPath);
			return false;
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(fromPath);
		} catch (FileNotFoundException e) {
			Log.d(TAG, fromPath + "-->copyFile: FileNotFoundException = " + e.toString());
			try {
				is = new FileInputStream(fromPath);
			} catch (Exception e1) {
				Log.d(TAG, fromPath + "--1-->copyFile: Exception = " + e1.toString());
			}
		}
		if (null == is) {
			Log.d(TAG, "copyFile: is==null -> " + fromPath);
			//log.e("Failed to open inputStream: " + fromPath + "->" + fromPath);
			return false;
		}
		// open output
		if (null != toPath) {
			File fl = new File(toPath);
			String pth = toPath.substring(0, toPath.lastIndexOf("/"));
			File pf = new File(pth);
			if (pf.exists() && !pf.isDirectory()) {
				pf.delete();
			}

			pf = new File(pth + File.separator);

			if (!pf.exists()) {
				if (!pf.mkdirs()) {
					log.e("Can't make dirs, path=" + pth);
				}
				
			}

			pf = new File(toPath);
			if (pf.exists()) {
				if (pf.isDirectory())
					deleteDirectory(toPath);
				else
					pf.delete();
			}

			try {
				os = new FileOutputStream(toPath);
			} catch (FileNotFoundException e) {
				Log.d(TAG, toPath + "-->copyFile: FileNotFoundException = " + e.toString());
				try {
					os = new FileOutputStream(toPath);
				} catch (Exception e2) {
					Log.d(TAG, toPath + "--2-->copyFile: Exception = " + e2.toString());
				}
			} catch (Exception e) {
				Log.d(TAG, "--3-->copyFile: Exception = " + e.toString());
			} 
			if (null == os) {
				if (null != is) {
					try {
						is.close();
						is = null;
					} catch (Exception ex) {
					}
				}
				Log.d(TAG, "copyFile: failed ");
				return false;
			}
			try {
				long time = System.currentTimeMillis();
				if (time > 0) {
					Log.d(TAG, fromPath+"-->copyFile: setLastModified = " + time);
					fl.setLastModified(time);
				}
			} catch (Exception e) {
			}
		}
		try {
			// copy file
			byte[] dat = new byte[1024];
			int i = is.read(dat);
			while (-1 != i) {
				os.write(dat, 0, i);
				i = is.read(dat);
			}
			
			boolean res = CommonUtil.chmodFile(toPath);
			
			Log.d(TAG, "copyFile: success = "+res);
			return res;

		} catch (Exception ex) {
			log.e("Exception, ex: " + ex.toString());
			try {
				// copy file
				byte[] dat = new byte[1024];
				int i = is.read(dat);
				while (-1 != i) {
					os.write(dat, 0, i);
					i = is.read(dat);
				}

				CommonUtil.chmodFile(toPath);
				Log.d(TAG, "2-->copyFile: success ");
				
				
				return true;
			} catch (Exception e) {
				Log.d(TAG, "2-->e:" + e.toString());
			}finally {
				if (null != is) {
					try {
						is.close();
						is = null;
					} catch (Exception e1) {

					}
				}
				if (null != os) {
					try {
						os.flush();
						os.close();
						os = null;
					} catch (Exception e2) {
					}
				}
			}
		} finally {
			if (null != is) {
				try {
					is.close();
					is = null;
				} catch (Exception ex) {

				}
			}
			if (null != os) {
				try {
					os.flush();
					os.close();
					os = null;
				} catch (Exception ex) {
				}
			}
		}
		return false;
	}

    public static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int c = is.read(buf);
        while (-1 != c) {
            baos.write(buf, 0, c);
            c = is.read(buf);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public static byte[] readFile(Context ctx, Uri uri) {
        if (null == ctx || null == uri) {
            log.e("Invalid param. ctx: " + ctx + ", uri: " + uri);
            return null;
        }

        InputStream is = null;
        String scheme = uri.getScheme().toLowerCase();
        if (scheme.equals("file")) {
            is = readFile(uri.getPath());
        }

        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (null == is) {
                return null;
            }

            byte[] bret = readAll(is);
            is.close();
            is = null;

            return bret;
        } catch (FileNotFoundException fne) {
            log.e("FilNotFoundException, ex: " + fne.toString());
        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                };
            }
        }
        return null;
    }
    
    // 复制文件   
    public static void copyFileInner(File sourceFile,File targetFile)   
    throws IOException{  
            // 新建文件输入流并对它进行缓冲   
            FileInputStream input = new FileInputStream(sourceFile);  
            BufferedInputStream inBuff=new BufferedInputStream(input);  
            Log.d("apply", "copyFileInner-->"+sourceFile.getAbsolutePath()+" \n"+
            targetFile.getAbsolutePath());
            // 新建文件输出流并对它进行缓冲   
            FileOutputStream output = new FileOutputStream(targetFile);  
            BufferedOutputStream outBuff=new BufferedOutputStream(output);  
              
            // 缓冲数组   
            byte[] b = new byte[1024 * 5];  
            int len;  
            while ((len =inBuff.read(b)) != -1) {  
                outBuff.write(b, 0, len);  
            }  
            // 刷新此缓冲的输出流   
            outBuff.flush();  
              
            //关闭流   
            inBuff.close();  
            outBuff.close();  
            output.close();  
            input.close();  
        }  
        // 复制文件夹   
        public static void copyDirectiory(String sourceDir, String targetDir)  
			throws IOException {
		// 新建目标目录
		// 获取源文件夹当前下的文件或目录
        	
        File targetDirFile = new File(targetDir);
        if(!targetDirFile.exists()){
        	boolean success = targetDirFile.mkdir();
        	Log.d("apply", "mkdir-->"+success+" path-->"+targetDirFile.getAbsolutePath());
        }
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFileInner(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	} 

    public static boolean writeFile(String filePath, byte[] content) {
        if (null == filePath || null == content) {
            log.e("Invalid param. filePath: " + filePath + ", content: " + content);
            return false;
        }

        FileOutputStream fos = null;
        try {
            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            File pf = null;
            pf = new File(pth);
            if (pf.exists() && !pf.isDirectory()) {
                pf.delete();
            }
            pf = new File(filePath);
            if (pf.exists()) {
                if (pf.isDirectory())
                    FileUtils.deleteDirectory(filePath);
                else
                    pf.delete();
            }

            pf = new File(pth + File.separator);
            if (!pf.exists()) {
                if (!pf.mkdirs()) {
                    log.e("Can't make dirs, path=" + pth);
                }
            }

            fos = new FileOutputStream(filePath);
            fos.write(content);
            fos.flush();
            fos.close();
            fos = null;
            
            try {
				long time = System.currentTimeMillis();
				if (time > 0) {
					pf.setLastModified(time);
				}
			} catch (Exception e) {
			}
            return true;

        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (Exception ex) {
                };
            }
        }
        return false;
    }

    /************* ZIP file operation ***************/
    public static boolean readZipFile(String zipFileName, StringBuffer crc) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                long size = entry.getSize();
                crc.append(entry.getCrc() + ", size: " + size);
            }
            zis.close();
        } catch (Exception ex) {
            log.e("Exception: " + ex.toString());
            return false;
        }
        return true;
    }

    public static byte[] readGZipFile(String zipFileName) {
        if (fileIsExist(zipFileName)) {
            log.i("zipFileName: " + zipFileName);
            try {
                FileInputStream fin = new FileInputStream(zipFileName);
                int size;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((size = fin.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, size);
                }
                return baos.toByteArray();
            } catch (Exception ex) {
                log.i("read zipRecorder file error");
            }
        }
        return null;
    }

    public static boolean zipFile(String baseDirName, String fileName, String targerFileName)
            throws IOException {
        if (baseDirName == null || "".equals(baseDirName)) {
            return false;
        }
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return false;
        }

        String baseDirPath = baseDir.getAbsolutePath();
        File targerFile = new File(targerFileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targerFile));
        File file = new File(baseDir, fileName);

        boolean zipResult = false;
        if (file.isFile()) {
            zipResult = fileToZip(baseDirPath, file, out);
        } else {
            zipResult = dirToZip(baseDirPath, file, out);
        }
        out.close();
        return zipResult;
    }

    public static boolean unZipFile(String fileName, String unZipDir) throws Exception {
        File f = new File(unZipDir);

        if (!f.exists()) {
            f.mkdirs();
        }

        BufferedInputStream is = null;
        ZipEntry entry;
        ZipFile zipfile = new ZipFile(fileName);
        Enumeration<?> enumeration = zipfile.entries();
        byte data[] = new byte[FILE_BUFFER_SIZE];
        log.i("unZipDir: " + unZipDir);

        while (enumeration.hasMoreElements()) {
            entry = ( ZipEntry ) enumeration.nextElement();
            if (entry.isDirectory()) {
            	String dirName = entry.getName();
                File f1 = new File(unZipDir + "/" + dirName);
                log.i("entry.isDirectory XXX " + f1.getPath());
                if (!f1.exists()) {
                    f1.mkdirs();
                }
            } else {
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                String name = unZipDir  + entry.getName();
                Log.d("ap", "name-->"+name);
                RandomAccessFile m_randFile = null;
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                }

                file.createNewFile();
                m_randFile = new RandomAccessFile(file, "rw");
                int begin = 0;

                while ((count = is.read(data, 0, FILE_BUFFER_SIZE)) != -1) {
                    try {
                        m_randFile.seek(begin);
                    } catch (Exception ex) {
                        log.e("exception, ex: " + ex.toString());
                    }

                    m_randFile.write(data, 0, count);
                    begin = begin + count;
                }

                m_randFile.close();
                is.close();
            }
        }
        
        chmodThemeApplyDir();

        return true;
    }

    private static boolean fileToZip(String baseDirPath, File file, ZipOutputStream out) throws IOException {
        FileInputStream in = null;
        ZipEntry entry = null;

        byte[] buffer = new byte[FILE_BUFFER_SIZE];
        int bytes_read;
        try {
            in = new FileInputStream(file);
            entry = new ZipEntry(getEntryName(baseDirPath, file));
            out.putNextEntry(entry);

            while ((bytes_read = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            out.closeEntry();
            in.close();
        } catch (IOException e) {
            log.e("Exception, ex: " + e.toString());
            return false;
        } finally {
            if (out != null) {
                out.closeEntry();
            }

            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    private static boolean dirToZip(String baseDirPath, File dir, ZipOutputStream out) throws IOException {
        if (!dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files.length == 0) {
            ZipEntry entry = new ZipEntry(getEntryName(baseDirPath, dir));

            try {
                out.putNextEntry(entry);
                out.closeEntry();
            } catch (IOException e) {
                log.e("Exception, ex: " + e.toString());
            }
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileToZip(baseDirPath, files[i], out);
            } else {
                dirToZip(baseDirPath, files[i], out);
            }
        }
        return true;
    }

    private static String getEntryName(String baseDirPath, File file) {
        if (!baseDirPath.endsWith(File.separator)) {
            baseDirPath = baseDirPath + File.separator;
        }

        String filePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            filePath = filePath + "/";
        }

        int index = filePath.indexOf(baseDirPath);
        return filePath.substring(index + baseDirPath.length());
    }

    public static boolean writeImage(Bitmap bitmap, String destPath, int quality) {
        try {
            FileUtils.deleteDirectory(destPath);
            if (FileUtils.createFile(destPath)) {
                FileOutputStream out = new FileOutputStream(destPath);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    out.flush();
                    out.close();
                    out = null;
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
        	return false;
		}
        return false;
    }

    /**
     * 删除一个文件
     * 
     * @param filePath
     *            要删除的文件路径名
     * @return true if this file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建一个文件，创建成功返回true
     * 
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                return file.createNewFile();
            }
        } catch (IOException e) {
			  Log.d(TAG, "createFile-->IOException: "+e.toString());
            e.printStackTrace();
        }
        return false;
    }

    
    
}
