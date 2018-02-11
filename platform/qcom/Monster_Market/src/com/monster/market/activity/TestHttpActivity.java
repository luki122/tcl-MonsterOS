package com.monster.market.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.bean.AppDetailInfo;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AdListResultData;
import com.monster.market.http.data.AppDetailResultData;
import com.monster.market.http.data.AppListResultData;
import com.monster.market.http.data.AppTypeListResultData;
import com.monster.market.http.data.AppUpgradeInfoRequestData;
import com.monster.market.http.data.AppUpgradeListResultData;
import com.monster.market.http.data.BannerListResultData;
import com.monster.market.http.data.ReportAdClickRequestData;
import com.monster.market.http.data.SearchAppListResultData;
import com.monster.market.http.data.SearchKeyListResultData;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaobin on 16-8-8.
 */
public class TestHttpActivity extends BaseActivity {

    private TextView text;
    private LinearLayout btn_container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_http);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        text = (TextView) findViewById(R.id.text);
        btn_container = (LinearLayout) findViewById(R.id.btn_container);
    }

    @Override
    public void initData() {
        Button btn1 = new Button(this);
        btn1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn1.setText("获取首页列表");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestHelper.getIndexInfo(TestHttpActivity.this, 0, 2, new DataResponse<AppListResultData>() {

                    @Override
                    public void onResponse(AppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });
            }
        });
        btn_container.addView(btn1);

        Button btn2 = new Button(this);
        btn2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn2.setText("获取BANNER");
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取banner
                RequestHelper.getBanner(TestHttpActivity.this, new DataResponse<BannerListResultData>() {
                    @Override
                    public void onResponse(BannerListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });
            }
        });
        btn_container.addView(btn2);

        Button btn3 = new Button(this);
        btn3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn3.setText("主页重点推广位");
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getAdList(TestHttpActivity.this, 0, 2, new DataResponse<AdListResultData>() {
                    @Override
                    public void onResponse(AdListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn3);

        Button btn4 = new Button(this);
        btn4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn4.setText("搜索提示接口");
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getSearchKey(TestHttpActivity.this, "天", new DataResponse<SearchKeyListResultData>() {
                    @Override
                    public void onResponse(SearchKeyListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn4);

        Button btn5 = new Button(this);
        btn5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn5.setText("热门搜索接口");
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               RequestHelper.getSearchPopKey(TestHttpActivity.this, new DataResponse<SearchKeyListResultData>() {
                   @Override
                   public void onResponse(SearchKeyListResultData value) {
                       text.setText(value.toString());
                   }

                   @Override
                   public void onErrorResponse(RequestError error) {
                       text.setText(error.toString());
                   }
               });

            }
        });
        btn_container.addView(btn5);

        Button btn6 = new Button(this);
        btn6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn6.setText("应用搜索接口");
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.searchAppList(TestHttpActivity.this, 0, 10, "天气", new DataResponse<SearchAppListResultData>() {
                    @Override
                    public void onResponse(SearchAppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn6);

        Button btn7 = new Button(this);
        btn7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn7.setText("应用详情接口");
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getAppDetail(TestHttpActivity.this, "com.moji.mjweather", new DataResponse<AppDetailInfo>() {
                    @Override
                    public void onResponse(AppDetailInfo value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn7);

        Button btn8 = new Button(this);
        btn8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn8.setText("排行接口(游戏)");
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getRankingAppList(TestHttpActivity.this, 0, 10, 1, new DataResponse<AppListResultData>() {
                    @Override
                    public void onResponse(AppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn8);

        Button btn9 = new Button(this);
        btn9.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn9.setText("排行接口(应用)");
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getRankingAppList(TestHttpActivity.this, 0, 10, 2, new DataResponse<AppListResultData>() {
                    @Override
                    public void onResponse(AppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn9);

        Button btn10 = new Button(this);
        btn10.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn10.setText("分类接口(游戏)");
        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getAppTypeList(TestHttpActivity.this, 0, 20, 1, new DataResponse<AppTypeListResultData>() {
                    @Override
                    public void onResponse(AppTypeListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn10);

        Button btn11 = new Button(this);
        btn11.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn11.setText("分类接口(应用)");
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getAppTypeList(TestHttpActivity.this, 0, 20, 1, new DataResponse<AppTypeListResultData>() {
                    @Override
                    public void onResponse(AppTypeListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn11);

        Button btn12 = new Button(this);
        btn12.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn12.setText("分类详情");
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getAppTypeInfoList(TestHttpActivity.this, 0, 10, 5, new DataResponse<AppListResultData>() {
                    @Override
                    public void onResponse(AppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn12);

        Button btn13 = new Button(this);
        btn13.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn13.setText("新品接口");
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestHelper.getNewAppList(TestHttpActivity.this, 0, 10, "0", new DataResponse<AppListResultData>() {
                    @Override
                    public void onResponse(AppListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn13);

        Button btn14 = new Button(this);
        btn14.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn14.setText("更新接口");
        btn14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<AppUpgradeInfoRequestData> list = new ArrayList<AppUpgradeInfoRequestData>();
                AppUpgradeInfoRequestData info = new AppUpgradeInfoRequestData();
                info.setVersionCode(5);
                info.setPackageName("com.moji.mjweather");
                info.setVersionName("xxxxxx");
                list.add(info);

                RequestHelper.getAppUpdateList(TestHttpActivity.this, list, new DataResponse<AppUpgradeListResultData>() {
                    @Override
                    public void onResponse(AppUpgradeListResultData value) {
                        text.setText(value.toString());
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        text.setText(error.toString());
                    }
                });

            }
        });
        btn_container.addView(btn14);

        Button btn15 = new Button(this);
        btn15.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn15.setText("上报广告点击量");
        btn15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ReportAdClickRequestData requestData = new ReportAdClickRequestData();
                requestData.setAdId(7);
                requestData.setAdName("美好宛如初现的广告");
                requestData.setClickNum(1);
                RequestHelper.reportAdClick(TestHttpActivity.this, requestData);

            }
        });
        btn_container.addView(btn15);

        Button btn16 = new Button(this);
        btn16.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        btn16.setText("上报广告点击量");
        btn16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ReportAdClickRequestData requestData = new ReportAdClickRequestData();
                requestData.setAdId(7);
                requestData.setAdName("美好宛如初现的广告");
                requestData.setClickNum(1);
                RequestHelper.reportAdClick(TestHttpActivity.this, requestData);

            }
        });
        btn_container.addView(btn16);

    }

}
