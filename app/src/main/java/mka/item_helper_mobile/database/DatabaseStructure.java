package mka.item_helper_mobile.database;

import android.provider.BaseColumns;

/**
 * Klasa definiująca strukturę bazy danych
 */
public class DatabaseStructure {

    public static final String DB_NAME = "mka.item_helper_mobile.database";
    public static final int DB_VERSION = 5;

    /**
     * Definicja tabeli produktów w bazie danych
     */
    public class ProductEntry implements BaseColumns {
        public static final String PRODUCT_TABLE = "products";
        public static final String PRODUCT_NAME = "name";
        public static final String PRODUCT_CHECKED = "checked";
    }

    /**
     * Definicja tabeli kodów kreskowych w bazie danych
     */
    public class BarcodeEntry implements BaseColumns {
        public static final String BARCODE_TABLE = "codes";
        public static final String BARCODE_PRODUCT_NAME = "name";
        public static final String BARCODE_PRODUCT_CODE = "code";
    }
}
