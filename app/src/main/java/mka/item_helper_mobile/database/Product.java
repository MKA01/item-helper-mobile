package mka.item_helper_mobile.database;

/**
 * Klasa definiująca produkt
 */
public class Product {

    private final String name;
    private final String code;
    private final boolean checked;

    public Product(String name, String code, boolean checked) {
        this.name = name;
        this.code = code;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
