package com.sapienssoftware.evemonitor;

import android.net.Uri;
import android.provider.BaseColumns;

public class ProviderMetaData {

	public static final String AUTHORITY = "com.sapienssoftware.provider.EVEMonitor";
	
	public static final String DB_NAME = "evemonitor.db";
	public static final int DB_VERSION = 1;
	public static final String API_KEYS_TABLE_NAME = "api_keys";
	//public static final String EVENTS_TABLE_NAME = "events";
	
	private ProviderMetaData () {}
	
	// ���������� ����� ����������� ApiKeysTable
	
	public static final class ApiKeysTableMetaData implements BaseColumns
	{
		private ApiKeysTableMetaData() {}
		public static final String TABLE_NAME = "api_keys";
		// ����������� URI � ���� MIME
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/api_keys");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sapienssoftware.api_keys";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sapienssoftware.api_keys";
		
		// �������� ��������
		
		// ��� ������
		public static final String NAME = "name";
		// ��� ������
		public static final String USER_ID = "user_id";
		// ��� ������
		public static final String API_KEY = "api_key";
		// ��� ����������� ����� System.currentTime
		public static final String EXP_DATE = "exp_date";
		// ��� ����������� ����� System.currentTime
		public static final String UPD_DATE = "upd_date";

		public static final String DEFAULT_SORT_ORDER = NAME + " DESC";
	}
}
