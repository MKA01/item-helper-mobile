package mka.item_helper_mobile.database;

import android.provider.BaseColumns;

public class ProductContract {

    static final String DB_NAME = "mka.item_helper_mobile.database";
    static final int DB_VERSION = 1;

    public class ProductEntry implements BaseColumns {
        public static final String TABLE = "products";

        public static final String COL_PRODUCT_NAME = "name";
    }
}
