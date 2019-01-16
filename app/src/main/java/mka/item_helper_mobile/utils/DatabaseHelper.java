package mka.item_helper_mobile.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mka.item_helper_mobile.database.DatabaseStructure;

/**
 * Klasa tworząca i aktualizująca tabele bazy danych
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseStructure.DB_NAME, null, DatabaseStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createTable = "CREATE TABLE " + DatabaseStructure.ProductEntry.PRODUCT_TABLE + " ( " +
                DatabaseStructure.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseStructure.ProductEntry.PRODUCT_NAME + " TEXT NOT NULL, " +
                DatabaseStructure.ProductEntry.PRODUCT_CHECKED + " BOOLEAN);";

        database.execSQL(createTable);

        createTable = "CREATE TABLE " + DatabaseStructure.BarcodeEntry.BARCODE_TABLE + " ( " +
                DatabaseStructure.BarcodeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseStructure.BarcodeEntry.BARCODE_PRODUCT_NAME + " TEXT NOT NULL, " +
                DatabaseStructure.BarcodeEntry.BARCODE_PRODUCT_CODE + " TEXT NOT NULL UNIQUE);";

        database.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + DatabaseStructure.ProductEntry.PRODUCT_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + DatabaseStructure.BarcodeEntry.BARCODE_TABLE);

        onCreate(database);
    }
}
