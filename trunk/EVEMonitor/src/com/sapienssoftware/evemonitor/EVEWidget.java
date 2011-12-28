package com.sapienssoftware.evemonitor;

import com.sapienssoftware.evemonitor.ProviderMetaData.ApiKeysTableMetaData;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

public class EVEWidget extends AppWidgetProvider {

	private static final String TAG = "EVE Widget";
	
    private static Cursor mCursor;
    private DBHelper mOpenHelper;
    
    
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		int[] appWidgetIds) {
	// TODO Auto-generated method stub
	super.onUpdate(context, appWidgetManager, appWidgetIds);
	
	final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.test_layout);
	final AppWidgetManager wm = appWidgetManager;
	final int[] ids = appWidgetIds;
	
	
	mOpenHelper = new DBHelper(context);
	
	SQLiteDatabase db = mOpenHelper.getReadableDatabase();

	String[] columns = new String[] {ProviderMetaData.ApiKeysTableMetaData._ID, 
			ProviderMetaData.ApiKeysTableMetaData.NAME, 
			ProviderMetaData.ApiKeysTableMetaData.EXP_DATE};

	String TextSQL = "SELECT " + ProviderMetaData.ApiKeysTableMetaData._ID +"," + 
								 ProviderMetaData.ApiKeysTableMetaData.NAME +"," +
								 ProviderMetaData.ApiKeysTableMetaData.EXP_DATE
			+" FROM " + ApiKeysTableMetaData.TABLE_NAME;
	
	
	Log.d(TAG,"Делаем запрос");
	mCursor = db.rawQuery(TextSQL, null);
	
	/*
	mCursor = db.query(ApiKeysTableMetaData.TABLE_NAME, 
					   columns, 
					   null, 
					   null, 
					   null, 
					   null, 
					   null);
	*/
	Log.d(TAG,"Количество данных " + mCursor.getCount());
	/*

*/
	if (mCursor.getCount() == 3) {
		int NameColInd = mCursor.getColumnIndex(ProviderMetaData.ApiKeysTableMetaData.NAME);
		int ExpColInd = mCursor.getColumnIndex(ProviderMetaData.ApiKeysTableMetaData.EXP_DATE);
		
		
		
		mCursor.moveToFirst();
		views.setTextViewText(R.id.AccName1, mCursor.getString(NameColInd));
		views.setTextViewText(R.id.AccExp1, mCursor.getString(ExpColInd));
		
		mCursor.moveToNext();
		views.setTextViewText(R.id.AccName2, mCursor.getString(NameColInd));
		views.setTextViewText(R.id.AccExp2, mCursor.getString(ExpColInd));
		
		mCursor.moveToNext();
		views.setTextViewText(R.id.AccName3, mCursor.getString(NameColInd));
		views.setTextViewText(R.id.AccExp3, mCursor.getString(ExpColInd));
	}
	
	
	wm.updateAppWidget(ids, views);

	/*
	Uri uri = ProviderMetaData.ApiKeysTableMetaData.CONTENT_URI;
    
    

	//Activity a = (Activity)this.mContext;
	mCursor = managedQuery(uri, 
			projection,
			null, //selection string
			null, //selection args array of strings
			null); //sort order
   
    startManagingCursor(mCursor);
	*/
	
	
}
}
