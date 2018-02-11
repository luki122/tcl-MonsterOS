package com.android.classloader;

import com.android.camera.debug.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import dalvik.system.DexClassLoader;

/**
 * Created by sichao.hu on 11/4/15.
 */
public class ExternalLoader {

    private static final Log.Tag TAG=new Log.Tag("ExternalLoader");
    private Map<String,TaggedDexLoader> mClassLoaderMap=new HashMap<>();
    private String mOptimizedPath;
    private ClassLoader mClassLoader;
    public ExternalLoader(String optimzedPath,ClassLoader classLoader){
        mOptimizedPath=optimzedPath;
        mClassLoader=classLoader;

    }

    private static class TaggedDexLoader extends DexClassLoader{
        Map<String,CommonInstruction> functionMap=new HashMap<>();
        public TaggedDexLoader(String dexPath,String optimizedPath,String libraryPath,ClassLoader classLoader){
            super(dexPath,optimizedPath,libraryPath,classLoader);
        }

        public void uploadClass(String classPath, CommonInstruction instruction){
            if(!functionMap.containsKey(classPath)) {
                functionMap.put(classPath, instruction);
            }
        }
    }

    /**
     * upload the expect DEX file into memory space
     * @param dexPath the ABSOLUTE PATH of expected dex file
     * @param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
     */
    public void uploadDexIntoMemory(String dexPath,String libraryPath){
        if(!mClassLoaderMap.containsKey(dexPath)) {
            TaggedDexLoader dexClassLoader=new TaggedDexLoader(dexPath,mOptimizedPath,libraryPath,mClassLoader);
            mClassLoaderMap.put(dexPath, dexClassLoader);
        }
    }

    /**
     * upload the exact class into memory space
     * @param dexPath the file path of DEX file which includes the expected class
     * @param classPath the ABSOLUTE class path of the expected class, the path MUST comprise it's package name
     * @return true for loading succeed while false for loading failed
     * @throws ClassNotFoundException the expected class not found
     */
    public boolean uploadExpectClass(String dexPath, String classPath) throws ClassNotFoundException{
        CommonInstruction dynamicLib=null;
        TaggedDexLoader dexClassLoader=mClassLoaderMap.get(dexPath);
        if(dexClassLoader==null){
            Log.e(TAG,"dex file "+dexPath+" not loaded yet");
            return false;
        }
        try {
            dynamicLib=(CommonInstruction)dexClassLoader.loadClass(classPath).newInstance();
            dexClassLoader.uploadClass(classPath, dynamicLib);
            Log.w(TAG,"class "+classPath+" uploaded");
            return true;
        } catch (InstantiationException|IllegalAccessException e) {
            Log.e(TAG,"load class failed for "+e.getMessage());
            return false;
        }
    }

    /**
     * As defined in {@link CommonInstruction#getFunctionPointer(String, Object...)}} , we can retrieve the expected callable by post specific message
     * @param dexPath the DEX file path related to the expected class
     * @param className the concrete class name of which includes the epected function;
     * @param functionName the name of the expected function
     * @param parameters parameters to post to the expected function
     * @return function pointer expected
     */
    public  Callable<Object> getFunctionPointer(String dexPath,String className,
                                                String functionName,final Object... parameters){
        TaggedDexLoader dexClassLoader=mClassLoaderMap.get(dexPath);
        if(dexClassLoader==null){
            Log.e(TAG,"dex file "+dexPath+" not loaded yet");
            return null;
        }
        CommonInstruction classInterface=dexClassLoader.functionMap.get(className);
        if(classInterface==null){
            Log.w(TAG,"class interface not found");
            return null;
        }else{
            Callable<Object> callable=classInterface.getFunctionPointer(functionName, parameters);
            if(callable==null) {
                Log.w(TAG, "invalid functionName "+functionName);
            }
            return callable;
        }
    }

}
