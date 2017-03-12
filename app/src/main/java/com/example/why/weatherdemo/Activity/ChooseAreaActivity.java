package com.example.why.weatherdemo.Activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.why.weatherdemo.Model.City;
import com.example.why.weatherdemo.Model.CoolWeatherDB;
import com.example.why.weatherdemo.Model.County;
import com.example.why.weatherdemo.Model.Province;
import com.example.why.weatherdemo.R;
import com.example.why.weatherdemo.Util.HttpCallbackListener;
import com.example.why.weatherdemo.Util.HttpUtil;
import com.example.why.weatherdemo.Util.Utility;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.country;

public class ChooseAreaActivity extends AppCompatActivity {

    private  static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;

    private List<String> dataList=new ArrayList<String>();

    private List<Province> provincesList;
    private List<City> cityList;
    private List<County>  countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_area);

        listView=(ListView)findViewById(R.id.list_view);
        textView=(TextView)findViewById(R.id.textView);

        adapter=new ArrayAdapter<String>(this,android.R.layout.activity_list_item,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getinstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
            {
                if(currentLevel==LEVEL_PROVINCE)
                {
                    selectedProvince = provincesList.get(position);
                    queryCities();
                }


                else if (currentLevel == LEVEL_CITY)
                {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        queryProvinces();
    }

   private void queryProvinces()
   {
       provincesList=coolWeatherDB.loadProvinces();
       if(provincesList.size()>0)
       {
           dataList.clear();
           for(Province province:provincesList)
           {
               dataList.add(province.getprovinceName());

           }
           adapter.notifyDataSetChanged();
           listView.setSelection(0);
           textView.setText("China");
           currentLevel=LEVEL_PROVINCE;
       }

       else
       {
           queryFromServer(null, "province");
       }
   }

    private void queryCities()
    {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0)
        {
            dataList.clear();
            for (City city : cityList)
            {
                dataList.add(city.getCityName());
            }    adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedProvince.getprovinceName());
        currentLevel = LEVEL_CITY;
        } else
        {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }

    }

    private void queryCounties()
    {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0)
        {
            dataList.clear();
            for (County county : countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else
        {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }



    private void  queryFromServer(final String code,final String type)
    {
        String address;
        if(!TextUtils.isEmpty(code))
        {
            address="http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }
        else
        {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
        {
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
                        public void onError(Exception e)
                        {
                            runOnUiThread(new Runnable() {      @Override
                            public void run() {
                                closeProgressDialog();
                            //  Toast.makeText(ChooseAreaActivity.this,           "加载失败", Toast.LENGTH_SHORT).show();
                                }     });    }

                    });
                }

    private void showProgressDialog()
    {
        if(progressDialog==null)
        {
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("loading......");
            progressDialog.setCanceledOnTouchOutside(false);
        }
       progressDialog.show();
    }

    private void closeProgressDialog()
    {


    }



    }
