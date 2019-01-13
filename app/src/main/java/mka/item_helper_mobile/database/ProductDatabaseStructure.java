package mka.item_helper_mobile.database;

import android.provider.BaseColumns;

/**
 * Klasa definiująca strukturę bazy przedmiotów
 */
public class ProductDatabaseStructure {

    static final String DB_NAME = "mka.item_helper_mobile.database";
    static final int DB_VERSION = 4;

    public class ProductEntry implements BaseColumns {
        public static final String TABLE = "products";

        public static final String PRODUCT_NAME = "name";
        public static final String PRODUCT_CHECKED = "checked";
    }
}
