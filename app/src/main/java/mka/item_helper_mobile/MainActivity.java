package mka.item_helper_mobile;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mka.item_helper_mobile.database.ProductContract;
import mka.item_helper_mobile.database.ProductDatabaseHelper;
import mka.item_helper_mobile.utils.ProductsXmlParser;

public class MainActivity extends AppCompatActivity {

    private ProductDatabaseHelper productDatabaseHelper;
    private ListView productListView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productDatabaseHelper = new ProductDatabaseHelper(this);
        productListView = findViewById(R.id.shopping_list);

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_product:
                final EditText productEditText = new EditText(this);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new product to shopping list")
                        .setMessage("What do you want to buy?")
                        .setView(productEditText)
                        .setPositiveButton("Add product", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String productName = String.valueOf(productEditText.getText());
                                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ProductContract.ProductEntry.COL_PRODUCT_NAME, productName);
                                sqLiteDatabase.insertWithOnConflict(ProductContract.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                                sqLiteDatabase.close();
                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            String productName = null;
            final String scannedProductCode = scanningResult.getContents();
            final ProductsXmlParser productsXmlParser = new ProductsXmlParser();
            List<Product> products = new ArrayList<>();

            final InputStream inputStream = this.getResources().openRawResource(R.raw.products);


            try {
                products = productsXmlParser.parse(inputStream);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            for (Product product : products) {
                if (product.getCode().equals(scannedProductCode)) {
                    productName = product.getName();
                }
            }

            if (productName != null) {
                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProductContract.ProductEntry.COL_PRODUCT_NAME, productName);
                sqLiteDatabase.insertWithOnConflict(ProductContract.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                sqLiteDatabase.close();
                try {
                    productsXmlParser.updateProducts("jajaja", "123321", inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateUI();
            } else {
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
                                contentValues.put(ProductContract.ProductEntry.COL_PRODUCT_NAME, productName);
                                sqLiteDatabase.insertWithOnConflict(ProductContract.ProductEntry.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                                sqLiteDatabase.close();
                                Log.e("Dupa2", inputStream.toString());
//                                try {
//                                    productsXmlParser.updateProducts(productName, scannedProductCode, inputStream);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }

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


    private void updateUI() {
        ArrayList<String> productsList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(ProductContract.ProductEntry.TABLE,
                new String[]{ProductContract.ProductEntry._ID, ProductContract.ProductEntry.COL_PRODUCT_NAME},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COL_PRODUCT_NAME);
            productsList.add(cursor.getString(columnIndex));
        }

        if (adapter == null) {
            adapter = new ArrayAdapter<>(this, R.layout.shopping_list, R.id.product_name, productsList);
            productListView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(productsList);
            adapter.notifyDataSetChanged();
        }

        cursor.close();
        sqLiteDatabase.close();
    }

    public void deleteProduct(View view) {
        View parent = (View) view.getParent();
        TextView productTextView = parent.findViewById(R.id.product_name);
        String product = String.valueOf(productTextView.getText());
        SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.delete(ProductContract.ProductEntry.TABLE, ProductContract.ProductEntry.COL_PRODUCT_NAME + " = ?", new String[]{product});
        sqLiteDatabase.close();

        updateUI();
    }
}
