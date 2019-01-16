package mka.item_helper_mobile.database;

/**
 * Klasa odpowiadająca obiektowi z tabeli produkt
 */
public class Product {

    private final String name;
    private final boolean checked;

    public Product(String name, boolean checked) {
        this.name = name;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", checked=" + checked +
                '}';
    }
}
