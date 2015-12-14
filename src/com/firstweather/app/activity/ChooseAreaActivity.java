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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ���б�
	 */
	private List<Country> countryList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�еļ���
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
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����ݿ�û����ӷ������ϲ�ѯ
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
			titleText.setText("�й�");
			currentLevel=LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null,"province");
		}
	}
	/**
	 * ��ѯѡ��ʡ�ݵ����еĳ��У����ȴ����ݿ��ѯ������ʹӷ������ϲ�ѯ
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
	 * ��ѯѡ�еĳ��������е��أ����ȴ����ݿ��ѯ������ӷ������ϲ�ѯ
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
	 * ���ݴ���Ĵ��ź����ͣ��ӷ������ϲ�ѯ����ʡ��������
	 */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+ code +".xml";
		}
		else{
			address="http://www.weather.com.cn/data/list3/city��xml";
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
					//ͨ��runOnUiThread()�����ص����̴߳�������߼�
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
				//ͨ�����߳�����ӡ�쳣
				runOnUiThread(new Runnable(){
					@Override 
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog.setMessage("���ڼ����У����Ժ󡣡���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	/**
	 * ����Back���������ݵ�ǰ�������ж�Ӧ�÷���Province�б� City�б� ����ֱ���˳���
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
