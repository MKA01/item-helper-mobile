package mka.item_helper_mobile.main;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mka.item_helper_mobile.R;
import mka.item_helper_mobile.database.Product;
import mka.item_helper_mobile.database.ProductDatabaseHelper;
import mka.item_helper_mobile.database.ProductDatabaseStructure;
import mka.item_helper_mobile.utils.ProductAdapter;
import mka.item_helper_mobile.utils.XmlParser;

/**
 * Klasa bazowa do uruchamiania aplikacji
 */
public class MainActivity extends AppCompatActivity {

    private ProductDatabaseHelper productDatabaseHelper;
    private ListView productListView;
    private ProductAdapter adapter;

    /**
     * Metoda bazowa
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productDatabaseHelper = new ProductDatabaseHelper(this);
        productListView = findViewById(R.id.shopping_list);

        updateUI();
    }

    /**
     * Nadpisana metoda pochodządza z klasy app.Activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Nadpisana metoda pochodządza z klasy app.Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_product:
                final EditText productEditText = new EditText(this);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.add_product)
                        .setMessage(R.string.what_to_buy)
                        .setView(productEditText)
                        .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String productName = String.valueOf(productEditText.getText());
                                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_NAME, productName);
                                contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);
                                sqLiteDatabase.insertWithOnConflict(ProductDatabaseStructure.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                                sqLiteDatabase.close();
                                updateUI();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                alertDialog.show();
                return true;
            case R.id.action_open_scanner:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda nasłuchuje na zakończenie skanowania i dodaje zeskanowany produkt jeżeli isnieje w bazie
     * Nadpisuje metodę z klasu app.Activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            String productName = null;
            final String scannedProductCode = scanningResult.getContents();
            final XmlParser xmlParser = new XmlParser();
            List<Product> products = new ArrayList<>();

            final InputStream productsXml = this.getResources().openRawResource(R.raw.products);

            try {
                products = xmlParser.parse(productsXml);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            for (Product product : products) {
                if (product.getCode().equals(scannedProductCode)) {
                    productName = product.getName();
                }
            }

            try {
                productsXml.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (productName != null) {
                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_NAME, productName);
                sqLiteDatabase.insertWithOnConflict(ProductDatabaseStructure.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                sqLiteDatabase.close();
                updateUI();
            } else {
                final InputStream productsXml2 = this.getResources().openRawResource(R.raw.products);
                final EditText productEditText = new EditText(this);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new product to and barcode to database")
                        .setMessage("What is the name of the product?")
                        .setView(productEditText)
                        .setPositiveButton("Save product", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String productName = String.valueOf(productEditText.getText());
                                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_NAME, productName);
                                contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);
                                sqLiteDatabase.insertWithOnConflict(ProductDatabaseStructure.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                                sqLiteDatabase.close();
                                try {
                                    xmlParser.updateProducts(productName, scannedProductCode, productsXml2);
                                    productsXml2.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                alertDialog.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Metoda służy do aktualizacji inteferjsu aplikacji. Wywoływana jest po każdej akcji na liście przedmiotów
     */
    private void updateUI() {
        ArrayList<Product> productsList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(ProductDatabaseStructure.ProductEntry.TABLE,
                new String[]{ProductDatabaseStructure.ProductEntry._ID, ProductDatabaseStructure.ProductEntry.PRODUCT_NAME, ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(ProductDatabaseStructure.ProductEntry.PRODUCT_NAME);
            int checkedIndex = cursor.getColumnIndex(ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED);
            productsList.add(new Product(cursor.getString(columnIndex), "1", cursor.getInt(checkedIndex) > 0));
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
     * Metoda służy do usuwania przedmiotu z listy i bazy
     *
     * @param view - widok
     */
    public void deleteProduct(View view) {
        View parent = (View) view.getParent();
        TextView productTextView = parent.findViewById(R.id.product_name);
        String product = String.valueOf(productTextView.getText());
        SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.delete(ProductDatabaseStructure.ProductEntry.TABLE, ProductDatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});
        sqLiteDatabase.close();

        updateUI();
    }

    /**
     * Metoda służy do zaktualizowania kolumny checked w bazie w zależności od wartości checkbox'a
     *
     * @param view - widok
     */
    public void setProductChecked(View view) {
        View parent = (View) view.getParent();
        TextView productTextView = parent.findViewById(R.id.product_name);
        CheckBox checkBox = parent.findViewById(R.id.check_box);
        SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getReadableDatabase();
        String product = String.valueOf(productTextView.getText());
        ContentValues contentValues = new ContentValues();

        if (checkBox.isChecked()) {
            contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED, true);
            sqLiteDatabase.update(ProductDatabaseStructure.ProductEntry.TABLE, contentValues, ProductDatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});
        } else if (!checkBox.isChecked()) {
            contentValues.put(ProductDatabaseStructure.ProductEntry.PRODUCT_CHECKED, false);
            sqLiteDatabase.update(ProductDatabaseStructure.ProductEntry.TABLE, contentValues, ProductDatabaseStructure.ProductEntry.PRODUCT_NAME + " = ?", new String[]{product});
        }
    }
}
