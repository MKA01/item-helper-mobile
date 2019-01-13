package mka.item_helper_mobile.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
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

import mka.item_helper_mobile.database.Product;

/**
 * Klasa parsująca plik xml produktu.
 */
public class XmlParser {

    private static final String namespace = null;

    /**
     * Metoda służy do zaktualizowania pliku xml z zapisanymi kodami kreskowymi
     *
     * @param name        - nazwa przedmiotu
     * @param code        - kod przedmiotu
     * @param inputStream - plik
     * @throws Exception -
     */
    public void updateProducts(String name, String code, InputStream inputStream) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element root = document.getDocumentElement();

        List<Product> products = new ArrayList<>();
        products.add(new Product(name, code, false));

        for (Product product : products) {
            Element newProduct = document.createElement("product");
            Element newProductName = document.createElement("name");
            Element newProductCode = document.createElement("code");

            newProductName.appendChild(document.createTextNode(product.getName()));
            newProduct.appendChild(newProductName);

            newProductCode.appendChild(document.createTextNode(product.getCode()));
            newProduct.appendChild(newProductCode);

            root.appendChild(newProduct);
        }

        DOMSource source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult("raw/products.xml");

        transformer.transform(source, result);
    }

    /**
     * Metoda służy do sprawsowania pliku xml z produktami i ich kodem
     *
     * @param inputStream - plik
     * @return - sparsowany plik xml
     * @throws XmlPullParserException -
     * @throws IOException            -
     */
    public List<Product> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
        XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
        xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xmlPullParser.setInput(inputStream, null);

//        xmlPullParser.next();
        xmlPullParser.next();

        return readProducts(xmlPullParser);
    }

    /**
     * Metoda służy do zczytania produktów z pliku xml
     *
     * @param xmlPullParser - parser
     * @return - produkty
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private List<Product> readProducts(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        List<Product> products = new ArrayList<>();

        xmlPullParser.require(XmlPullParser.START_TAG, namespace, "products");
        while (xmlPullParser.next() != XmlPullParser.END_TAG) {
            if (xmlPullParser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tag = xmlPullParser.getName();

            if (tag.equals("product")) {
                products.add(readProduct(xmlPullParser));
            } else {
                skip(xmlPullParser);
            }
        }

        return products;
    }

    /**
     * Metoda śłuży do zczytania produktu z pliku xml
     *
     * @param xmlPullParser - parser
     * @return - produkt
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private Product readProduct(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(XmlPullParser.START_TAG, namespace, "product");

        String name = null;
        String code = null;

        while (xmlPullParser.next() != XmlPullParser.END_TAG) {
            if (xmlPullParser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tag = xmlPullParser.getName();

            switch (tag) {
                case "name":
                    name = readName(xmlPullParser);
                    break;
                case "code":
                    code = readCode(xmlPullParser);
                    break;
                default:
                    skip(xmlPullParser);
                    break;
            }
        }

        return new Product(name, code, false);
    }

    /**
     * Metoda służy do zczytania kodu kreskowego produktu
     *
     * @param xmlPullParser - parser
     * @return - kod kreskowy produktu
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private String readCode(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(XmlPullParser.START_TAG, namespace, "code");
        String code = readText(xmlPullParser);
        xmlPullParser.require(XmlPullParser.END_TAG, namespace, "code");

        return code;

    }

    /**
     * Metoda służy do zczytania nazwy produktu
     *
     * @param xmlPullParser - parser
     * @return - nazwa produktu
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private String readName(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(XmlPullParser.START_TAG, namespace, "name");
        String name = readText(xmlPullParser);
        xmlPullParser.require(XmlPullParser.END_TAG, namespace, "name");

        return name;
    }

    /**
     * Metoda służy do zczytania tekstu z pliku xml
     *
     * @param xmlPullParser - parser
     * @return - tekst
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private String readText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String result = "";

        if (xmlPullParser.next() == XmlPullParser.TEXT) {
            result = xmlPullParser.getText();
            xmlPullParser.nextTag();
        }

        return result;
    }

    /**
     * Metoda służy do pominięcia nie interesujących nas tag'ów
     *
     * @param xmlPullParser - parser
     * @throws IOException            -
     * @throws XmlPullParserException -
     */
    private void skip(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        if (xmlPullParser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (xmlPullParser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
