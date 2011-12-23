package com.sapienssoftware.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import com.sapienssoftware.evemonitor.ProviderMetaData;

public class Common {
	private Common() {}
	private static String TAG = "EVE Monitor";
	public static class AccAccess{
    	public static String UserID;
    	public static String ApiKey;
    	public static String _id;
    	
    	public AccAccess () {
    		UserID = "";
    		ApiKey = "";
    		_id = "";
    	}
	}
	
    public static class ParseDate{
    	public static int Mount;
    	public static int Day;
    	public static int Minutes;
    	public static int Second;
    	
    	public ParseDate () {
    		Mount = 0;
    		Day = 0;
        	Minutes = 0;
        	Second = 0;
    	}
    }
    
    
    public static ParseDate getDiffDate(Date start, Date end){
		
    	ParseDate Result = new ParseDate();
    	
    	long delta = end.getTime() - start.getTime();
    	
    	long MILLISEC_IN_MOUNT = (long) (30*24*60*60*1000);
        long MILLISEC_IN_DAY = (long) (24*60*60*1000);
        long MILLISEC_IN_HOUR = (long) (60*60*1000);
        long MILLISEC_IN_MINUTE = (long) (60*1000);
        
        Result.Mount = (int) Math.abs(delta/1000/60/60/24/30);
        Result.Day = (int) Math.abs((delta - Result.Mount * MILLISEC_IN_MOUNT)/1000/60/60/24);
        Result.Minutes = (int) Math.abs((delta - Result.Mount * MILLISEC_IN_MOUNT - Result.Day * MILLISEC_IN_DAY)/1000/60/60);

        return Result;
    }
    
    
/*	
	// handler for the background updating
	public static Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
            
        	//dialog.incrementProgressBy(increment);
        }
    };
	
	public static void CallUpdateAcc(long id, Cursor mCursor) {
		// TODO Auto-generated method stub
		
		
		Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
		Log.d(TAG,uri.toString());
		String temp = "" + id;
		
		Uri uri_acc = uri.withAppendedPath(ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, temp);
		
		uri.parse(temp);
		Log.d(TAG,"Добавили к uri " + uri_acc.toString());
        //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
		Log.d(TAG,uri.toString());
		mCursor = managedQuery(uri_acc, 
				null, //projection
				null, //selection string
				null, //selection args array of strings
				null); //sort order
		mCursor.moveToFirst();
		Log.d(TAG,"Выбираем данные");
		String userId = mCursor.getString(2);
		String APIKey = mCursor.getString(3);
		
		Log.d(TAG,userId + "/"+ APIKey);

		/*
		String str_mlsDate = mCursor.getString(4);
		Log.d(TAG,str_mlsDate);
		//int tr = 
		long tr = Long.parseLong(str_mlsDate.toString());
		Date fsd = new Date();
		fsd.setTime(tr);
		
		Log.d(TAG,"" + fsd.getYear() + "/" + fsd.getMonth() + "/"+ fsd.getDay());
		
		*/
/*		
		AccAccess info = new AccAccess();
		info.ApiKey = mCursor.getString(3);
		info.UserID = mCursor.getString(2);
		info._id = "" + id;
		
		ArrayList<AccAccess> list = new ArrayList<AccAccess>();
		
		list.add(info);
		
		getAccInfo(list, mCursor);

	}
	
	
	
	private static void getAccInfo(ArrayList<AccAccess> ListOfAccess, final Cursor mCursor) {
		
		final String MyText;
		
		for (final AccAccess item : ListOfAccess) {
			
			Thread downloadInfo = new Thread(new Runnable(){

			@Override
			public void run() {
				ArrayList<String> list = new ArrayList<String>();
				Message msg = new Message();
				msg.arg1 = 1;
			    progressHandler.sendMessage(msg);
			        
			    try {

			    	Log.d(TAG, "Создаем Client");
			        //list.add("Создаем Client");
			        HttpClient client = new DefaultHttpClient();
			        //HttpHost host = new HttpHost("server.com", 443, "https");
			        //list.add("Создаем HttpPost");
			        Log.d(TAG, "Создаем HttpPost");
			        Log.d(TAG, "https://api.eveonline.com/account/AccountStatus.xml.aspx");
			        HttpPost request = new HttpPost("https://api.eveonline.com/account/AccountStatus.xml.aspx");
			        Log.d(TAG, "Вносим параметры");
			        //list.add("Вносим параметры");
			        List<NameValuePair> postParametrs = new ArrayList<NameValuePair>();
			        postParametrs.add(new BasicNameValuePair("userID", item.UserID));
			        postParametrs.add(new BasicNameValuePair("apikey", item.ApiKey));
			        //list.add("Устанавливаем параметры");
			        Log.d(TAG, "Устанавливаем параметры");
			        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParametrs);
			        	
			        request.setEntity(formEntity);
			        //list.add("Выполняем POST запрос");
			        Log.d(TAG, "Выполняем POST запрос");
			        HttpResponse response = client.execute(request);
			        //list.add("Получаем InputStream");
			        Log.d(TAG, "Получаем InputStream");
			        HttpEntity responseEntity = response.getEntity();
			        InputStream InStream = responseEntity.getContent();

					Log.d(TAG, "Создаем XmlPullParser");
						
			        XmlPullParser parser = Xml.newPullParser();
			        parser.setInput(InStream, null);
			        //parser.getEventType()
			        int i = 0;
			        while (parser.getEventType()!= XmlPullParser.END_DOCUMENT) {
			        	i++;
			        	//list.add("Вошли в цикл " + i);
			        	Log.d(TAG, "Вошли в цикл " + i);
			        	if (parser.getEventType() == XmlPullParser.START_TAG
			                    && parser.getName().equals("paidUntil")) {
			        			parser.next();
			        	MyText = parser.getText();
			        	Log.d(TAG, MyText);
			        	break;
			        	//list.add(parser.getText());
			                          //  + parser.getAttributeValue(1) + "\n"
			                          //  + parser.getAttributeValue(2));
			             }
			             parser.next();
			                
					}

			    } catch (MalformedURLException e) {
			    	//list.add("Ошибка MalformedURLException -- " + e);
					Log.d(TAG, "Ошибка MalformedURLException -- " + e);
					throw new RuntimeException(e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//list.add("Ошибка IOException -- " + e);
					Log.d(TAG, "Ошибка IOException -- " + e);
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					//list.add("Ошибка XmlPullParserException -- " + e);
					Log.d(TAG, "Ошибка XmlPullParserException -- " + e);
					e.printStackTrace();
				}
			        
			      
			    int year =   Integer.parseInt(MyText.substring(0, 4).toString());     
			    Log.d(TAG, "year -- " + year);
			        
			    int month =   Integer.parseInt(MyText.substring(5, 7).toString());     
			    Log.d(TAG, "month -- " + month);
			        
			    int day =   Integer.parseInt(MyText.substring(8, 10).toString());     
			    Log.d(TAG, "day -- " + day);
			        
			    int hour =   Integer.parseInt(MyText.substring(11, 13).toString());     
			    Log.d(TAG, "hour -- " + hour);
			        
			    int minute =   Integer.parseInt(MyText.substring(14, 16).toString());     
			    Log.d(TAG, "minute -- " + minute); 
			        
			    ContentValues values = new ContentValues(2);
			        
			    Date tempDate = new Date();
			    values.put(ProviderMetaData.ApiKeysTableMetaData.UPD_DATE, "" + tempDate.getTime() );
			        
			    tempDate.setYear(year - 1900); // 1900 - это наверное ноль у этого класса
			    tempDate.setMonth(month - 1); // первый месяц это ноль
			    tempDate.setDate(day);
			    tempDate.setHours(hour);
			    tempDate.setMinutes(minute);
			        
			    Log.d(TAG, "tempDate -- " + tempDate);
			       
					
			    //Message msg = new Message();
					//msg
					
			    progressHandler.sendMessage(progressHandler.obtainMessage());
		              
		        values.put(ProviderMetaData.ApiKeysTableMetaData.EXP_DATE, "" + tempDate.getTime() );
		            
		              
		        getContentResolver().update(
		            		ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + item._id, null);
		        mCursor.requery();
					
			} // eof run()
				
		}); // eof create Thread
			
			
		downloadInfo.start();
			
		} // eof for
		
	}
*/

}
