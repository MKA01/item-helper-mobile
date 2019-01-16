package mka.item_helper_mobile.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import mka.item_helper_mobile.R;
import mka.item_helper_mobile.database.Product;

/**
 * Klasa rozszerzająca klasę ArrayAdapter. Pomaga pamiętać stan checkbox'a przedmiotu.
 */
public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context, ArrayList<Product> users) {
        super(context, 0, users);
    }

    /**
     * Metoda nadpisana z klasy widget.Adapter. Aktualizuje nazwę przedmiotu i wartość checkbox'a
     *
     * @param position    - pozycja
     * @param convertView - widok
     * @param parent      - rodzic
     * @return - widok
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        Product product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list, parent, false);
        }

        TextView name = convertView.findViewById(R.id.product_name);
        CheckBox checked = convertView.findViewById(R.id.check_box);

        assert product != null;
        name.setText(product.getName());
        checked.setChecked(product.isChecked());

        return convertView;
    }
}
