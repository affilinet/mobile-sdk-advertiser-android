package com.example.affilinetadvertiserdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rm.affilinet.SessionCallback;
import com.rm.affilinet.act.profiling.ACTAddToCartProfiling;
import com.rm.affilinet.act.profiling.ACTCartViewProfiling;
import com.rm.affilinet.act.profiling.ACTCategoryViewProfiling;
import com.rm.affilinet.act.profiling.ACTCheckoutProfiling;
import com.rm.affilinet.act.profiling.ACTPageViewProfiling;
import com.rm.affilinet.act.profiling.ACTProductViewProfiling;
import com.rm.affilinet.act.profiling.ACTRemoveFromCartProfiling;
import com.rm.affilinet.act.profiling.ACTSearchViewProfiling;
import com.rm.affilinet.act.tracking.ACTBasketTracking;
import com.rm.affilinet.act.tracking.ACTLeadTracking;
import com.rm.affilinet.act.tracking.ACTSaleTracking;
import com.rm.affilinet.advertiser.Session;
import com.rm.affilinet.communication.Request;
import com.rm.affilinet.communication.RequestResponse;
import com.rm.affilinet.models.Account;
import com.rm.affilinet.models.AdvertiserAccount;
import com.rm.affilinet.models.Gender;
import com.rm.affilinet.models.OTBasketItem;
import com.rm.affilinet.models.OTOrderRate;
import com.rm.affilinet.models.OTRateMode;
import com.rm.affilinet.models.Platform;
import com.rm.affilinet.models.RTDatingCustomer;
import com.rm.affilinet.models.RTDatingProduct;
import com.rm.affilinet.models.RTOrder;
import com.rm.affilinet.models.RTOrderItem;
import com.rm.affilinet.models.RTProduct;
import com.rm.affilinet.models.RTProductCategory;
import com.rm.affilinet.models.RTTravelProduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final String TAG = "affilinet";

    private ProgressDialog progress;

    class LocalSessionCallback implements SessionCallback {

        @Override
        public void onRequestsFinished() {
            Log.v(TAG, "Finished all requests in Session");
            hideProgressDialog();

        }

        @Override
        public void onRequestsError(Error error) {
            Log.v(TAG, "Failed requests in Session: " + error.getMessage());
            hideProgressDialog();
        }

        @Override
        public void onRequestResponse(Request request, RequestResponse response) {
            if (response.error != null) {
                Log.v(TAG, "Request Completed With Error: " + response.error.getMessage());
                // for App Download Tracking
                saveToPrefs(MainActivity.this, "PREF_DOWNLOAD_TRACKING", "true");
            } else {
                Log.v(TAG, "Request Completed successfully");
            }
        }

    }

    int mAccountId = 123; // Please replace with your PROGRAM-ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Integer> ids = new ArrayList<>();
        ids.add(mAccountId);
        Account account = new AdvertiserAccount(ids);
        Session session = Session.getInstance();
        try {
            session.open(getApplicationContext(), account, Platform.DE); // Please replace with your country code
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }

        runAdvertiserTests();

        // App Download Tracking should only be executed if preference PREF_DOWNLOAD_TRACKING was not set aka as long as the MainActivity was not executed once.
        if (getFromPrefs(this, "PREF_DOWNLOAD_TRACKING", null) == null) {
            runAppDownloadTracking();
        }

        initWeb(session);
    }

    private void initWeb(Session session) {
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //webView.loadUrl(String.format("http://%s/click.asp?ref=%d&js=1&site=%d&b=1", session.getCountry().getDomain(), session.getCountry().getRef(), mAccountId));
        webView.loadData(
                String.format(getString(R.string.click_html), session.getCountry().getDomain(), session.getCountry().getRef(), mAccountId),
                "text/html",
                "UTF-8"
        );

    }

    private void runAdvertiserTests() {
        setContentView(R.layout.activity_main);

        final ListView listview = (ListView) findViewById(R.id.main_menu_list_view);
        String[] values = new String[]{"Basket Order Tracking", "Lead Order Tracking", "Sale Order Tracking",
                "Product View Profiling", "Add To Cart Profiling",
                "Remove from Cart Profiling", "Checkout Profiling", "Cart View Profiling", "Category View Profiling",
                "Page View Profiling", "Search Page Profiling"};

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                switch (position) {
                    case 0:
                        runBasketTracking();
                        break;
                    case 1:
                        runLeadTracking();
                        break;
                    case 2:
                        runSaleTracking();
                        break;
                    case 3:
                        runProductViewProfiling();
                        break;
                    case 4:
                        runAddToCartProfiling();
                        break;
                    case 5:
                        runRemoveFromCartProfiling();
                        break;
                    case 6:
                        runCheckoutProfiling();
                        break;
                    case 7:
                        runCartViewProfiling();
                        break;
                    case 8:
                        runCategoryViewProfiling();
                        break;
                    case 9:
                        runPageViewProfiling();
                        break;
                    case 10:
                        runSearchViewProfiling();
                        break;

                    default:
                        break;
                }
            }

        });

    }

    private void runBasketTracking() {
        OTBasketItem basketItem = new OTBasketItem();
        // mandatory parameters
        basketItem.articleNumber = UUID.randomUUID().toString(); // Replace with your product id
        basketItem.singlePrice = 20.45;
        basketItem.quantity = 1;
        // optional parameters
        basketItem.productName = "amazing%20product";
        basketItem.category = "jeans";
        basketItem.brand = "Versace";
        //basketItem.orderRate = new OTOrderRate(1, OTRateMode.SALE);
        List<String> properties = new ArrayList<>();
        properties.add("Size%3A%20XXL");
        properties.add("Color%3A%20green");
        basketItem.properties = properties;

        OTBasketItem basketItem2 = new OTBasketItem();
        // mandatory parameters
        basketItem2.articleNumber = UUID.randomUUID().toString(); // Replace with your product id
        basketItem2.singlePrice = 40.45;
        basketItem2.quantity = 1;
        // optional parameters
        basketItem2.productName = "amazing%20product%202";
        basketItem2.category = "jeans%202";
        basketItem2.brand = "Versace%202";
        //basketItem2.orderRate = new OTOrderRate(1, OTRateMode.SALE);
        List<String> properties2 = new ArrayList<>();
        properties2.add("Size%3A%20XXL");
        properties2.add("Color%3A%20green");
        basketItem.properties = properties2;

        OTBasketItem basketItem3 = new OTBasketItem();
        // mandatory parameters
        basketItem3.articleNumber = UUID.randomUUID().toString(); // Replace with your product id
        basketItem3.singlePrice = 40.45;
        basketItem3.quantity = 3;
        // optional parameters
        basketItem3.productName = "amazing%20product%203";
        basketItem3.category = "jeans%203";
        basketItem3.brand = "Versace%203";
        //basketItem3.orderRate = new OTOrderRate(6, OTRateMode.SALE);

        List<OTBasketItem> basketItems = new ArrayList<>();
        basketItems.add(basketItem);
        basketItems.add(basketItem2);
        basketItems.add(basketItem3);

        RTOrder order = new RTOrder();
        // mandatory parameters
        order.orderId = UUID.randomUUID().toString(); // Replace with your order id
        //optional parameters
        order.orderDescription = "new-custom-order-description";

        ACTBasketTracking actBasketTracking = new ACTBasketTracking(Session.getInstance(), order);
        // mandatory parameters
        actBasketTracking.setBasketItems(basketItems);
        // optional parameters
        actBasketTracking.setOrderRate(new OTOrderRate(1, OTRateMode.SALE));
        actBasketTracking.addProgramSubId("custom-program-subid1");
        actBasketTracking.addProgramSubId("custom-program-subid2");
        actBasketTracking.setCurrency("EUR");
        actBasketTracking.setVoucherCode("voucher-code");

        this.executeRequest(actBasketTracking);
    }

    private void runSaleTracking() {
        RTOrder order = new RTOrder();
        // mandatory parameters
        order.orderId = UUID.randomUUID().toString(); // Replace with your order id
        order.total = 324.45;
        //optional parameter
        order.orderDescription = "new-custom-order-description";

        ACTSaleTracking actSaleTracking = new ACTSaleTracking(Session.getInstance(), order);
        // mandatory parameters
        actSaleTracking.setOrderRate(new OTOrderRate(1, OTRateMode.SALE));
        // optional parameters
        actSaleTracking.addProgramSubId("custom-program-subid1");
        actSaleTracking.addProgramSubId("custom-program-subid2");
        actSaleTracking.setCurrency("EUR");
        actSaleTracking.setVoucherCode("voucher-code");

        this.executeRequest(actSaleTracking);
    }

    private void runLeadTracking() {
        RTOrder order = new RTOrder();
        // mandatory parameter
        order.orderId = UUID.randomUUID().toString(); // Replace with your order id
        // optional parameters
        order.total = 24.45;
        order.orderDescription = "new-custom-order-description";

        ACTLeadTracking actLeadTracking = new ACTLeadTracking(Session.getInstance(), order);
        // mandatory parameters
        actLeadTracking.setOrderRate(new OTOrderRate(1, OTRateMode.LEAD));
        // optional parameters
        actLeadTracking.addProgramSubId("custom-program-subid1");
        actLeadTracking.addProgramSubId("custom-program-subid2");
        actLeadTracking.setCurrency("EUR");
        actLeadTracking.setVoucherCode("voucher-code");

        this.executeRequest(actLeadTracking);
    }

    private void runAppDownloadTracking() {
        RTOrder order = new RTOrder();
        // mandatory parameter
        order.orderId = UUID.randomUUID().toString();
        order.orderDescription = "App-Download";

        ACTLeadTracking actLeadTracking = new ACTLeadTracking(Session.getInstance(), order);
        // mandatory parameters
        actLeadTracking.setOrderRate(new OTOrderRate(1, OTRateMode.LEAD));

        this.executeRequest(actLeadTracking);
    }

    private void runProductViewProfiling() {
        RTProduct product = createProduct();
        //RTProduct product = createTravelProduct();
        //RTProduct product = createDatingProduct();

        ACTProductViewProfiling actProductViewProfiling = new ACTProductViewProfiling(Session.getInstance(), product, "EUR");
        actProductViewProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com");

        this.executeRequest(actProductViewProfiling);
    }

    private void runRemoveFromCartProfiling() {
        RTProduct product = createProduct();
        RTOrderItem item1 = new RTOrderItem();
        // mandatory parameters
        item1.quantity = 2;
        item1.product = product;

        ACTRemoveFromCartProfiling actRemoveFromCartProfiling = new ACTRemoveFromCartProfiling(Session.getInstance(), item1, "EUR");

        this.executeRequest(actRemoveFromCartProfiling);
    }

    private void runAddToCartProfiling() {
        RTProduct product = createProduct();
        RTOrderItem item1 = new RTOrderItem();
        // mandatory parameters
        item1.quantity = 1;
        item1.product = product;

        ACTAddToCartProfiling actAddToCartProfiling = new ACTAddToCartProfiling(Session.getInstance(), item1, "EUR");
        this.executeRequest(actAddToCartProfiling);
    }

    private void runCheckoutProfiling() {
        RTOrder order = new RTOrder();
        // mandatory parameters
        order.orderId = UUID.randomUUID().toString();
        order.total = 324.45;
        order.items = new ArrayList<>();

        RTProduct product = createProduct();
        RTOrderItem item1 = new RTOrderItem();
        // mandatory parameters
        item1.quantity = 2;
        item1.product = product;

        RTTravelProduct travelProduct = createTravelProduct();
        RTOrderItem item2 = new RTOrderItem();
        // mandatory parameters
        item2.quantity = 1;
        item2.product = travelProduct;

        RTDatingProduct datingProduct = createDatingProduct();
        RTOrderItem item3 = new RTOrderItem();
        // mandatory parameters
        item3.quantity = 1;
        item3.product = datingProduct;

        order.items.add(item1);
        order.items.add(item2);
        order.items.add(item3);

        ACTCheckoutProfiling actCheckoutProfiling = new ACTCheckoutProfiling(Session.getInstance(), order, "EUR");
        actCheckoutProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com%2Fcheckout.html");
        actCheckoutProfiling.setPaymentType("paypal");
        actCheckoutProfiling.setShippingGrossPrice("4.95");
        actCheckoutProfiling.setShippingTax(6.0);
        actCheckoutProfiling.setShippingType("standard");
        actCheckoutProfiling.setVoucherCode("v9917417431784");
        actCheckoutProfiling.setVoucherCodeDiscount(12.0);
        actCheckoutProfiling.setTax(20.0);
        actCheckoutProfiling.setCustomer(datingProduct.customer);

        this.executeRequest(actCheckoutProfiling);
    }

    private void runCartViewProfiling() {
        List<RTOrderItem> items = new ArrayList<>();

        RTProduct product = createProduct();
        RTOrderItem item1 = new RTOrderItem();
        // mandatory parameters
        item1.quantity = 2;
        item1.product = product;

        RTTravelProduct travelProduct = createTravelProduct();
        RTOrderItem item2 = new RTOrderItem();
        // mandatory parameters
        item2.quantity = 1;
        item2.product = travelProduct;

        RTDatingProduct datingProduct = createDatingProduct();
        RTOrderItem item3 = new RTOrderItem();
        // mandatory parameters
        item3.quantity = 1;
        item3.product = datingProduct;

        items.add(item1);
        items.add(item2);
        items.add(item3);

        ACTCartViewProfiling actCartViewProfiling = new ACTCartViewProfiling(Session.getInstance(), items, "EUR");
        actCartViewProfiling.setTax(20.0);
        actCartViewProfiling.setTotalPrice(100.0);
        actCartViewProfiling.setVoucherCode("3iidd-12d3");
        actCartViewProfiling.setVoucherCodeDiscount(5.0);
        actCartViewProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com");

        this.executeRequest(actCartViewProfiling);
    }

    private void runCategoryViewProfiling() {
        RTProductCategory productCategory = new RTProductCategory();
        // mandatory parameters
        productCategory.categoryId = "c1";
        productCategory.categoryName = "category%20name";
        // optional parameters
        productCategory.clickURL = "http%3A%2F%2Fcategory-clickurl.com";
        productCategory.imageURL = "http%3A%2F%2Fcategory-imageurl.com";
        productCategory.pathItems = new ArrayList<>();
        productCategory.pathItems.add("Clothes");
        productCategory.pathItems.add("Shirts");
        productCategory.pathItems.add("T-Shirt");

        ACTCategoryViewProfiling actCategoryViewProfiling = new ACTCategoryViewProfiling(Session.getInstance(), productCategory);
        actCategoryViewProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com%2Fcategoryview.html");

        this.executeRequest(actCategoryViewProfiling);
    }

    private void runPageViewProfiling() {
        ACTPageViewProfiling actPageViewProfiling = new ACTPageViewProfiling(Session.getInstance());
        // mandatory parameters
        actPageViewProfiling.setPageName("pageName");
        // optional parameters
        actPageViewProfiling.setPageUrl("pageUrl");
        actPageViewProfiling.setPageType("pageType");
        actPageViewProfiling.setPageCategory("pageCategory");
        actPageViewProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com%2Fpageview.html");

        this.executeRequest(actPageViewProfiling);
    }

    private void runSearchViewProfiling() {
        RTProduct product = new RTProduct();
        product.productId = UUID.randomUUID().toString(); // Replace with your productId
        product.name = "name%201";

        RTProduct product2 = new RTProduct();
        product2.productId = UUID.randomUUID().toString(); // Replace with your productId
        product2.name = "name%202";

        List<RTProduct> products = new ArrayList<>();
        products.add(product);
        products.add(product2);

        List<String> keywords = new ArrayList<>();
        keywords.add("keyword%201");
        keywords.add("keyword%202");

        ACTSearchViewProfiling actSearchViewProfiling = new ACTSearchViewProfiling(Session.getInstance());
        actSearchViewProfiling.setKeywords(keywords);
        actSearchViewProfiling.setProducts(products);
        actSearchViewProfiling.setRefererUrl("http%3A%2F%2Fadvertiser.com%2Fsearch.html");

        this.executeRequest(actSearchViewProfiling);
    }

    private void executeRequest(Request request) {
        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        try {
            this.showProgressDialog();
            Session.getInstance().executeRequests(requests, new LocalSessionCallback());
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
    }

    private void showProgressDialog() {
        if (this.progress == null) {
            this.progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading...");
        }
        progress.show();
    }

    private void hideProgressDialog() {
        if (progress != null) {
            progress.dismiss();
        }
    }

    private RTProduct createProduct() {
        RTProduct product = new RTProduct();
        // mandatory parameters
        product.productId = UUID.randomUUID().toString(); // Replace with your productId
        product.name = "Amazing%20Product";
        product.price = 40.45;
        // optional parameters
        product.oldPrice = 42.99;
        product.category = new RTProductCategory();
        product.category.pathItems = new ArrayList<>();
        product.category.pathItems.add("Clothes");
        product.category.pathItems.add("Shoes");
        product.category.pathItems.add("Flip%20Flops");
        product.category.clickURL = "http%3A%2F%2Fadvertiser.com%2Fcategory%2Fclick.html";
        product.category.imageURL = "http%3A%2F%2Fadvertiser.com%2Fcategory%2Fimage.png";
        product.brand = "Amazing%20Brand";
        product.inStock = true;
        product.rating = 7;
        product.onSale = true;
        product.accessory = false;
        product.clickURL = "http%3A%2F%2Fadvertiser.com%2Fproduct%2Fclick.html";
        product.imageURL = "http%3A%2F%2Fadvertiser.com%2Fproduct%2Fimage.png";
        return product;
    }

    private RTDatingProduct createDatingProduct() {
        RTDatingProduct datingProduct = new RTDatingProduct();
        // mandatory parameters
        datingProduct.productId = UUID.randomUUID().toString(); // Replace with your productId
        datingProduct.name = "Amazing%20Dating%20Product";
        datingProduct.price = 60.45;
        // optional parameters
        datingProduct.customer = new RTDatingCustomer();
        datingProduct.customer.gender = Gender.Male;
        datingProduct.customer.ageRange = "25-50";
        datingProduct.customer.zipCode = "80637";
        datingProduct.customer.age = 23;
        datingProduct.customer.country = "Germany";
        datingProduct.customer.status = "New";
        datingProduct.customer.wasLoggedIn = true;
        return datingProduct;
    }

    private RTTravelProduct createTravelProduct() {
        RTTravelProduct travelProduct = new RTTravelProduct();
        // mandatory parameters
        travelProduct.productId = UUID.randomUUID().toString(); // Replace with your productId
        travelProduct.name = "Amazing%20Travel%20Product";
        travelProduct.price = 50.45;
        // optional parameters
        travelProduct.departureDate = new Date();
        travelProduct.endDate = new Date();
        travelProduct.productType = "with%20hotel";
        travelProduct.kids = "false";
        travelProduct.adults = "2";
        travelProduct.hotelCategory = "middle";
        travelProduct.pointOfDeparture = "Lisbon";
        travelProduct.pointOfDestination = "Frankfurt";
        travelProduct.customer = createDatingCustomer();
        return travelProduct;
    }

    private RTDatingCustomer createDatingCustomer() {
        RTDatingCustomer customer = new RTDatingCustomer();
        customer.gender = Gender.Male;
        customer.ageRange = "18-25";
        customer.zipCode = "60329";
        customer.wasLoggedIn = false;
        customer.age = 25;
        customer.country = "Germany";
        customer.status = "existing_customer";
        return customer;
    }

    private RTOrder createOrder() {
        RTOrder order = new RTOrder();
        // mandatory parameters
        order.orderId = UUID.randomUUID().toString(); // Replace with your order id
        order.total = 324.45;
        order.items = new ArrayList<>();

        // Retail product
        RTProduct product = createProduct();
        RTOrderItem item1 = new RTOrderItem();
        // mandatory parameters
        item1.quantity = 2;
        item1.product = product;

        // Travel product
        RTTravelProduct travelProduct = createTravelProduct();
        RTOrderItem item2 = new RTOrderItem();
        // mandatory parameters
        item2.quantity = 1;
        item2.product = travelProduct;

        // Dating product
        RTDatingProduct datingProduct = createDatingProduct();
        RTOrderItem item3 = new RTOrderItem();
        // mandatory parameters
        item3.quantity = 1;
        item3.product = datingProduct;

        order.items.add(item1);
        order.items.add(item2);
        order.items.add(item3);

        return order;
    }

    public static void saveToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getFromPrefs(Context context, String key, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}

