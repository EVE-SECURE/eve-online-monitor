package com.sapienssoftware.evemonitor;

import java.util.Date;
import java.util.HashMap;
import com.sapienssoftware.evemonitor.ProviderMetaData.ApiKeysTableMetaData;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class EVEProvider extends ContentProvider {

private static final String TAG = "EVE Monitor";

    private static HashMap<String, String> sEVEProjectionMap;
    static 
    {
    	sEVEProjectionMap = new HashMap<String, String>();
    	sEVEProjectionMap.put(ApiKeysTableMetaData._ID, 
    			ApiKeysTableMetaData._ID);
    	sEVEProjectionMap.put(ApiKeysTableMetaData.NAME, 
    			ApiKeysTableMetaData.NAME);
    	sEVEProjectionMap.put(ApiKeysTableMetaData.USER_ID, 
    			ApiKeysTableMetaData.USER_ID);
    	sEVEProjectionMap.put(ApiKeysTableMetaData.API_KEY, 
    			ApiKeysTableMetaData.API_KEY);
    	sEVEProjectionMap.put(ApiKeysTableMetaData.EXP_DATE, 
    			ApiKeysTableMetaData.EXP_DATE);
    }
 
    //Provide a mechanism to identify
    //all the incoming uri patterns.
    private static final UriMatcher sUriMatcher;
    private static final int INCOMING_TRACK_COLLECTION_URI_INDICATOR = 1;
    private static final int INCOMING_SINGLE_TRACK_URI_INDICATOR = 2;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ProviderMetaData.AUTHORITY, "api_keys", 
        		INCOMING_TRACK_COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(ProviderMetaData.AUTHORITY, "api_keys/#", 
        		INCOMING_SINGLE_TRACK_URI_INDICATOR);

    }

	private DBHelper mOpenHelper;
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri url, ContentValues inValues) {
		
		Log.d(TAG,"Обработка запроса добавления записи");
		
        ContentValues values;
        if (inValues != null) {
            values = new ContentValues(inValues);
        } else {
            values = new ContentValues();
        }
        
        Log.d(TAG,"Непосредственно добавляем запись");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(ApiKeysTableMetaData.TABLE_NAME, ApiKeysTableMetaData.USER_ID, values);
        Log.d(TAG,"Получаем ID новой записи = " + rowId);
        
        if (rowId > 0) {
            Uri uri = ContentUris.withAppendedId(ApiKeysTableMetaData.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        else {
        	throw new SQLException("Failed to insert row into " + url);
        }
	}

	@Override
	public boolean onCreate() {
		
		Log.d(TAG,"Инициализируем Helper");
		
		mOpenHelper = new DBHelper(getContext());
		
        if (mOpenHelper == null) {
        	Log.d(TAG,"База данных не создана или не найдена");
		} else {
			Log.d(TAG,"База данных создана или найдена");
		}
		
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Log.d(TAG,"query");
		
		String ListField = "*";
    	String TabName = "";
    	String strGroup = "";
    	String strWhere = "";
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Log.d(TAG,"Проверяем коллекция это или одно значение");
        switch (sUriMatcher.match(uri)) {
        case INCOMING_TRACK_COLLECTION_URI_INDICATOR:
        	Log.d(TAG,"Это коллекция");
        	
        	/** Устанавливаем имя таблицы */
        	TabName = ApiKeysTableMetaData.TABLE_NAME;
        	
        	/** Формируем список полей для запроса */
        	if (projection != null) {
        		ListField = "";
        		for (int i = 0; i < projection.length; i++) {
					if (i == projection.length -1) {
						if (projection[i] == ApiKeysTableMetaData.EXP_DATE) {
							ListField = ListField 
									+ "strftime('%d/%m/%Y %H:%M',datetime(" 
									+ ApiKeysTableMetaData.EXP_DATE 
									+ "/1000, 'unixepoch','localtime'))"
									+ " AS " 
									+ ApiKeysTableMetaData.EXP_DATE;
							
						} else{
							ListField = ListField + projection[i];
						}
					} else {
						if (projection[i] == ApiKeysTableMetaData.EXP_DATE) {
							ListField = ListField 
									+ "strftime('%d/%m/%Y %H:%M',datetime(" 
									+ ApiKeysTableMetaData.EXP_DATE 
									+ "/1000, 'unixepoch','localtime'))"
									+ " AS " 
									+ ApiKeysTableMetaData.EXP_DATE
									+ ",";
						} else {
							ListField = ListField + projection[i] + ",";
						}
					}
				}
        		/** в данном конкретном случае добавляем вычисляемое поле
        		 *  которое возвращает через сколько дней исеткает срок подписки
        		 */
        		ListField = ListField + ","
         				+"(CASE "
         				+ "WHEN "
         					+ "(STRFTIME('%j',datetime(" 
         					+ ApiKeysTableMetaData.EXP_DATE 
         					+ "/1000, 'unixepoch','localtime'))- STRFTIME('%j','now')) >= 0 "
        				+ "THEN "
         					+ "'Осталось ' || (STRFTIME('%j',datetime(" 
         					+ ApiKeysTableMetaData.EXP_DATE 
         					+ "/1000, 'unixepoch','localtime'))- STRFTIME('%j','now')) || ' дней' "
         				+ "ELSE 'Subscribt expired' END"	
        				+ ")"
        				+ " AS rem_day";
        		
        		/** 
        		 *  Здесь вычисляем как давно было последнее обновление
        		 */         			
        		ListField = ListField + ","
        				+ "(CASE "
        				+ 	"WHEN " + ApiKeysTableMetaData.UPD_DATE + "<>'Loading data...' " 
        				+   	"THEN CASE " 
        				+			"WHEN " + ApiKeysTableMetaData.UPD_DATE + " NOT LIKE '%Error%' "     //+ ApiKeysTableMetaData.UPD_DATE + "<>'Processing data...' AND "*/
        				+ 	  			"THEN CASE " 
        		   		+ 					"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60 < 1 " 
        		   		+						"THEN 'Обновлялось менее минуты назад' "
        		   		+					"ELSE CASE "
        		   		+						"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60 < 1 " 
        		   		+							"THEN 'Обновлялось ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60) || 'мин. назад' "
        		   		+	 					"ELSE CASE "
        		   		+	 						"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24 < 1 "
        		   		+	 							"THEN 'Обновлялось ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60) || 'ч. назад' "
        		   		+	 						"ELSE CASE "
        		   		+	 							"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24/30 < 1 "
        		   		+	 								"THEN 'Обновлялось ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24) || 'дн. назад' "
        		   		+	 							"ELSE "
        		   		+	 								"'Обновлялось более месяца назад' "
        		   		+	 							"END " //eof проверка на месяц
        		   		+	 						"END " // eof проверка на дни
        		   		+	 					"END " // eof проверка на часы
        		   		+					"END " //eof проверка на минуты
        		   		+			"ELSE "+ ApiKeysTableMetaData.UPD_DATE + " "
        		   		+	  		"END "
        		   		+	"ELSE " + ApiKeysTableMetaData.UPD_DATE + " "
        		   		+ "END) AS " + ApiKeysTableMetaData.UPD_DATE;

        		
         		
         		Log.d(TAG,"==================== ListField ====================");
        		Log.d(TAG,ListField);
        		Log.d(TAG,"===================================================");
			}
        	
        	/** Формируем выражение WHERE */
        	
        	strWhere = getStrWhere(selection, selectionArgs);
        	
        		//String strWhere = selection.indexOf(string).replace("?", "%1$s");
        	Log.d(TAG,strWhere);
        		//strWhere = strWhere.format(strWhere, selectionArgs)
        	
        	
        	
        	/*
        	Log.d(TAG,"Устанавливаем имя таблицы -- " + ApiKeysTableMetaData.TABLE_NAME);
        	qb.setTables(ApiKeysTableMetaData.TABLE_NAME);
        	Log.d(TAG,"Устанавливаем HashMap");
            qb.setProjectionMap(sEVEProjectionMap);
            */
            break;

        case INCOMING_SINGLE_TRACK_URI_INDICATOR:
        	Log.d(TAG,"Это одно значение");
        	/** Устанавливаем имя таблицы */
        	TabName = ApiKeysTableMetaData.TABLE_NAME;
        	
        	/** Формируем список полей для запроса */
        	if (projection != null) {
        		ListField = "";
        		for (int i = 0; i < projection.length; i++) {
					if (i == projection.length -1) {
						ListField = ListField + projection[i];
					} else {
						ListField = ListField + projection[i] + ",";
					}
				}
        		Log.d(TAG,ListField);
			}
        	
        	strWhere = " WHERE _id=" + uri.getPathSegments().get(1); 
        	Log.d(TAG,strWhere);
        	/*
        	
            qb.setTables(ApiKeysTableMetaData.TABLE_NAME);
            qb.setProjectionMap(sEVEProjectionMap);
            qb.appendWhere(ApiKeysTableMetaData._ID + "=" 
            		    + uri.getPathSegments().get(1));
            */
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
        // If no sort order is specified use the default
        Log.d(TAG,"Устанавливаем сортировку");
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ApiKeysTableMetaData.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        Log.d(TAG,"Сортировка будет по '" + orderBy + "'");
        // Get the database and run the query
        Log.d(TAG,"Получаем нашу базу данных");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Log.d(TAG,"После создания базы и таблиц делаем запрос через qb");
        
        String TextSQL = 
        		  "SELECT " + ListField 
        		+ " FROM " + TabName 
          		+ strWhere 
          		+ strGroup + " "
          		+ "ORDER BY " + orderBy + ";";
        Log.d(TAG,TextSQL);
        Cursor c = db.rawQuery(TextSQL, 
       		 null);
        
        
        //Cursor c = qb.query(db, projection, selection, 
        //		   selectionArgs, null, null, orderBy);
        
        /**example of getting a count */
        
        Log.d(TAG,"Получаем кол записей");
        int i = c.getCount();
        Log.d(TAG,"getCount =" + i);
        // Tell the cursor what uri to watch, 
        // so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	private String getStrWhere(String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
    	String strWhere = "";
		if (selection != null & selectionArgs != null){
    		strWhere = selection;
    		int posChar = 0;
    		int iter = 0;
    		int lengthStr = 0;
    		while (strWhere.indexOf("?") != -1) {
    			Log.d(TAG,"итератор/длина строки = " + iter + "/" + selectionArgs.length);
    			if (iter == selectionArgs.length)
    			{
    				Log.d(TAG,"Превышен размер массива selectionArgs");
    			}
    			posChar = strWhere.indexOf("?");
    			Log.d(TAG,"POS = " +posChar);
    			lengthStr = strWhere.length();
    			strWhere = strWhere.substring(0, posChar)  
    					+ selectionArgs[iter] 
    					+ strWhere.substring(posChar + 1, lengthStr);
    			iter++;
    		}
        	strWhere = " WHERE " + strWhere;
		}
	
		return strWhere;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String TabName;
        Log.e(TAG,"Обновление ...");
        switch (sUriMatcher.match(uri)) {
		case INCOMING_TRACK_COLLECTION_URI_INDICATOR:
	        	Log.d(TAG,"Это коллекция");
	           	/** Устанавливаем имя таблицы */
	        	TabName = ApiKeysTableMetaData.TABLE_NAME;
	        	break;

	    case INCOMING_SINGLE_TRACK_URI_INDICATOR:
	        	Log.d(TAG,"Это одно значение");
	        	/** Устанавливаем имя таблицы */
	        	TabName = ApiKeysTableMetaData.TABLE_NAME;

	            break;

	     default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	     }
        
        
		int retVal = db.update(TabName, values, selection, selectionArgs);
        
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
	}

}
