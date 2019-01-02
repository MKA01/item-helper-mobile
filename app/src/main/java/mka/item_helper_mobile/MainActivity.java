package mka.item_helper_mobile;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import mka.item_helper_mobile.database.ProductContract;
import mka.item_helper_mobile.database.ProductDatabaseHelper;

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
                                String product = String.valueOf(productEditText.getText());
                                SQLiteDatabase sqLiteDatabase = productDatabaseHelper.getWritableDatabase();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ProductContract.ProductEntry.COL_PRODUCT_NAME, product);
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
                Intent intent = new Intent(this, BarcodeScanner.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
