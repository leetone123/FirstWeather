package com.firstweather.app.activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import java.util.List;
import java.util.ArrayList;

import com.firstweather.app.R;
import com.firstweather.app.model.Province;
import com.firstweather.app.model.City;
import com.firstweather.app.model.Country;
import com.firstweather.app.util.HttpCallbackListener;
import com.firstweather.app.util.HttpUtil;
import com.firstweather.app.util.Utility;
import com.firstweather.app.db.FirstWeatherDB;
public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private FirstWeatherDB firstWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<Country> countryList;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		firstWeatherDB = FirstWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?>arg0,View view,int index,long arg3){
				if(currentLevel== LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}
				else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCountries();
				}
			}
		});
		queryProvinces();
	}
	/**
	 * 查询全国所有的省，优先从数据库查询，数据库没有则从服务器上查询
	 */
	private void queryProvinces(){
		provinceList = firstWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null,"province");
		}
	}
	/**
	 * 查询选中省份的所有的城市，优先从数据库查询，否则就从服务器上查询
	 */
	private void queryCities(){
		cityList = firstWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}
		else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/**
	 * 查询选中的城市下所有的县，优先从数据库查询，否则从服务器上查询
	 */
	private void queryCountries(){
		countryList = firstWeatherDB.loadCountries(selectedCity.getId());
		if(countryList.size()>0){
			for(Country country:countryList){
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		}
		else{
			queryFromServer(selectedCity.getCityCode(),"country");
		}
	}
	/**
	 * 根据传入的代号和类型，从服务器上查询各个省市县数据
	 */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+ code +".xml";
		}
		else{
			address="http://www.weather.com.cn/data/list3/city。xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
			@Override
			public void onFinish(String response){
				boolean result =false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(firstWeatherDB,response);
				}
				else if("city".equals(type)){
					result=Utility.handleCitiesResponse(firstWeatherDB,response,selectedProvince.getId());
				}
				else if("country".equals(type)){
					result = Utility.handleCountriesResponse(firstWeatherDB, response, selectedCity.getId());
				}
				if(result){
					//通过runOnUiThread()方法回到主线程处理相关逻辑
					runOnUiThread(new Runnable(){
						@Override
						public void run(){
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}
							else if("city".equals(type)){
								queryCities();
							}
							else if("country".equals(type)){
								queryCountries();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e){
				//通过主线程来打印异常
				runOnUiThread(new Runnable(){
					@Override 
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败，请重试", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog.setMessage("正在加载中，请稍后。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 捕获Back按键，根据当前级别来判断应该返回Province列表 City列表 还是直接退出！
	 */
	@Override
	public void onBackPressed(){
		if(currentLevel==LEVEL_COUNTRY){
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY){
			queryProvinces();
		}
		else{
			finish();
		}
	}

}
