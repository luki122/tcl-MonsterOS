package cn.download.mie.base.util;

import android.os.Environment;

import com.tcl.framework.network.HostNameResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/8/3 10:03
 * @copyright TCL-MIE
 */
public class UrlConfig {

    private static final String	PROTOCOL_HOST_D	= "http://172.26.50.37:8090";//开发环境
    private static final String	PROTOCOL_HOST_T	= "http://story-test.tclclouds.com";//测试环境
    private static final String PROTOCOL_HOST_O = "http://story.tclclouds.com";//正式环境
    private static String PROTOCOL_HOST = PROTOCOL_HOST_O;

    private static final String DOMAIN_DEV = "DEV";
    private static final String DOMAIN_TEST = "TEST";
    private static final String DOMAIN_OFFICIAL = "OFFICIAL";
    private static final String DOMAIN_DEFAULT = DOMAIN_OFFICIAL;

    public static final String GET_BIND_ACCOUNTS = "/api/tv/getbindaccounts";//获取绑定帐户列表
    public static final String GET_USER_RES = "/api/tv/getuserres";//获取用户所有资源
    public static final String GET_RES_BY_TYPE = "/api/tv/getresbytype";//获取某类型的资源
    public static final String RES_VIEWED = "/api/tv/resviewed";//上送资源查看标记
    public static final String HAS_NEW_BIND = "/api/tv/hasnewbind";//是否有新用户绑定
    public static final String HAS_NEW_RES = "/api/tv/hasnewres";//是否有新的资源
    public static final String HAS_NEW_RES_OF_USER = "/api/tv/hasnewresofuser";//用户是否有新的资源
    public static final String HAS_UNVIEWED_RES = "/api/tv/hasunviewedres";//用户是否有未(在TV上)浏览的资源
    public static final String DEL_BIND = "/api/tv/delbind";//解除绑定
    public static final String GET_QRCODES = "/api/tv/getqrcodes";//获取二维码
    public static final String HAS_BIND_CHANGE = "/api/tv/hasbindchange";//是否有用户绑定变化


    private static final String		VERSION_PROTOCOL_HOST_I			= "http://cmscn.tclclouds.com/";
    private static final String		VERSION_PROTOCOL_HOST_T			= "http://cmscntest.tclclouds.com/";
    private static final String		VERSION_PROTOCOL_HOST_O			= "http://globalcms.tclclouds.com/";
    public static String			VERSION_PROTOCOL_HOST			= VERSION_PROTOCOL_HOST_I;

    private static final String UPDATE_URL = "version/updateVersion";

    private UrlConfig(){

    }

    public static void init(){
        String domain = DOMAIN_DEFAULT;
        InputStream is = null;
        try {

            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += File.separator + "tcl_tv_leho_domain_version.properties";
            File file= new File(path);
            if (file.exists()) {
                is = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(is, "utf-8");
                Properties properties = new Properties();
                properties.load(reader);
                domain = properties.getProperty("domain_version", DOMAIN_DEFAULT);
            }

        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        if (DOMAIN_DEV.equalsIgnoreCase(domain)) {
            PROTOCOL_HOST = PROTOCOL_HOST_D;
        }
        else if (DOMAIN_TEST.equalsIgnoreCase(domain)) {
            PROTOCOL_HOST = PROTOCOL_HOST_T;
        }
        else if (DOMAIN_OFFICIAL.equalsIgnoreCase(domain)) {
            PROTOCOL_HOST = PROTOCOL_HOST_O;
        }

    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 获取绑定帐户列表
     * @return
     */
    public static String getBindAccounts() {
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(GET_BIND_ACCOUNTS);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 获取用户所有资源
     * @return
     */
    public static String getUserRes(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(GET_USER_RES);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 获取某类型的资源
     * @return
     */
    public static String getResByType(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(GET_RES_BY_TYPE);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 上送资源查看标记
     * @return
     */
    public static String resViewed(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(RES_VIEWED);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 是否有新用户绑定
     * @return
     */
    public static String hasNewBind(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(HAS_NEW_BIND);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 是否有新的资源
     * @return
     */
    public static String hasNewRes(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(HAS_NEW_RES);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 用户是否有新的资源
     * @return
     */
    public static String hasNewResOfUser(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(HAS_NEW_RES_OF_USER);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 用户是否有未(在TV上)浏览的资源
     * @return
     */
    public static String hasUnviewedRes(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(HAS_UNVIEWED_RES);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 解除绑定
     * @return
     */
    public static String delBind(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(DEL_BIND);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 获取二维码
     * @return
     */
    public static String getQRcodes(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(GET_QRCODES);
        return sb.toString();
    }

    /**
     * http://confluence.lab.tclclouds.com/pages/viewpage.action?pageId=6820639
     * 获取二维码
     * @return
     */
    public static String hasbindchange(){
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(HAS_BIND_CHANGE);
        return sb.toString();
    }

    public static String getUpdateVersionURL() {
        StringBuffer sb = new StringBuffer(VERSION_PROTOCOL_HOST);
        sb.append(UPDATE_URL);
        return HostNameResolver.resovleURL(sb.toString());
    }

}
