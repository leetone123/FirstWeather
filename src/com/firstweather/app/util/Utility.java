package com.firstweather.app.util;

import com.firstweather.app.db.FirstWeatherDB;
import com.firstweather.app.model.City;
import com.firstweather.app.model.Country;
import com.firstweather.app.model.Province;

import android.text.TextUtils;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(FirstWeatherDB firstWeatherDB, String response){
		if(!TextUtils.isEmpty(response)){
			//ͨ��split���ָ����ݲ�������������
			String[] allProvinces = response.split(",");
			if(allProvinces != null&& allProvinces.length>0){
				for(String p:allProvinces){
					//�ٴ�ͨ��split���ָ�ÿ����Ԫ
					String[] array = p.split(":");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[0]);
					//���������������ݴ洢�����ݿ��Ӧ�ı���
					firstWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * �����ʹ�����������ص��м�����
	 */
	public synchronized static boolean handleCitiesResponse(FirstWeatherDB firstWeatherDB, String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities = response.split(",");
			if(allCities!= null&&allCities.length>0){
				for(String c:allCities){
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					firstWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public synchronized static boolean handleCountriesResponse(FirstWeatherDB firstWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCountries = response.split(",");
			if(allCountries != null && allCountries.length>0){
				for(String c:allCountries){
					String[] array = c.split("\\|");
					Country country = new Country();
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					country.setCityId(cityId);
					firstWeatherDB.saveCountries(country);
				}
				return true;
			}
		}
		return false;
	}

}
