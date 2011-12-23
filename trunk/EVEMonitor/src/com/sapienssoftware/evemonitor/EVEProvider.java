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
		
		Log.d(TAG,"��������� ������� ���������� ������");
		
        ContentValues values;
        if (inValues != null) {
            values = new ContentValues(inValues);
        } else {
            values = new ContentValues();
        }
        
        Log.d(TAG,"��������������� ��������� ������");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(ApiKeysTableMetaData.TABLE_NAME, ApiKeysTableMetaData.USER_ID, values);
        Log.d(TAG,"�������� ID ����� ������ = " + rowId);
        
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
		
		Log.d(TAG,"�������������� Helper");
		
		mOpenHelper = new DBHelper(getContext());
		
        if (mOpenHelper == null) {
        	Log.d(TAG,"���� ������ �� ������� ��� �� �������");
		} else {
			Log.d(TAG,"���� ������ ������� ��� �������");
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
		Log.d(TAG,"��������� ��������� ��� ��� ���� ��������");
        switch (sUriMatcher.match(uri)) {
        case INCOMING_TRACK_COLLECTION_URI_INDICATOR:
        	Log.d(TAG,"��� ���������");
        	
        	/** ������������� ��� ������� */
        	TabName = ApiKeysTableMetaData.TABLE_NAME;
        	
        	/** ��������� ������ ����� ��� ������� */
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
        		/** � ������ ���������� ������ ��������� ����������� ����
        		 *  ������� ���������� ����� ������� ���� �������� ���� ��������
        		 */
        		ListField = ListField + ","
         				+"(CASE "
         				+ "WHEN "
         					+ "(STRFTIME('%j',datetime(" 
         					+ ApiKeysTableMetaData.EXP_DATE 
         					+ "/1000, 'unixepoch','localtime'))- STRFTIME('%j','now')) >= 0 "
        				+ "THEN "
         					+ "'�������� ' || (STRFTIME('%j',datetime(" 
         					+ ApiKeysTableMetaData.EXP_DATE 
         					+ "/1000, 'unixepoch','localtime'))- STRFTIME('%j','now')) || ' ����' "
         				+ "ELSE 'Subscribt expired' END"	
        				+ ")"
        				+ " AS rem_day";
        		
        		/** 
        		 *  ����� ��������� ��� ����� ���� ��������� ����������
        		 */         			
        		ListField = ListField + ","
        				+ "(CASE "
        				+ 	"WHEN " + ApiKeysTableMetaData.UPD_DATE + "<>'Loading data...' " 
        				+   	"THEN CASE " 
        				+			"WHEN " + ApiKeysTableMetaData.UPD_DATE + " NOT LIKE '%Error%' "     //+ ApiKeysTableMetaData.UPD_DATE + "<>'Processing data...' AND "*/
        				+ 	  			"THEN CASE " 
        		   		+ 					"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60 < 1 " 
        		   		+						"THEN '����������� ����� ������ �����' "
        		   		+					"ELSE CASE "
        		   		+						"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60 < 1 " 
        		   		+							"THEN '����������� ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60) || '���. �����' "
        		   		+	 					"ELSE CASE "
        		   		+	 						"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24 < 1 "
        		   		+	 							"THEN '����������� ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60) || '�. �����' "
        		   		+	 						"ELSE CASE "
        		   		+	 							"WHEN (STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24/30 < 1 "
        		   		+	 								"THEN '����������� ' || abs((STRFTIME('%s', 'now','localtime') - STRFTIME('%s', datetime(" + ApiKeysTableMetaData.UPD_DATE + "/1000, 'unixepoch','localtime')))/60/60/24) || '��. �����' "
        		   		+	 							"ELSE "
        		   		+	 								"'����������� ����� ������ �����' "
        		   		+	 							"END " //eof �������� �� �����
        		   		+	 						"END " // eof �������� �� ���
        		   		+	 					"END " // eof �������� �� ����
        		   		+					"END " //eof �������� �� ������
        		   		+			"ELSE "+ ApiKeysTableMetaData.UPD_DATE + " "
        		   		+	  		"END "
        		   		+	"ELSE " + ApiKeysTableMetaData.UPD_DATE + " "
        		   		+ "END) AS " + ApiKeysTableMetaData.UPD_DATE;

        		
         		
         		Log.d(TAG,"==================== ListField ====================");
        		Log.d(TAG,ListField);
        		Log.d(TAG,"===================================================");
			}
        	
        	/** ��������� ��������� WHERE */
        	
        	strWhere = getStrWhere(selection, selectionArgs);
        	
        		//String strWhere = selection.indexOf(string).replace("?", "%1$s");
        	Log.d(TAG,strWhere);
        		//strWhere = strWhere.format(strWhere, selectionArgs)
        	
        	
        	
        	/*
        	Log.d(TAG,"������������� ��� ������� -- " + ApiKeysTableMetaData.TABLE_NAME);
        	qb.setTables(ApiKeysTableMetaData.TABLE_NAME);
        	Log.d(TAG,"������������� HashMap");
            qb.setProjectionMap(sEVEProjectionMap);
            */
            break;

        case INCOMING_SINGLE_TRACK_URI_INDICATOR:
        	Log.d(TAG,"��� ���� ��������");
        	/** ������������� ��� ������� */
        	TabName = ApiKeysTableMetaData.TABLE_NAME;
        	
        	/** ��������� ������ ����� ��� ������� */
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
        Log.d(TAG,"������������� ����������");
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ApiKeysTableMetaData.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        Log.d(TAG,"���������� ����� �� '" + orderBy + "'");
        // Get the database and run the query
        Log.d(TAG,"�������� ���� ���� ������");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Log.d(TAG,"����� �������� ���� � ������ ������ ������ ����� qb");
        
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
        
        Log.d(TAG,"�������� ��� �������");
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
    			Log.d(TAG,"��������/����� ������ = " + iter + "/" + selectionArgs.length);
    			if (iter == selectionArgs.length)
    			{
    				Log.d(TAG,"�������� ������ ������� selectionArgs");
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
        Log.e(TAG,"���������� ...");
        switch (sUriMatcher.match(uri)) {
		case INCOMING_TRACK_COLLECTION_URI_INDICATOR:
	        	Log.d(TAG,"��� ���������");
	           	/** ������������� ��� ������� */
	        	TabName = ApiKeysTableMetaData.TABLE_NAME;
	        	break;

	    case INCOMING_SINGLE_TRACK_URI_INDICATOR:
	        	Log.d(TAG,"��� ���� ��������");
	        	/** ������������� ��� ������� */
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
