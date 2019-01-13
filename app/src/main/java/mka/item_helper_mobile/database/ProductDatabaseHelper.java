package mka.item_helper_mobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Klasa tworząca i aktualizująca tabele bazy danych
 */
public class ProductDatabaseHelper extends SQLiteOpenHelper {

    public ProductDatabaseHelper(Context context) {
        super(context, ProductDatabaseStructure.DB_NAME, null, ProductDatabaseStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createTable = "CREATE TABLE " + ProductDatabaseStructure.ProductEntry.TABLE + " ( " +
                ProductDatabaseStructure.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductDatabaseStructure.ProductEntry.PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED + " BOOLEAN);";

        database.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + ProductDatabaseStructure.ProductEntry.TABLE);

        onCreate(database);
    }
}
