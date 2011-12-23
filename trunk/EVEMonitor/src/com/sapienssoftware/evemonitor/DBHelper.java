package com.sapienssoftware.evemonitor;

import com.sapienssoftware.evemonitor.ProviderMetaData.ApiKeysTableMetaData;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = "DBHelper";
	
	DBHelper(Context context) {
        super(context, 
        		ProviderMetaData.DB_NAME, 
        	null, 
        	ProviderMetaData.DB_VERSION);
        Log.d(TAG,"Был вызван конструктор TrackNumDBHelper");
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Создание таблици трек номеров track_number
    	Log.d(TAG,"Первое обращение к базе. Создаем db (tabl " + ApiKeysTableMetaData.TABLE_NAME + ")");
       
    	String SQL_Text = "CREATE TABLE " + ApiKeysTableMetaData.TABLE_NAME + " ("
                + ApiKeysTableMetaData._ID + " INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + ApiKeysTableMetaData.NAME + " TEXT,"
                + ApiKeysTableMetaData.USER_ID + " TEXT," 
                + ApiKeysTableMetaData.API_KEY + " TEXT,"
                + ApiKeysTableMetaData.EXP_DATE + " INTEGER,"
                + ApiKeysTableMetaData.UPD_DATE + " TEXT"
                + ");";
    	
    	Log.d(TAG, SQL_Text);
    	db.execSQL(SQL_Text);
    	Log.d(TAG, "Таблица создана"); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
