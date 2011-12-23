package com.sapienssoftware.evemonitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
import com.sapienssoftware.core.Common;
import com.sapienssoftware.core.Common.*;
import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class EVEMonitorActivity extends ListActivity {
    /** Called when the activity is first created. */
    private static String TAG = "EVE Monitor";
    private static String MyText;
    private boolean eve_error;
    
    private static Cursor mCursor;
    private static ListAdapter mAdapter;
    private Timer globalTimer;
    private static final int REQUEST_CODE = 10;
    
    // Коды сообщений выполнения процесса
    private static final int MSG_LOAD_DATA = 1;
    private static final int MSG_PROCESS_DATA = 2;
    private static final int MSG_UPDATE_CURSOR = 3;
    private static final int MSG_ERROR_PROCESS_UPDATE = 4;
    
    
    
    
	/**===================================================
	 *           КОНСТРУКТОР КЛАССА
	 *===================================================*/
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //ListView list = (ListView)findViewById(android.R.id.list);
        registerForContextMenu(this.getListView());
       
        Log.d(TAG,"Обращаемся к базе данных" );
        Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
        Log.d(TAG,"CONTENT_URI = " +  uri);
        
        String[] projection = new String[] {ProviderMetaData.ApiKeysTableMetaData._ID, 
        									ProviderMetaData.ApiKeysTableMetaData.NAME, 
        									ProviderMetaData.ApiKeysTableMetaData.EXP_DATE};

		//Activity a = (Activity)this.mContext;
		mCursor = this.managedQuery(uri, 
				projection,
				null, //selection string
				null, //selection args array of strings
				null); //sort order
		Log.d(TAG,"Запрос выполнен есть данные");
       
        startManagingCursor(mCursor);

        ListAdapter adapter = new SimpleCursorAdapter(
                this, // Context.
                R.layout.acc_list_item,  // Specify the row template to use (here, two columns bound to the two retrieved cursorrows).
                mCursor,                                              // Pass in the cursor to bind to.
                new String[] {ProviderMetaData.ApiKeysTableMetaData.NAME, 
                				ProviderMetaData.ApiKeysTableMetaData.EXP_DATE, 
                				"rem_day", 	
                				ProviderMetaData.ApiKeysTableMetaData.UPD_DATE},           // Array of cursor columns to bind to.
                new int[] {R.id.name_acc, R.id.exp_date, R.id.remainsDay, R.id.status}); // Parallel array of which template objects to bind to those columns.

        setListAdapter(adapter);
/*
        if (mCursor.getCount() > 0) {
        	Log.e(TAG,"Запуск GlobalTaimer в конструкторе");
        	StartGlobalTime();
        }
        */
    }
	
	public void StartGlobalTime(){
        
        globalTimer = new Timer();
        TimerTask task = new UpdateListTask();

        // Ждать пять секунд перед выполнением task, 
        // а затем выполнять каждые 2 мин
        globalTimer.schedule( task, 5000, 300000 );
        
	}
	
	public class UpdateListTask extends TimerTask {
	    public void run() {
	        Log.e(TAG,"Запуск задачи");
	        Message msg = new Message();
	        msg.arg1 = MSG_UPDATE_CURSOR;
		    //progressHandler.sendMessage(progressHandler.obtainMessage());
		    progressHandler.sendMessage(msg);
	    }
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if (mCursor.getCount() > 0) {
			Log.e(TAG,"Остановка GlobalTaimer по onPause");
			globalTimer.cancel();
        }

	}	
     
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mCursor.getCount() > 0) {
			Log.e(TAG,"Запуск GlobalTaimer по onResume");
			StartGlobalTime();
        }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		//super.onCreateContextMenu(menu, v, menuInfo);
		//menu.setHeaderTitle("Мое конт меню");
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_context_menu, menu);
		
		
		
		//String menuTitle = this.getString(R.string.ctx_menu_update);
		//menu.add(1,1,1, menuTitle);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		//Log.d(TAG,"" + item.getItemId() + "/" + R.id.item_ctx_menu_update);
		String temp;
		switch (item.getItemId()) {
		case R.id.item_ctx_menu_update:
			Log.d(TAG,"Info.id = " + info.id);	
			View currView = info.targetView;
			
			if (currView.isEnabled()){
				//currView.setEnabled(false);
				CallUpdateAcc(info.id, this, this, currView);
				//currView.setEnabled(true);
			}
			//currView.setEnabled(true);
			//Common.CallUpdateAcc(info.id, mCursor);
			break;

		case R.id.item_ctx_menu_updateall:
			

			break;
		
		case R.id.item_ctx_menu_edit:
			
			Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
			Log.d(TAG,uri.toString());
			temp = "" + info.id;
			
			Uri uri_acc = uri.withAppendedPath(ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, temp);
			Log.d(TAG,uri.toString());
			
			mCursor = managedQuery(uri_acc, 
					null, //projection
					null, //selection string
					null, //selection args array of strings
					null); //sort order
			mCursor.moveToFirst();
			Log.d(TAG,"Выбираем данные");
			String _id = mCursor.getString(0);
			String name = mCursor.getString(1);
			String userId = mCursor.getString(2);
			String APIKey = mCursor.getString(3);
			
			
			Log.d(TAG,"Вызывается активность редактирования записи");
			String actionName = "com.sapienssoftware.intent.action.GET_URI";
			Intent call = new Intent(actionName);
			call.putExtra("name", name);
			call.putExtra("userId", userId);
			call.putExtra("APIKey", APIKey);
			call.putExtra("_id", _id);
			//call.putExtra("isEdit", "yes");
			startActivityForResult(call, REQUEST_CODE);
			break;
			
		case R.id.item_ctx_menu_delete:
			temp = "" + info.id;
			getContentResolver().delete(
					ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, "_ID=" + temp, null);
            mCursor.requery();
			break;
			
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}


	
	// handler for the background updating
	public Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
        	String strProcess;
			ContentValues values = new ContentValues(2);
        	switch (msg.arg1) {
			case MSG_LOAD_DATA:
				Log.e(TAG,"Загрузка данных...");
				values.clear();
				strProcess = getString(R.string.load_data);
		        values.put(ProviderMetaData.ApiKeysTableMetaData.UPD_DATE, strProcess);
		        getContentResolver().update(
		            		ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + msg.arg2, null);
		        mCursor.requery();
		        
				break;
			case MSG_PROCESS_DATA:
				Log.e(TAG,"Обработка данных...");

				strProcess = getString(R.string.process_data);
		        values.put(ProviderMetaData.ApiKeysTableMetaData.UPD_DATE, strProcess);
		        getContentResolver().update(
		            		ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + msg.arg2, null);
		        mCursor.requery();
		        
				break;

			case MSG_UPDATE_CURSOR:
				Log.e(TAG,"Обновление ListView по таймеру");
				mCursor.requery();
				break;

			case MSG_ERROR_PROCESS_UPDATE:
				Log.e(TAG,"Ошибка!!");
				//Toast.makeText(
					//	null, "Ошибка загрузки данных", Toast.LENGTH_LONG).show();
				//strProcess = "dfgdfgdfgd";
				strProcess = getString(R.string.error_load);
				strProcess = strProcess + "\n" + msg.obj;
				Log.d(TAG,strProcess);
				Log.d(TAG,"ID = " + msg.arg2);
				values.clear();
				values.put(ProviderMetaData.ApiKeysTableMetaData.UPD_DATE, strProcess);
		        getContentResolver().update(
		            		ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + msg.arg2, null);
		        mCursor.requery();
				break;
				
			default:
				break;
			}
        	//dialog.incrementProgressBy(increment);
        }
    };
	
	public void CallUpdateAcc(long id, Activity act, Context ctx, View view) {
		// TODO Auto-generated method stub
		
		
		Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
		Log.d(TAG,uri.toString());
		String temp = "" + id;
		
		Uri uri_acc = uri.withAppendedPath(ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, temp);
		
		//uri.parse(temp);
		Log.d(TAG,"Добавили к uri " + uri_acc.toString());
        //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
		Log.d(TAG,uri.toString());
		mCursor = act.managedQuery(uri_acc, 
				null, //projection
				null, //selection string
				null, //selection args array of strings
				null); //sort order
		mCursor.moveToFirst();
		Log.d(TAG,"Выбираем данные");
		String userId = mCursor.getString(2);
		String APIKey = mCursor.getString(3);
		
		Log.d(TAG,userId + "/"+ APIKey);
		
		AccAccess info = new AccAccess();
		info.ApiKey = mCursor.getString(3);
		info.UserID = mCursor.getString(2);
		info._id = "" + id;
		
		ArrayList<AccAccess> list = new ArrayList<AccAccess>();
		
		list.add(info);
		
		getAccInfo(list, ctx, view);

	}
	
	
	
	private void getAccInfo(ArrayList<AccAccess> ListOfAccess, final Context ctx, final View view) {
		
		for (final AccAccess item : ListOfAccess) {
			
			eve_error = false;
			Thread downloadInfo = new Thread(new Runnable(){

			@Override
			public void run() {
				view.setEnabled(false);
				ArrayList<String> list = new ArrayList<String>();
				Message msg = new Message();
				msg.arg1 = MSG_LOAD_DATA;
				msg.arg2 = Integer.parseInt(item._id);
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
					
			        //msg.arg1 = MSG_PROCESS_DATA;
				    //progressHandler.sendMessage(msg);

			        int i = 0;
			        while (parser.getEventType()!= XmlPullParser.END_DOCUMENT) {
			        	i++;
			        	//list.add("Вошли в цикл " + i);
			        	Log.d(TAG, "Вошли в цикл " + i);
			        	if (parser.getEventType() == XmlPullParser.START_TAG
			                    && parser.getName().equals("error")) {
			        		Log.e(TAG, "ОШИБКА!!");
			        		eve_error = true;
			        		parser.next();
			        		//MyText = parser.getText();
			        		//Log.e(TAG, MyText);

					    	msg.obj = parser.getText();

						    break;
			             }
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
			    Log.d(TAG, " Проверка на ошибку = " + eve_error);    
			    if (!eve_error){
			    	Log.d(TAG, " Ошибки нет"); 
			    	int year =   Integer.parseInt(MyText.substring(0, 4).toString());     
			    	//Log.d(TAG, "year -- " + year);
			        
			    	int month =   Integer.parseInt(MyText.substring(5, 7).toString());     
			    	//Log.d(TAG, "month -- " + month);
			        
			    	int day =   Integer.parseInt(MyText.substring(8, 10).toString());     
			    	//Log.d(TAG, "day -- " + day);
			        
			    	int hour =   Integer.parseInt(MyText.substring(11, 13).toString());     
			    	//Log.d(TAG, "hour -- " + hour);
			        
			    	int minute =   Integer.parseInt(MyText.substring(14, 16).toString());     
			    	//Log.d(TAG, "minute -- " + minute); 
			        
			    	ContentValues values = new ContentValues(2);
			        
			    	Date tempDate = new Date();
			    	values.put(ProviderMetaData.ApiKeysTableMetaData.UPD_DATE, "" + tempDate.getTime() );
			        
			    	tempDate.setYear(year - 1900); // 1900 - это наверное ноль у этого класса
			    	tempDate.setMonth(month - 1); // первый месяц это ноль
			    	tempDate.setDate(day);
			    	tempDate.setHours(hour);
			    	tempDate.setMinutes(minute);
			        
			    	//Log.d(TAG, "tempDate -- " + tempDate);
		              
			    	values.put(ProviderMetaData.ApiKeysTableMetaData.EXP_DATE, "" + tempDate.getTime() );
		              
			    	ctx.getContentResolver().update(
			    			ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + item._id, null);
			    	mCursor.requery();
			    	
			    } else {
			    	Log.d(TAG, " Ошибка есть");
			    	msg.arg1 = MSG_ERROR_PROCESS_UPDATE;
			    	msg.arg2 = Integer.parseInt(item._id);

	        		Log.e(TAG, "Отправка сообщения");
				    progressHandler.sendMessage(msg);
			    }
			    view.setEnabled(true);
			} // eof run()
			}); // eof create Thread
				
			if (!eve_error){
				Log.d(TAG, "Поток запущен");
				downloadInfo.start();
			}
		} // eof for
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	//return super.onCreateOptionsMenu(menu);
    	Log.d(TAG,"Создается меню приложения");
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub
		Log.d(TAG,"" + R.id.item_main_menu_add_acc + "/" + item.getItemId());
		switch (item.getItemId())
    	{
    		case R.id.item_main_menu_add_acc: 
    			Log.d(TAG,"Вызывается активность создания новой записи");
    			String actionName = "com.sapienssoftware.intent.action.GET_URI";
    			Intent call = new Intent(actionName);
    			
    			startActivityForResult(call, REQUEST_CODE);

    			break;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d(TAG,"Получаем результат");
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			if (data.hasExtra("URI")) {
				int id = Integer.parseInt(data.getExtras().getString("URI"));
				Log.d(TAG,"Результат есть id = " + id);
				if (id >= 0) {
					CallUpdateAcc(id, this, this);
				}
				
			}
		}
	}

	
	
}