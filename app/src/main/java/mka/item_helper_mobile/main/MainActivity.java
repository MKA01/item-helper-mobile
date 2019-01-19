package mka.item_helper_mobile.main;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Objects;

import mka.item_helper_mobile.R;
import mka.item_helper_mobile.database.DatabaseStructure;
import mka.item_helper_mobile.database.Product;
import mka.item_helper_mobile.utils.DatabaseHelper;
import mka.item_helper_mobile.utils.ProductAdapter;

/**
 * Klasa bazowa do uruchamiania aplikacji
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView productListView;
    private ProductAdapter adapter;

    /**
     * Metoda służy do utrzymania otwartego okna dialogowego podczas obrotu ekranu
     *
     * @param dialog okno dialogowe
     */
    private static void preventDialogDismiss(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * Metoda bazowa służy do otwarcia aplikacji
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        productListView = findViewById(R.id.shopping_list);

        refreshProductList();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.blue));
    }

    /**
     * Nadpisana metoda pochodząca z klasy app.Activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda sprawdza, który przycisk w menu został kliknięty i wykonuje odpowiadającą mu akcje
     * Nadpisuje metodę z klasy app.Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_product:
                addNewProduct("button", null);
                refreshProductList();
                return true;
            case R.id.action_open_scanner:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                refreshProductList();
                return true;
            case R.id.delete_all:
                deleteAllCheckedProducts();
                refreshProductList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda nasłuchuje na zakończenie skanowania. Po otrzymaniu odpowiedzi sprawdza czy kod kreskowy znajduje się w bazie.
     * Jeżeli tak to odczytuje nazwę przedmiotu i dodaje go do listy jeżeli go na niej nie ma.
     * Jeżeli nie to pyta o nazwę przedmiotu i dodaje go do listy i zapamiętuje kod kreskowy.
     * Nadpisuje metodę z klasy app.Activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            final String scannedProductCode = scanningResult.getContents();
            SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.BarcodeEntry.BARCODE_TABLE +
                    " WHERE code = ?", new String[]{scannedProductCode});

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DatabaseStructure.BarcodeEntry.BARCODE_PRODUCT_NAME);
                String productName = cursor.getString(columnIndex);
                Cursor innerCursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.ProductEntry.PRODUCT_TABLE +
                        " WHERE name = ?", new String[]{productName});

                if (innerCursor.moveToFirst()) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.duplicate, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    ContentValues productValues = new ContentValues();
                    productValues.put(DatabaseStructure.ProductEntry.PRODUCT_NAME, productName);
                    productValues.put(DatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);

                    sqLiteDatabase.insertWithOnConflict(DatabaseStructure.ProductEntry.PRODUCT_TABLE, null, productValues, SQLiteDatabase.CONFLICT_REPLACE);
                }

                innerCursor.close();
            } else {
                addNewProduct("scanner", scannedProductCode);
            }

            cursor.close();
            sqLiteDatabase.close();

            refreshProductList();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_scan_data, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Metoda otwiera okno dialogowe, w którym podaje się nazwę przedmiotu.
     * W zależności od parametrów z jakimi jest wywołana dodaje do bazy kod kreskowy i produkt lub sam produkt.
     *
     * @param mode        informacja skąd została wywołana metoda
     * @param productCode kod kreskowy przedmiotu
     */
    private void addNewProduct(final String mode, final String productCode) {
        int title = R.string.error;
        int message = R.string.error;

        if (mode.equals("button")) {
            title = R.string.add_product;
            message = R.string.what_to_buy;
        } else if (mode.equals("scanner")) {
            title = R.string.add_barcode_product;
            message = R.string.what_is_the_name;
        }

        final EditText productEditText = new EditText(this);
        AlertDialog setItemNameDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setView(productEditText)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String productName = String.valueOf(productEditText.getText());
                        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

                        if (mode.equals("scanner")) {
                            ContentValues barcodeValues = new ContentValues();
                            barcodeValues.put(DatabaseStructure.BarcodeEntry.BARCODE_PRODUCT_NAME, productName);
                            barcodeValues.put(DatabaseStructure.BarcodeEntry.BARCODE_PRODUCT_CODE, productCode);

                            sqLiteDatabase.insertWithOnConflict(DatabaseStructure.BarcodeEntry.BARCODE_TABLE, null, barcodeValues, SQLiteDatabase.CONFLICT_REPLACE);
                        }

                        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.ProductEntry.PRODUCT_TABLE +
                                " WHERE name = ?", new String[]{productName});

                        if (cursor.moveToFirst()) {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.duplicate, Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            ContentValues productValues = new ContentValues();
                            productValues.put(DatabaseStructure.ProductEntry.PRODUCT_NAME, productName);
                            productValues.put(DatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);

                            sqLiteDatabase.insertWithOnConflict(DatabaseStructure.ProductEntry.PRODUCT_TABLE, null, productValues, SQLiteDatabase.CONFLICT_REPLACE);
                        }

                        cursor.close();
                        sqLiteDatabase.close();

                        refreshProductList();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        setItemNameDialog.show();
        preventDialogDismiss(setItemNameDialog);
    }

    /**
     * Metoda służy do aktualizacji interfejsu aplikacji. Wywoływana jest po każdej akcji na liście przedmiotów
     * Dzięki niej pamiętane są zaznaczone produkty
     */
    private void refreshProductList() {
        ArrayList<Product> productsList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(DatabaseStructure.ProductEntry.PRODUCT_TABLE,
                new String[]{DatabaseStructure.ProductEntry._ID, DatabaseStructure.ProductEntry.PRODUCT_NAME, DatabaseStructure.ProductEntry.PRODUCT_CHECKED},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex(DatabaseStructure.ProductEntry.PRODUCT_NAME);
            int checkedIndex = cursor.getColumnIndex(DatabaseStructure.ProductEntry.PRODUCT_CHECKED);
            productsList.add(new Product(cursor.getString(nameIndex), cursor.getInt(checkedIndex) > 0));
        }

        if (adapter == null) {
            adapter = new ProductAdapter(this, productsList);
            productListView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(productsList);
            adapter.notifyDataSetChanged();
        }

        cursor.close();
        sqLiteDatabase.close();
    }

    /**
     * Metoda służy do usuwania przedmiotów
     *
     * @param view widok
     */
    public void deleteProduct(View view) {
        View parent = (View) view.getParent();
        TextView productTextView = parent.findViewById(R.id.product_name);
        String product = String.valueOf(productTextView.getText());

        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.delete(DatabaseStructure.ProductEntry.PRODUCT_TABLE, DatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});

        sqLiteDatabase.close();
        refreshProductList();
    }

    /**
     * Metoda służy do usunięcia wszystkich zaznaczonych przedmiotów
     */
    public void deleteAllCheckedProducts() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        sqLiteDatabase.delete(DatabaseStructure.ProductEntry.PRODUCT_TABLE, DatabaseStructure.ProductEntry.PRODUCT_CHECKED + " = 1", null);

        sqLiteDatabase.close();
        refreshProductList();
    }

    /**
     * Metoda służy do zaktualizowania kolumny checked w bazie w zależności od stanu checkbox'a
     *
     * @param view widok
     */
    public void setProductChecked(View view) {
        View parent = (View) view.getParent();
        TextView productTextView = parent.findViewById(R.id.product_name);
        CheckBox checkBox = parent.findViewById(R.id.check_box);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        String product = String.valueOf(productTextView.getText());
        ContentValues contentValues = new ContentValues();

        if (checkBox.isChecked()) {
            contentValues.put(DatabaseStructure.ProductEntry.PRODUCT_CHECKED, true);
            sqLiteDatabase.update(DatabaseStructure.ProductEntry.PRODUCT_TABLE, contentValues, DatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});
        } else if (!checkBox.isChecked()) {
            contentValues.put(DatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);
            sqLiteDatabase.update(DatabaseStructure.ProductEntry.PRODUCT_TABLE, contentValues, DatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});
        }
    }
}
