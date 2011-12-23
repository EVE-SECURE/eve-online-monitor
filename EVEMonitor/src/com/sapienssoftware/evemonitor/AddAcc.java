package com.sapienssoftware.evemonitor;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

public class AddAcc extends Activity {
	
	private static final String TAG = "EVE Monitor";
	
	private static EditText userID;
	private static EditText NameAcc;
	private static EditText APIKey;
	
	private static  String strNameAcc;
	private static  String strUserID;
	private static  String strAPIKey;
	
	private static String defaultUserIDText;
	private static String defaultAPIKeyText;
	private static String defaultNameAccText;
	
	private static boolean isEdit;
	private static String _id;
	
	/**===================================================
	 *           КОНСТРУКТОР КЛАССА
	 *===================================================*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_acc);
	
		Log.d(TAG,"Конструктор");
		
		defaultUserIDText = this.getString(R.string.defaultUserIDText);
		defaultAPIKeyText = this.getString(R.string.defaultAPIKeyText);
		defaultNameAccText = this.getString(R.string.defaultNameAccText);
		
		
		Button btn_close = (Button)this.findViewById(R.id.btCancelNewAcc);
		btn_close.setOnClickListener(OnBtnCloseClickListen);
	
		Button btn_add = (Button)this.findViewById(R.id.btAddAcc);
		btn_add.setOnClickListener(OnBtnAddClickListen);
		Log.d(TAG,"Запущена активность добавления записи");
		
		NameAcc = (EditText)findViewById(R.id.etNameAcc);
		NameAcc.setOnFocusChangeListener(onEditFocusChangeListener);
		NameAcc.addTextChangedListener(onTextChanged);	
		
		userID = (EditText)findViewById(R.id.etUserID);
		userID.setOnFocusChangeListener(onEditFocusChangeListener);
		userID.addTextChangedListener(onTextChanged);
		
		APIKey = (EditText)findViewById(R.id.etAPIKey);
		APIKey.setOnFocusChangeListener(onEditFocusChangeListener);
		APIKey.addTextChangedListener(onTextChanged);
		

		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			strNameAcc = (String) extras.getString("name");
			strUserID  = (String) extras.getString("userId");
			strAPIKey  = (String) extras.getString("APIKey");
			_id        = (String) extras.getString("_id");
			
			NameAcc.setText(strNameAcc);
			userID.setText(strUserID);
			APIKey.setText(strAPIKey);
			
			isEdit = true;
			btn_add.setText(this.getString(R.string.btn_text_edit));
			
		} else {
			strNameAcc = (String) NameAcc.getText().toString();
			strUserID = (String) userID.getText().toString();
			strAPIKey = (String) APIKey.getText().toString();
			
			isEdit = false;
		}
		
		if (strUserID.equals(defaultUserIDText) || strAPIKey.equals(defaultAPIKeyText) || strNameAcc.equals(defaultNameAccText)) {
			btn_add.setEnabled(false);
		}
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG,"OnShow");
		if (isEdit != true){
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		 
			if (clipboard.hasText()) {
				String pasteData = clipboard.getText().toString();
				Log.d(TAG,pasteData);
				int start = pasteData.indexOf("ID:");
				int end = pasteData.indexOf("API Key:");
				if (start != -1 && end != -1){
					String temp = pasteData.substring(start + 4, end);
					userID.setText(temp);
					Log.d(TAG,temp);
				}	
			
				start = pasteData.indexOf("API Key:");
				if (start != -1 && end != -1){
					String temp = pasteData.substring(start + 9, pasteData.length());
					APIKey.setText(temp);
					Log.d(TAG,temp);
				}
		}}
	}
	
	public TextWatcher onTextChanged = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
			Log.d(TAG,"Набрана следующая строка \"" + s +"\"");
			
			Boolean boolNameAcc = NameAcc.getText().toString().equals(defaultNameAccText) || NameAcc.getText().toString().equals("");
			Boolean boolUserID = userID.getText().toString().equals(defaultUserIDText) || userID.getText().toString().equals("");
			Boolean boolAPIKey = APIKey.getText().toString().equals(defaultAPIKeyText) || APIKey.getText().toString().equals("");
			Log.d(TAG,"Строка равна defaultUserIDText  " + boolUserID + "/" + boolAPIKey);
			
			if (boolUserID || boolAPIKey || boolNameAcc) {
				findViewById(R.id.btAddAcc).setEnabled(false);
				Log.d(TAG,"Кнопка заблокирована");
			} else {
				findViewById(R.id.btAddAcc).setEnabled(true);
				Log.d(TAG,"Кнопка разблокирована");
			}
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}
	};
	
	/** Обрабатываем фокусировку на Edit's */
	public OnFocusChangeListener onEditFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			
			EditText temp = (EditText) arg0;
			if (arg1) {
				switch (temp.getId()) {
				case R.id.etNameAcc:
					Log.d(TAG,"Фокус есть для etNameAcc");
					Log.d(TAG,temp.getText().toString() + "/" + defaultNameAccText);
					if (temp.getText().toString().equals(defaultNameAccText)) {
						Log.d(TAG,temp.getText().toString() + "=" + defaultNameAccText);
						temp.setText("");	
					}
					break;
				case R.id.etUserID:
					Log.d(TAG,"Фокус есть для etUserID");
					Log.d(TAG,temp.getText().toString() + "/" + defaultUserIDText);
					if (temp.getText().toString().equals(defaultUserIDText)) {
						Log.d(TAG,temp.getText().toString() + "=" + defaultUserIDText);
						temp.setText("");	
					}
					break;
				case R.id.etAPIKey:
					Log.d(TAG,"Фокус есть для etAPIKey");
					Log.d(TAG,temp.getText().toString() + "/" + defaultAPIKeyText);
					if (temp.getText().toString().equals(defaultAPIKeyText)) {
						Log.d(TAG,temp.getText().toString() + "=" + defaultAPIKeyText);
						temp.setText("");	
					}
					break;
				default:
					break;
				}
			} else {
				Log.d(TAG,"Строка ввода равна \"" + temp.getText().toString() + "\"");
				switch (temp.getId()) {
				case R.id.etNameAcc:
					Log.d(TAG,"Фокус нет для etNameAcc");
					Log.d(TAG,temp.getText().toString() + "/" + defaultNameAccText);
					if (temp.getText().toString().equals("")){
						Log.d(TAG,temp.getText().toString() + "= пусто");
						temp.setText(defaultNameAccText);
					} else {
						
					}
					break;
				case R.id.etUserID:
					Log.d(TAG,"Фокус нет для etUserID");
					Log.d(TAG,temp.getText().toString() + "/" + defaultUserIDText);
					if (temp.getText().toString().equals("")){
						Log.d(TAG,temp.getText().toString() + "= пусто");
						temp.setText(defaultUserIDText);
					} else {
						
					}
					break;
				case R.id.etAPIKey:
					Log.d(TAG,"Фокус нет для etAPIKey");
					Log.d(TAG,temp.getText().toString() + "/" + defaultAPIKeyText);
					if (temp.getText().toString().equals("")){
						Log.d(TAG,temp.getText().toString() + "= пусто");
						temp.setText(defaultAPIKeyText);
					}
					break;
				default:
					break;
					}
			}
		}
	};
	
	/** Обрабатывем нажатие на кнопку Отмена */
	public OnClickListener OnBtnCloseClickListen = new OnClickListener() {
	
		public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG,"Активность добавления записи закрыта");
		finish();
		}
	};
	
	/** Обрабатывем нажатие на кнопку Создать аккаунт */
	public OnClickListener OnBtnAddClickListen = new OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub

			Log.d(TAG,"Создаем новую запись");
		
			strNameAcc = (String) NameAcc.getText().toString();
			Log.d(TAG,"Текст трек номера -- " + strUserID);
			
			strUserID = (String) userID.getText().toString();
			Log.d(TAG,"Текст трек номера -- " + strUserID);
			
			strAPIKey = (String) APIKey.getText().toString();
			Log.d(TAG,"Текст описания -- " + strAPIKey);
		
			//if (userID.getText().toString().equals(defaultUserIDText) || APIKey.getText().toString().equals(defaultAPIKeyText)) {
			
				ContentValues values = new ContentValues();
				Log.d(TAG,"Заполняем значение NAME");
				values.put(ProviderMetaData.ApiKeysTableMetaData.NAME, strNameAcc);
				Log.d(TAG,"Заполняем значение USER_ID");
				values.put(ProviderMetaData.ApiKeysTableMetaData.USER_ID, strUserID);
				Log.d(TAG,"Заполняем значение API_KEY");
				values.put(ProviderMetaData.ApiKeysTableMetaData.API_KEY, strAPIKey);
				
				/** Заполняем дату */
				Log.d(TAG,"Заполняем значение даты");
				//Calendar r = Calendar.getInstance();
				
				//String t = "" + r.getTime();
				//long l = Long.valueOf(r.getTimeInMillis());
				//cv.put(ProviderMetaData.ApiKeysTableMetaData.EXP_DATE, l);
				
				
				
		
				Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
				Log.d(TAG,"Запоминаем URI -- " + uri);
				Log.d(TAG,"Запрос в базу на добавление записи");
				String result = null; 
				if (isEdit){
					//result = getContentResolver().insert(uri, cv);
					//result = "" + getContentResolver().update(uri, values, null, null);
					result = "" + getContentResolver().update(
			    			ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI, values, "_ID=" + _id, null);
					
					
				} else {
					result = getContentResolver().insert(uri, values).getPathSegments().get(1);
				}
				
				
				if (result != null) {
					//String id = result.getPathSegments().get(1);

					if(getIntent().getAction().equals("com.sapienssoftware.intent.action.GET_URI")) {
					    // Возвращаем результат
						Log.e(TAG,"Возвращаем результат");
						Intent data = new Intent();
					    data.putExtra("URI", result);
					    setResult(RESULT_OK, data); // Устанавливаем результат
					    finish(); // Завершаем Activity
					    return; // Завершаем исполнение кода
					}

					//finish();
					//return;
				} else {
					/** Здесь надо описать ошибку */
				} 
		}
	};
}
