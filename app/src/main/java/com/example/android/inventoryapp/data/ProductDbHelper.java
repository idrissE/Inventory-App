package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventoryApp.db";
    private static final int DATABASE_VERSION = 1;

    ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + InventoryContract.ProductEntry.PRODUCT_TABLE_NAME + "(" +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_THUMBNAIL + " TEXT NOT NULL," +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, " +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL," +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL," +
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + " TEXT NOT NULL" +
                ")";
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql_drop_product_table = "DROP TABLE " + InventoryContract.ProductEntry.PRODUCT_TABLE_NAME;
        db.execSQL(sql_drop_product_table);
        onCreate(db);
    }
}
