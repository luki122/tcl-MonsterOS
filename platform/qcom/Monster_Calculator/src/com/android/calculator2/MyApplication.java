package com.android.calculator2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import com.android.calculator2.exchange.bean.MainExchangeBean;
import com.android.calculator2.utils.AppConst;
import com.android.calculator2.utils.ContryFlag;
import com.android.calculator2.utils.Utils;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initDatabase();
        if (TextUtils.isEmpty(Utils.getRateJson(getApplicationContext()))) {
            initRateFile();
        }
        if (Utils.isNotSaveExchangeData(getApplicationContext())) {
            initThreeExchange();
        }
        
//        setDefaultFont(this, "DEFAULT", "HelveticaNeue.ttf");
//        setDefaultFont(this, "MONOSPACE", "HelveticaNeue.ttf");
//        setDefaultFont(this, "SERIF", "HelveticaNeue.ttf");
//        setDefaultFont(this, "SANS_SERIF", "HelveticaNeue.ttf");
    }

    private void initDatabase() {
        String filePath = AppConst.filePath;
        String pathStr = AppConst.pathStr;
        File jhPath = new File(filePath);
        // 查看数据库文件是否存在
        if (jhPath.exists()) {
            // 存在则直接返回打开的数据库
            return;
        } else {
            // 不存在先创建文件夹
            File path = new File(pathStr);
            if (path.mkdir()) {
                System.out.println("创建成功");
                Log.i("zouxu", "创建成功");
            } else {
                System.out.println("创建失败");
                Log.i("zouxu", "创建失败");
            }
            try {
                // 得到资源
                AssetManager am = getApplicationContext().getAssets();
                // 得到数据库的输入流
                InputStream is = am.open("currency.db");
                // 用输出流写到SDcard上面
                FileOutputStream fos = new FileOutputStream(jhPath);
                // 创建byte数组 用于1KB写一次
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                // 最后关闭就可以了
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("zouxu", "数据库操作异常");
                return;
            }
            return;
        }
    }

    private void initRateFile() {
        String content;
        Resources resources = this.getResources();
        InputStream is = null;
        try {
            is = resources.openRawResource(R.raw.init_rate);
            byte buffer[] = new byte[is.available()];
            is.read(buffer);
            content = new String(buffer);
            Utils.saveRateJson(getApplicationContext(), content);
        } catch (IOException e) {
            Log.e("zouxu", "write file", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("zouxu", "close file", e);
                }
            }
        }
    }

    private void initThreeExchange() {
        MainExchangeBean m_bean1 = new MainExchangeBean();
        MainExchangeBean m_bean2 = new MainExchangeBean();
        MainExchangeBean m_bean3 = new MainExchangeBean();
        ContryFlag m_flag = new ContryFlag();

        m_bean1.currency_code = "CNY";
        m_bean1.currency_ch = "人民币";
        m_bean1.currency_en = "Chinese Yuan";//currency_name_en
        m_bean1.flag_id = m_flag.getFlagIdByCurrencyCode(m_bean1.currency_code);

        m_bean2.currency_code = "USD";
        m_bean2.currency_ch = "美元";
        m_bean2.currency_en = "United States Dollar";
        m_bean2.flag_id = m_flag.getFlagIdByCurrencyCode(m_bean2.currency_code);

        m_bean3.currency_code = "EUR";
        m_bean3.currency_ch = "欧元";
        m_bean3.currency_en = "Euro";
        m_bean3.flag_id = m_flag.getFlagIdByCurrencyCode(m_bean3.currency_code);

        Utils.saveExchangeBean1(getApplicationContext(), m_bean1);
        Utils.saveExchangeBean2(getApplicationContext(), m_bean2);
        Utils.saveExchangeBean3(getApplicationContext(), m_bean3);
    }

    public void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    public void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
