package com.example.androidhive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.DialogInterface;
import android.preference.PreferenceManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.telephony.TelephonyManager;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import com.manateeworks.cameraDemo.ActivityCapture;

public class AllProductsActivity extends ListActivity {
    static final int SCAN_BARCODE_REQUEST = 320;
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> LineList;
    ArrayList<HashMap<String, String>> DeliveryList;
    HashMap<String, String> HeaderObject;
    // url to get all products list
    private static String url_get_invoice = "http://88.247.205.145:18600/get_invoice.php";
    private static String url_insert_delivery = "http://88.247.205.145:18600/insert_delivery.php";
    private static String url_get_employees = "http://88.247.205.145:18600/get_employees.php";
    // JSON Node names
    private static final String TAG_Parameter_Company = "Company";
    private static final String TAG_Parameter_Invoiceid = "Invoiceid";
    private static final String TAG_Parameter_Recordid = "Recordid";
    private static final String TAG_Parameter_Amount = "Amount";
    private static final String TAG_Parameter_Person = "Person";
    private static final String TAG_Parameter_Line1Number="Line1Number";
    private static final String TAG_Parameter_DeviceId="DeviceId";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_message = "message";
    private static final String TAG_Header = "Header";
    private static final String TAG_Header_Sube = "Sube";
    private static final String TAG_Header_Documentid = "Documentid";
    private static final String TAG_Header_Unvan = "Unvan";
    private static final String TAG_Header_Durum = "Durum";
    private static final String TAG_Header_Tutar = "Tutar";
    private static final String TAG_Header_Tarih = "Tarih";
    private static final String TAG_Header_Kullanici = "Kullanici";
    private static final String TAG_Header_Bakiye = "Bakiye";
    private static final String TAG_Lines = "Lines";
    private static final String TAG_Line_Recordid = "Recordid";
    private static final String TAG_Line_Depo = "isim";
    private static final String TAG_Line_Kod = "Kod";
    private static final String TAG_Line_isim = "isim";
    private static final String TAG_Line_Miktar = "Miktar";
    private static final String TAG_Line_Birim = "Birim";
    private static final String TAG_Line_Fiyat = "Fiyat";
    private static final String TAG_Line_given = "given";
    private static final String TAG_Line_remain = "remain";
    private static final String TAG_Line_Company = "Company";
    private static final String TAG_Deliveries = "Deliveries";
    private static final String TAG_Delivery_id = "id";
    private static final String TAG_Delivery_RecordTime = "RecordTime";
    private static final String TAG_Delivery_Person = "Person";
    private static final String TAG_Delivery_Amount = "Amount";
    private static final String TAG_Delivery_Birim = "Birim";
    private static final String TAG_Delivery_Kod = "Kod";
    private static final String TAG_Delivery_isim = "isim";
    private String Invoiceid = "";
    private String Company="";
    private String Line1Number="";
    private String DeviceId="";
    private String Person = "Seçim Yapılmadı";
    private Boolean isInfoTaken=false;
    private Boolean isRefresh=false;
    // products JSONArray
    JSONArray Lines = null;
    JSONArray Deliveries = null;
    JSONObject Header = null;
    private enum Switch
    {
        Delivery,Remain;

    }
    private Switch ClickedButton;
    public void GetInfo()
    {
        if (!isInfoTaken) {
            isInfoTaken=true;
            TelephonyManager tMgr = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

            Line1Number = tMgr.getLine1Number()==null ? "SIM kart yok":tMgr.getLine1Number() ;
            DeviceId = tMgr.getDeviceId();
            Log.e("com.rasimyilmaz.invoice","Line1Number: "+Line1Number );
            Log.e("com.rasimyilmaz.invoice","DeviceId: "+DeviceId );
        }
    }
    public boolean CheckConnection(Context context) {
        GetInfo();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }
    public String FindCompany()
    {
        String Company="";
        RadioButton radioButtonAyd = (RadioButton) findViewById(R.id.radioButtonAyd);
        RadioButton radioButtonIzm = (RadioButton) findViewById(R.id.radioButtonIzm);
        if (radioButtonAyd.isChecked()) {
            Log.e("com.rasimyilmaz.invoice", "Aydın");
            Company="Ayd";
        }
        if (radioButtonIzm.isChecked()) {
            Company="Izm";
        }
        return Company;
    }
    public void Ara() {
        Company=FindCompany();
        if (CheckConnection(this.getApplicationContext())) {
            EditText Keyword1 = (EditText) findViewById(R.id.editText);
            Invoiceid = Keyword1.getText().toString();
            Log.e("com.rasimyilmaz.invoice", Invoiceid);
            final String[] params=new String[2];
            params[0]=Invoiceid;
            params[1]=Company;
            try {
                LoadAllProducts a = new LoadAllProducts();
                a.execute(params).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (ExecutionException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (TimeoutException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            }
        }else {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.Hata_Baglanti), Toast.LENGTH_LONG);
            toast.show();
        }
        EditText myEditText = (EditText) findViewById(R.id.editText);
        HideInputArea(myEditText);
    }
    public void Refresh(){
        isRefresh=true;
        if (CheckConnection(this.getApplicationContext()))
        {
            final String[] params=new String[2];
            params[0]=Invoiceid;
            params[1]=Company;
            try {
                LoadAllProducts a = new LoadAllProducts();
                a.execute(params).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (ExecutionException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (TimeoutException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            }
        } else {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.Hata_Baglanti), Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void ShowMessage(final String Text){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(),Text,Toast.LENGTH_LONG).show();
            }
        });
    }
    public void HideInputArea(EditText myEditText){

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }
    public void Input_Delivery(String Company,String Recordid,Double Amount,Integer Position)
    {
        if (CheckConnection(this.getApplicationContext()))
        {
            final String[] params=new String[4];
            params[0]=Company;
            params[1]=Recordid;
            params[2]=DStr(Amount);
            params[3]=String.valueOf(Position);
            try
            {
                InputDelivery a = new InputDelivery();
                a.execute(params).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (ExecutionException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (TimeoutException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            }
        } else {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.Hata_Baglanti), Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void Get_Employees() {
        if (CheckConnection(this.getApplicationContext())) {
            final String[] params = new String[1];
            params[0]=FindCompany();
            try {
                FillEmployeesSpinner a=new FillEmployeesSpinner();
                a.execute(params).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (ExecutionException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            } catch (TimeoutException e) {
                ShowMessage(getResources().getString(R.string.Connection_Drop_Down));
            }
        } else {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.Hata_Baglanti), Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void inputBarcode(View v) {
        Invoiceid = "";
        Intent intent = new Intent(this, ActivityCapture.class);
        startActivityForResult(intent, SCAN_BARCODE_REQUEST);
    }

    public void open_category(View v) {
        Intent intent = new Intent(this, Category.class);
        startActivity(intent);
    }
    public String DStr(Double Value){
        if ((Value % 1) == 0)
        {
            return String.format("%.0f", Value).replace(",",".");
        }
        else
        {
            return String.format("%.2f", Value).replace(",",".");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_products);
        // Hashmap for ListView
        LineList = new ArrayList<HashMap<String, String>>();
        DeliveryList = new ArrayList<HashMap<String, String>>();
        HeaderObject = new HashMap<String, String>();
        // Loading products in Background Thread
        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        final EditText SearcheditText = (EditText) findViewById(R.id.editText);
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                SearcheditText.setText("");
                return true;
            }
        });
        SearcheditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String LayoutId=view.toString().substring(view.toString().indexOf("app:id/")+7);
                LayoutId=LayoutId.substring(0,LayoutId.length()-1);
                String LineListViewId=getResources().getResourceEntryName(R.id.LineListView);
                LineListViewId=LineListViewId.substring(LineListViewId.indexOf("/")+1);
                String DeliveryListViewId=getResources().getResourceEntryName(R.id.DeliveryListView);
                DeliveryListViewId=DeliveryListViewId.substring(DeliveryListViewId.indexOf("/")+1);
                Log.e("com.rasimyilmaz.invoice",LayoutId +" == "+ LineListViewId);
                return true;
            }
        });
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupCompany);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("checkedId",0)!=0
                        && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("checkedId",0)==checkedId)
                {
                    return;
                }
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putInt("checkedId", checkedId).commit();
                Log.e("com.rasimyilmaz.invoice", "Save preferences for Group RadioButtons is set to: " + checkedId + " and radio button Izm id is " + R.id.radioButtonIzm + " Aydin is " + R.id.radioButtonAyd);
                Get_Employees();
            }
        });
        radioGroup.check(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("checkedId", R.id.radioButtonAyd));
        Spinner spinner = (Spinner) findViewById(R.id.spinnerPerson);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString("Employee", parent.getItemAtPosition(position).toString()).commit();
                Log.e("com.rasimyilmaz.invoice", "Selected employee is " + parent.getItemAtPosition(position).toString());
                Person=parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        final Button ToListDeliveries = (Button) findViewById( R.id.buttonDeliveryInfo);
        final Button ToListRemains = (Button) findViewById(R.id.buttonRemainInfo);
        final Button Find = (Button) findViewById(R.id.buttonFind);
        ToListDeliveries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToListDeliveries.setVisibility(View.INVISIBLE);
                ToListRemains.setVisibility(View.VISIBLE);
                ClickedButton= Switch.Delivery;
                Refresh();
            }
        });
        ToListRemains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToListRemains.setVisibility(View.INVISIBLE);
                if (!DeliveryList.isEmpty())
                {
                    ToListDeliveries.setVisibility(View.VISIBLE);
                }
                ClickedButton=Switch.Remain;
                Refresh();
            }
        });
        Find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ara();
            }
        });
        Get_Employees();
    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
        if (requestCode == SCAN_BARCODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                EditText Keyword1 = (EditText) findViewById(R.id.editText);
                Invoiceid = data.getData().toString();
                Keyword1.setText(Invoiceid);
                Ara();
            }
        }
    }

    class LoadAllProducts extends AsyncTask<String, String, String> {
        private String Message;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Fatura yükleniyor. Lütfen Bekleyiniz...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            Message="";
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_Parameter_Invoiceid, args[0]));
            params.add(new BasicNameValuePair(TAG_Parameter_Company, args[1]));
            JSONObject json = jParser.makeHttpRequest(url_get_invoice, "GET", params);
            Log.e("com.rasimyilmaz.invoice", json.toString());
            try
            {
                int success = json.getInt(TAG_SUCCESS);
                Message = json.getString(TAG_message);
                Log.e("com.rasimyilmaz.invoice",Message);
                HeaderObject.clear();
                LineList.clear();
                DeliveryList.clear();
                Header=null;
                Lines=null;
                Deliveries=null;
                Log.e("com.rasimyilmaz.invoice","Success: "+ success);
                if (success == 1) {
                    try {
                        Header = json.getJSONObject(TAG_Header);
                        Log.e("com.rasimyilmaz.invoice", "Invoice header is ready.");
                    } catch (JSONException ex) {
                        Log.e("com.rasimyilmaz.invoice", ex.getMessage());
                    }
                    try {
                        Lines = json.getJSONArray(TAG_Lines);
                        Log.e("com.rasimyilmaz.invoice", "Invoice line count result is : " + Lines.length());
                    } catch (JSONException ex) {
                        Log.e("com.rasimyilmaz.invoice", ex.getMessage());
                    }
                    try {
                        Deliveries = json.getJSONArray(TAG_Deliveries);
                        Log.e("com.rasimyilmaz.invoice", "Number of deliveries is : " + Deliveries.length());
                    } catch (JSONException ex) {
                        Log.e("com.rasimyilmaz.invoice", ex.getMessage());
                    }
                    if (Header != null)
                    {
                        String H_Sube = Header.getString(TAG_Header_Sube);
                        String H_Documentid = Header.getString(TAG_Header_Documentid);
                        String H_Unvan = Header.getString(TAG_Header_Unvan);
                        String H_Durum = Header.getString(TAG_Header_Durum);
                        String H_Tutar = Header.getString(TAG_Header_Tutar);
                        String H_Tarih = Header.getString(TAG_Header_Tarih);
                        String H_Kullanici = Header.getString(TAG_Header_Kullanici);
                        String H_Bakiye = Header.getString(TAG_Header_Bakiye);
                        HeaderObject.put(TAG_Header_Sube, H_Sube);
                        HeaderObject.put(TAG_Header_Documentid, H_Documentid);
                        HeaderObject.put(TAG_Header_Unvan, H_Unvan);
                        HeaderObject.put(TAG_Header_Durum, H_Durum);
                        HeaderObject.put(TAG_Header_Tutar, H_Tutar);
                        HeaderObject.put(TAG_Header_Tarih, H_Tarih);
                        HeaderObject.put(TAG_Header_Kullanici, H_Kullanici);
                        HeaderObject.put(TAG_Header_Bakiye, H_Bakiye);
                    }
                    if (Lines != null) {
                        for (int i = 0; i < Lines.length(); i++)
                        {
                            JSONObject c = Lines.getJSONObject(i);
                            if (c.getDouble(TAG_Line_remain)<=0)
                            {
                                continue;
                            }
                            Log.e("com.rasimyilmaz.invoice", "Adding listview because remain amount" + c.getDouble(TAG_Line_remain));
                            Log.e("com.rasimyilmaz.invoice", "Adding invoice line : " + i);
                            String L_Recordid = c.getString(TAG_Line_Recordid);
                            String L_Depo = c.getString(TAG_Line_Depo);
                            String L_Kod = c.getString(TAG_Line_Kod);
                            if (L_Kod.length()>12){
                                L_Kod=L_Kod.substring(0,10)+"...";
                            }
                            String L_isim = c.getString(TAG_Line_isim);
                            String L_Miktar = DStr(c.getDouble(TAG_Line_Miktar));
                            String L_Birim = c.getString(TAG_Line_Birim);
                            String L_Fiyat = c.getString(TAG_Line_Fiyat);
                            String L_given = DStr(c.getDouble(TAG_Line_given));
                            String L_remain = DStr(c.getDouble(TAG_Line_remain));
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_Line_Recordid, L_Recordid);
                            map.put(TAG_Line_Depo, L_Depo);
                            map.put(TAG_Line_Kod, L_Kod);
                            map.put(TAG_Line_isim, L_isim);
                            map.put(TAG_Line_Miktar, L_Miktar);
                            map.put(TAG_Line_Birim, L_Birim);
                            map.put(TAG_Line_Fiyat, L_Fiyat);
                            map.put(TAG_Line_given, L_given);
                            map.put(TAG_Line_remain, L_remain);
                            map.put(TAG_Line_Company, args[1]);
                            LineList.add(map);
                        }
                    }
                    if (Deliveries != null) {
                        for (int i = 0; i < Deliveries.length(); i++) {
                            JSONObject c = Deliveries.getJSONObject(i);
                            String D_id = c.getString(TAG_Delivery_id);
                            String D_RecordTime = c.getString(TAG_Delivery_RecordTime);
                            String D_Person = c.getString(TAG_Delivery_Person);
                            String D_Amount = DStr(c.getDouble(TAG_Delivery_Amount));
                            String D_Birim = c.getString(TAG_Delivery_Birim);
                            String D_Kod = c.getString(TAG_Delivery_Kod);
                            String D_isim = c.getString(TAG_Delivery_isim);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_Delivery_id, D_id);
                            map.put(TAG_Delivery_RecordTime, D_RecordTime);
                            map.put(TAG_Delivery_Person, D_Person);
                            map.put(TAG_Delivery_Amount, D_Amount);
                            map.put(TAG_Delivery_Birim, D_Birim);
                            map.put(TAG_Delivery_Kod, D_Kod);
                            map.put(TAG_Delivery_isim, D_isim);
                            DeliveryList.add(map);
                        }
                    }
                }
                Log.e("com.rasimyilmaz.invoice", "Getting data completed with no errors.");
            } catch (JSONException e) {
                Log.e("com.rasimyilmaz.invoice", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    if (isRefresh)
                    {
                        isRefresh=false;
                        if (ClickedButton== Switch.Delivery)
                        {
                            UpdateDeliveryList();
                        }
                        else
                        {
                            UpdateLineList();
                        }
                    }
                    else
                    {
                        UpdateHeader();
                        UpdateLineList();
                        Toast.makeText(getApplicationContext(),Message,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private void UpdateHeader(){
        LinearLayout Header = (LinearLayout) findViewById(R.id.Header);
        TextView Sube=(TextView) findViewById(R.id.textView_Header_Sube);
        TextView Documentid = (TextView) findViewById(R.id.textView_Header_Documentid);
        TextView Unvan = (TextView) findViewById(R.id.textView_Header_Unvan);
        TextView Durum = (TextView) findViewById(R.id.textView_Header_Durum);
        TextView Tutar = (TextView) findViewById(R.id.textView_Header_Tutar);
        TextView Tarih = (TextView) findViewById(R.id.textView_Header_Tarih);
        TextView Kullanici = (TextView) findViewById(R.id.textView_Header_Kullanici);
        TextView Bakiye = (TextView) findViewById(R.id.textView_Header_Bakiye);
        Button ToDeliveryList = (Button) findViewById(R.id.buttonDeliveryInfo);
        Button ToRemainList = (Button) findViewById(R.id.buttonRemainInfo);
        if (!HeaderObject.isEmpty())
        {
            Header.setVisibility(View.VISIBLE);
            Sube.setText(HeaderObject.get(TAG_Header_Sube));
            Documentid.setText(HeaderObject.get(TAG_Header_Documentid));
            String Label = HeaderObject.get(TAG_Header_Unvan);
            Unvan.setText(Label.length()>25 ? Label.substring(0,25)+"..." : Label);
            Durum.setText(HeaderObject.get(TAG_Header_Durum));
            Tutar.setText(HeaderObject.get(TAG_Header_Tutar));
            Tarih.setText(HeaderObject.get(TAG_Header_Tarih));
            Kullanici.setText(HeaderObject.get(TAG_Header_Kullanici));
            Bakiye.setText(HeaderObject.get(TAG_Header_Bakiye));
            if(!DeliveryList.isEmpty())
            {
                ToDeliveryList.setVisibility(View.VISIBLE);
                ToRemainList.setVisibility(View.INVISIBLE);
            }
            else
            {
                ToDeliveryList.setVisibility(View.INVISIBLE);
                ToRemainList.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            Header.setVisibility(View.INVISIBLE);
            Sube.setText("");
            Documentid.setText("");
            Unvan.setText("");
            Durum.setText("");
            Tutar.setText("");
            Tarih.setText("");
            Kullanici.setText("");
            Bakiye.setText("");
        }
    }
    private void UpdateDeliveryList(){
        ListAdapter adapter = new SimpleAdapter(
                AllProductsActivity.this, DeliveryList,
                R.layout.list2_item, new String[]{TAG_Delivery_id, TAG_Delivery_Kod, TAG_Delivery_isim,
                TAG_Delivery_Amount, TAG_Delivery_Birim, TAG_Delivery_Person, TAG_Delivery_RecordTime},
                new int[]{R.id.textView_Delivery_id, R.id.textView_Delivery_Kod, R.id.textView_Delivery_isim,
                        R.id.textView_Delivery_Miktar, R.id.textView_Delivery_Birim,
                        R.id.textView_Delivery_Person, R.id.textView_Delivery_RecordTime});
        setListAdapter(adapter);
    }
    private void UpdateLineList(){

        ListAdapter adapter = new SimpleAdapter(
                AllProductsActivity.this, LineList,
                R.layout.list_item, new String[]{
                TAG_Line_Recordid,TAG_Line_Kod,
                TAG_Line_isim, TAG_Line_Miktar,
                TAG_Line_Birim, TAG_Line_Fiyat,
                TAG_Line_given, TAG_Line_remain,
                TAG_Line_Company},
                new int[]{
                        R.id.textView_Line_Recordid,R.id.textView_Line_Kod,
                        R.id.textView_Line_isim, R.id.textView_Line_Miktar,
                        R.id.textView_Line_Birim, R.id.textView_Line_Fiyat,
                        R.id.textView_Line_given, R.id.textView_Line_remain,
                        R.id.textView_Line_Company}) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                TextView text_Konum = (TextView) view.findViewById(R.id.textView_Line_Company);
                TextView text_isim = (TextView) view.findViewById(R.id.textView_Line_isim);
                if (text_Konum.getText().toString().equals("Ayd")) {
                    text_isim.setTextColor(Color.parseColor("#FFFFD43B"));
                } else if (text_Konum.getText().toString().equals("Izm")) {
                    text_isim.setTextColor(Color.WHITE);
                }
                TextView text_recordid=(TextView) view.findViewById(R.id.textView_Line_Recordid);
                final String Recordid= text_recordid.getText().toString();
                TextView text_remain = (TextView) view.findViewById(R.id.textView_Line_remain);
                final Button button=(Button)view.findViewById(R.id.buttonPartialDelivery);
                final double Remain = Double.valueOf(text_remain.getText().toString());
                final Button fullbutton=(Button) view.findViewById(R.id.buttonDelivery);

                if (Remain ==1 || (Remain % 1 !=0))
                {
                    button.setVisibility(View.INVISIBLE);
                }else
                {
                    button.setVisibility(View.VISIBLE);
                }
                final int Position=position;
                final String Company= ((TextView) view.findViewById(R.id.textView_Line_Company)).getText().toString();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialog(Company,Recordid,Remain,Position);
                    }
                });
                fullbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Input_Delivery(Company,Recordid,Remain,Position);
                    }
                });
                return view;
            }
        };
        setListAdapter(adapter);
    }

    public void ShowAlertDialog(final String Company,final String Recordid, final Double Remain,final int Position)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AllProductsActivity.this);
        alertDialog.setTitle("Teslimat");
        alertDialog.setMessage("Verilen miktar nedir ?");
        final EditText input = new EditText(AllProductsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Tamam",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        double Amount=Double.valueOf(input.getText().toString());
                        if(Amount>Remain)
                        {
                            Toast.makeText(AllProductsActivity.this,"Girilen miktar kalan miktardan fazla olmamalıydı.", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Input_Delivery(Company,Recordid,Amount,Position);
                        }
                        HideInputArea(input);
                    }
                });

        alertDialog.setNegativeButton("İptal",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        HideInputArea(input);
                    }
                });

        alertDialog.show();
    }

    class InputDelivery extends AsyncTask<String, String, String> {
        private String Message;
        private int Position;
        private double Amount=0;
        private double remain=0;
        private boolean Succeed=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (Person=="Seçim Yapılmadı")
            {
                Spinner spinner = (Spinner)findViewById(R.id.spinnerPerson);
                Person=spinner.getSelectedItem().toString();
            }
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Ürün teslim ediliyor. Lütfen Bekleyiniz...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_Parameter_Company, args[0]));
            params.add(new BasicNameValuePair(TAG_Parameter_Invoiceid, Invoiceid));
            params.add(new BasicNameValuePair(TAG_Parameter_Recordid, args[1]));
            params.add(new BasicNameValuePair(TAG_Parameter_Amount, args[2]));
            Amount=Double.valueOf(args[2]);
            params.add(new BasicNameValuePair(TAG_Parameter_Person, Person));
            params.add(new BasicNameValuePair(TAG_Parameter_Line1Number, Line1Number));
            params.add(new BasicNameValuePair(TAG_Parameter_DeviceId,DeviceId));
            Position=Integer.valueOf(args[3]);
            Log.e("com.rasimyilmaz.invoice", "Line id: " + params.toString());
            JSONObject json2 = jParser.makeHttpRequest(url_insert_delivery, "GET", params);
            Log.e("com.rasimyilmaz.invoice","Delivery insert response: "+ json2.toString() );
            Message="";
            try
            {
                int success = json2.getInt(TAG_SUCCESS);
                Log.e("com.rasimyilmaz.invoice",getListAdapter().getItem(Position).toString());
                if (success==0)
                {
                    Message = json2.getString(TAG_message);
                    Succeed=false;
                }
                else
                {
                    Position=Integer.valueOf(args[3]);
                    Succeed=true;
                }
            }
            catch (Exception ex)
            {
                Message=ex.getMessage();
                Log.e("com.rasimyilmaz.invoice", ex.getMessage());
            }
            return Message;
        }
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run()
                {

                    if (Succeed)
                    {
                        remain=Double.valueOf(LineList.get(Position).get(TAG_Line_remain));
                        if (remain-Amount>0)
                        {
                            HashMap<String,String> replace=new HashMap<String, String>();
                            replace=LineList.get(Position);
                            replace.remove(TAG_Line_remain);
                            replace.put(TAG_Line_remain,DStr(remain-Amount));
                            LineList.set(Position,replace);
                        }
                        else
                        {
                            LineList.remove(Position);
                        }
                        UpdateLineList();
                        final Button ToDeliveryList =  (Button) findViewById(R.id.buttonDeliveryInfo);
                        if (ToDeliveryList.getVisibility()== View.INVISIBLE)
                        {
                            ToDeliveryList.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        Toast.makeText(AllProductsActivity.this, Message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    class FillEmployeesSpinner extends AsyncTask<String, String, String> {
        private boolean Succeed;
        private String Message;
        private ArrayList<String> EmployeesList = new ArrayList<String>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Personel listesi alınıyor. Lütfen Bekleyiniz...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_Parameter_Company, args[0]));
            JSONObject json2 = jParser.makeHttpRequest(url_get_employees, "GET", params);
            Log.e("com.rasimyilmaz.invoice","Delivery insert response: "+ json2.toString() );
            try
            {
                int success = json2.getInt(TAG_SUCCESS);
                if (success==0)
                {
                    Message = json2.getString(TAG_message);
                    Succeed=false;
                }
                else
                {
                    Succeed=true;
                    JSONArray Employees =json2.getJSONArray("Employees");
                    if (Employees != null) {
                        for (int i = 0; i < Employees.length(); i++)
                        {
                            EmployeesList.add(Employees.getJSONObject(i).getString("name"));
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Message=ex.getMessage();
                Log.e("com.rasimyilmaz.invoice", ex.getMessage());
            }
            return Message;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run()
                {
                    if (Succeed)
                    {
                        String Employee = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("Employee","");
                        Spinner spinner = (Spinner) findViewById(R.id.spinnerPerson);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AllProductsActivity.this,   android.R.layout.simple_spinner_item,EmployeesList );
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);
                        int position=spinnerArrayAdapter.getPosition(Employee);
                        if (position>0)
                        {
                            spinner.setSelection(position);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}