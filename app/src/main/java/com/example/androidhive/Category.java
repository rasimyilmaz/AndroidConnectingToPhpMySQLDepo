package com.example.androidhive;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.manateeworks.cameraDemo.ActivityCapture;
import com.manateeworks.cameraDemo.ActivityCapture2;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class Category extends Activity
{

    List<HashMap<String, String>> _Categories_List=new ArrayList<HashMap<String, String>>();
    private String Barcode="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
       //         .permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        final CheckBox ck1=(CheckBox)findViewById(R.id.checkBoxAyd2);
        ck1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FillSpinner();
            }
        });
        final CheckBox ck2=(CheckBox)findViewById(R.id.checkBoxIzm2);
        ck2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FillSpinner();
            }
        });
        Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                String selectedItem = parent.getItemAtPosition(pos).toString();
                SelectedCategoryCode=selectedItem.substring(selectedItem.lastIndexOf("=")+1,selectedItem.lastIndexOf("}"));
                SelectedCategoryName=selectedItem.substring(selectedItem.indexOf("=")+1,selectedItem.indexOf(","));
                error=false;
                for (int i=0 ;i<_Categories_List.size();i++){

                if(_Categories_List.get(i).get("ktg_kod").toString().startsWith(SelectedCategoryCode + ".") ){
                error=true;
                    Log.d("a",(_Categories_List.get(i).get("ktg_kod").toString()));
                }}
                if(error){
                    showToastFromBackground("Seçilen Kategorinin Alt Kategorisi var.");
                }else
                {
                    showToastFromBackground(SelectedCategoryName+ " seçildi.");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    String SelectedCategoryCode="";
    String SelectedCategoryName="";
    boolean error;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }
    public void Scan(View v){
        if(CheckConnection(this.getApplicationContext())) {
            Intent intent=new Intent(this, ActivityCapture2.class);
            CheckBox Ayd=(CheckBox)findViewById(R.id.checkBoxAyd2);
            CheckBox Izm=(CheckBox)findViewById(R.id.checkBoxIzm2);

            if (Ayd.isChecked() | Izm.isChecked()) {
                if (!error) {
                    Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
                    intent.putExtra("Category", SelectedCategoryCode);
                    intent.putExtra("Ayd", (Ayd.isChecked()) ? "1" : "0");
                    intent.putExtra("Izm", (Izm.isChecked()) ? "1" : "0");
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(this, getString(R.string.Hata_Kategori), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else
                {Toast toast=Toast.makeText(this,getString(R.string.Hata_MagazaSec),Toast.LENGTH_LONG);
                toast.show();}
        }
        else{
            Toast toast=Toast.makeText(this,getString(R.string.Hata_Baglanti),Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public boolean CheckConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }
    public void FillSpinner(){
        Log.d("Step","fill spinner");
        new Thread(new Runnable() {
            public void run()
            {
                Log.d("Step","1");
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                CheckBox checkboxAyd=((CheckBox)findViewById(R.id.checkBoxAyd2));
                CheckBox checkboxIzm=(CheckBox)findViewById(R.id.checkBoxIzm2);
                if (checkboxAyd.isChecked()|checkboxIzm.isChecked())
                {
                    Log.d("Step","2");
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("Ayd", checkboxAyd.isChecked() ? "1" : "0"));
                    params.add(new BasicNameValuePair("Izm", checkboxIzm.isChecked() ? "1" : "0"));
                    int success = 0;
                    JSONParser jParser = new JSONParser();
                    List<HashMap<String, String>> Categories_List=new ArrayList<HashMap<String, String>>();
                    try
                    {

                        Log.d("Step","3");
                        Log.d("0",params.get(0).getName()+"="+params.get(0).getValue());
                        JSONObject json = jParser.makeHttpRequest("http://88.247.205.145:18600/list_category.php", "GET", params);
                        success = json.getInt("success");
                        Log.d("success",String.valueOf(success));
                        if (success >0)
                        {
                            JSONArray Categories = null;
                            Categories = json.getJSONArray("Categories");
                            for (int i = 0; i < Categories.length(); i++)
                            {
                                JSONObject c = Categories.getJSONObject(i);
                                Log.d("Categories", c.getString("ktg_kod"));
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("ktg_kod",c.getString("ktg_kod"));
                                map.put("ktg_isim", c.getString("ktg_isim"));
                                Categories_List.add(map);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        showToastFromBackground(getString(R.string.Hata_Server));
                        Log.d("e", e.toString());
                    }
                    if (success > 0)
                    {
                        SpinnerAdapter adapter = new SimpleAdapter(
                                Category.this,Categories_List,
                                R.layout.spinner_item, new String[]{"ktg_kod","ktg_isim"},
                                new int[]{R.id.ktg_kod,R.id.ktg_isim});
                            updateSpinnerBackground(adapter);
                            _Categories_List=Categories_List;
                    }else
                    {
                        updateSpinnerBackground(null);
                    }
                }
                else
                {
                    updateSpinnerBackground(null);
                }
            }
        }
        ).start();
    }
    public void showToastFromBackground(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Category.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    public void updateSpinnerBackground(final SpinnerAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
                spinner.setAdapter(adapter);

            }
        });
    }
    public class CategoryItem{
        private String _id;
        private String _name;

        public CategoryItem(){
            this._id = "";
            this._name = "";
        }

        public void setId(String id){
            this._id = id;
        }

        public String getId(){
            return this._id;
        }

        public void setName(String name){
            this._name = name;
        }

        public String getName(){
            return this._name;
        }
    }
}