package com.firstweather.app.db;
import java.util.List;
import java.util.ArrayList;

import com.firstweather.app.model.City;
import com.firstweather.app.model.Country;
import com.firstweather.app.model.Province;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;

public class FirstWeatherDB {
	/**
	 * ���ݿ���
	 */
	public static final String DB_NAME= "first_weather";
	/**
	 * ���ݿ�汾
	 */
	public static final int VERSION = 1;
	private static FirstWeatherDB firstWeatherDB;
	private SQLiteDatabase db;
	/**
	 * ���췽��˽�л�
	 */
	private FirstWeatherDB(Context context){
		FirstWeatherOpenHelper dbHelper = new FirstWeatherOpenHelper(context,DB_NAME,null,VERSION);
		db = dbHelper.getWritableDatabase();
	}
	/**
	 * ��ȡFirstWeatherDB��ʵ��
	 */
	public synchronized static FirstWeatherDB getInstance(Context context){
	    if(	firstWeatherDB == null){
	    	firstWeatherDB = new FirstWeatherDB(context);
	    }
	    return firstWeatherDB;
	}
	/**
	 * ��Provinceʵ���洢�����ݿ�
	 */
	public void saveProvince(Province province){
		if(province != null){
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province",null,values);
		}
	}
	/**
	 * �����ݿ��л�ȡȫ������ʡ�ݵ���Ϣ
	 */
	public List<Province> loadProvinces(){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			}
			while(cursor.moveToNext());
		}
		return list;
	}
	/**
	 * ��Cityʵ���洢�����ݿ���
	 */
	public void saveCity(City city){
		if(city != null){
			ContentValues values = new ContentValues();
			values.put("city_name",city.getCityName());
			values.put("city_code", city.getCityCode());
			db.insert("City",null,values);
			}
	}
	/**
	 * �����ݿ��ж�ȡĳʡ�����еĳ�����Ϣ
	 */
	public List<City> loadCities(int provinceId){
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City",null,"province_id=?",new String[]{String.valueOf(provinceId)},null,null,null);
		if(cursor.moveToFirst()){
			do{
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			}
			while(cursor.moveToNext());
		}
		return list;
	}
	/**
	 * ��Countryʵ���洢�����ݿ���
	 */
	public void saveCountries(Country country){
		if(country != null){
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code",country.getCountryCode());
			values.put("city_id",country.getCityId());
			db.insert("Country",null,values);
		}
		
	}
	/**
	 * �����ݿ��ж�ȡĳ�����µ���������Ϣ
	 */
	public List<Country> loadCountry(int cityId){
		List<Country> list = new ArrayList<Country>();
		Cursor cursor = db.query("Country", null, "city_id=?", new String[]{
				String.valueOf(cityId)},null,null,null);
		if(cursor.moveToFirst()){
			do{
				Country country = new Country();
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
				country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
				country.setCityId(cityId);
				list.add(country);
			}
			while(cursor.moveToNext());
		}
		return list;
	}

}
