package com.spikingacacia.spikyletabuyer.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.SCategories;
import com.spikingacacia.spikyletabuyer.database.SGroups;
import com.spikingacacia.spikyletabuyer.database.SItems;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class ShopA extends AppCompatActivity
        implements BSCategoryF.OnListFragmentInteractionListener,
        BSGroupF.OnListFragmentInteractionListener,
        BSItemF.OnListFragmentInteractionListener,
        BSOrderFragment.OnFragmentInteractionListener
{
    private String url_get_s_categories=base_url+"get_seller_categories.php";
    private String url_get_s_groups=base_url+"get_seller_groups.php";
    private String url_get_s_items=base_url+"get_seller_items.php";
    private String url_place_order=base_url+"place_order.php";
    public static LinkedHashMap<Integer, SCategories> bCategoriesList;
    public static LinkedHashMap<Integer, SGroups> bGroupsList;
    public static LinkedHashMap<Integer, SItems> bItemsList;
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String TAG="ShopA";
    private JSONParser jsonParser;
    public static int sellerId;
    private int sellerOrderRadius=2;
    private double buyerDistance=0;
    private int numberOfTables=10;
    private int backgroundTasksProgress=0;
    private final int finalProgressCount=3;
    int whichFragment=1;
    String previousTitle[]=new String[2];
    public static int cartCount=0;
    public static  double totalPrice=0;
    private  ExtendedFloatingActionButton fab;
    public static List<Integer>items;
    public static List<String>names;
    public static List<Double>price;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_shop);
        //preference
        preferences=new Preferences(getBaseContext());
        sellerId=getIntent().getIntExtra("seller_id",0);
        sellerOrderRadius=getIntent().getIntExtra("order_radius",2);
        buyerDistance=getIntent().getDoubleExtra("buyer_distance",0);
        numberOfTables=getIntent().getIntExtra("number_of_tables",10);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order");
        if(!preferences.isDark_theme_enabled())
        {
            setTheme(R.style.AppThemeLight_NoActionBarLight);
            toolbar.setTitleTextColor(getResources().getColor(R.color.text_light));
            toolbar.setPopupTheme(R.style.AppThemeLight_PopupOverlayLight);
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
            appBarLayout.getContext().setTheme(R.style.AppThemeLight_AppBarOverlayLight);
            appBarLayout.setBackgroundColor(getResources().getColor(R.color.main_background_light));
            findViewById(R.id.main).setBackgroundColor(getResources().getColor(R.color.main_background_light));
        }
        /*Fragment fragment= SRRestaurantsF.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"restaurants");
        transaction.commit();*/
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (items.size()==0)
                {
                    Snackbar.make(fab,"Cart is empty",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                whichFragment=4;
                String title="cart";
                getSupportActionBar().setTitle(title);
                Fragment fragment=BSOrderFragment.newInstance("","");
                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.base,fragment,title);
                transaction.addToBackStack(title);
                transaction.commit();
                fab.hide();
            }
        });
        jsonParser=new JSONParser();
        bCategoriesList=new LinkedHashMap<>();
        bGroupsList=new LinkedHashMap<>();
        bItemsList=new LinkedHashMap<>();
        items=new ArrayList<>();
        names=new ArrayList<>();
        price=new ArrayList<>();
        cartCount=0;
        totalPrice=0;
        //start the tasks
        new SCategoriesTask().execute((Void)null);
        new SGroupsTask().execute((Void)null);
        new SItemsTask().execute((Void)null);
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(whichFragment==2)
            getSupportActionBar().setTitle("Order");
        else
        {
            getSupportActionBar().setTitle(previousTitle[whichFragment-3]);
            whichFragment-=1;
            if(!fab.isShown())
            {
                fab.show();
                fab.setText(String.valueOf(cartCount));
            }
        }

    }
    /**
     * implementation of SICategoryF.java*/
    @Override
    public void onItemClicked(BSCategoryC.CategoryItem item)
    {
        whichFragment=2;
        String title=item.category;
        title=title.replace("_"," ");
        getSupportActionBar().setTitle(title);
        previousTitle[0]=title;
        Fragment fragment=BSGroupF.newInstance(item.id);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,title);
        transaction.addToBackStack(title);
        transaction.commit();
    }
    /**
     * implementation of SIGroupF.java*/
    @Override
    public void onItemClicked(BSGroupC.GroupItem item)
    {
        whichFragment=3;
        String title=item.group;
        title=title.replace("_"," ");
        getSupportActionBar().setTitle(title);
        previousTitle[1]=title;
        Fragment fragment=BSItemF.newInstance(item.category,item.id);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,title);
        transaction.addToBackStack(title);
        transaction.commit();
    }
    @Override
    /**
     * implementation of BSITEMF.java*/
    public void onItemClicked(final BSItemC.InventoryItem item)
    {
        items.add(item.id);
        names.add(item.item);
        price.add(item.sellingPrice);
        String name=item.item;
        name=name.replace("_"," ");
        cartCount+=1;
        totalPrice+=item.sellingPrice;
        fab.setText(String.valueOf(cartCount));
        Snackbar.make(fab,name+" added.\nTotal: "+totalPrice,Snackbar.LENGTH_LONG)
                .setAction("Remove", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        cartCount-=1;
                        totalPrice-=item.sellingPrice;
                        items.remove(items.size()-1);
                        names.remove(names.size()-1);
                        price.remove(price.size()-1);
                        fab.setText(String.valueOf(cartCount));
                    }
                }).show();
    }
    /**
     * implementation of BSOrderFragment.java*/
    @Override
    public void onOrderItems()
    {
        //first check if the the location is within the restaurants range
        if(buyerDistance>sellerOrderRadius)
        {
            Snackbar.make(fab,"You are not within the restaurant's order radius",Snackbar.LENGTH_LONG).show();
            return;
        }
        Log.d("distance:",buyerDistance+":"+sellerOrderRadius);
        final NumberPicker numberPicker=new NumberPicker(getBaseContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(numberOfTables);
        new androidx.appcompat.app.AlertDialog.Builder(ShopA.this)
                .setTitle("Table Number?")
                .setView(numberPicker)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int tableNumber=numberPicker.getValue();
                        new OrderTask(tableNumber).execute((Void)null);
                    }
                })
               .create().show();
    }
    /**
     * Following code will get the sellers categories
     * The returned infos are id,  categories, descriptions, dateadded, datechanged.
     * Arguments are:
     * id==boss id.
     * Returns are:
     * success==1 successful get
     * success==0 for id argument missing
     **/
    private class SCategoriesTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("SCATEGORIES: ","starting....");
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("id",Integer.toString(sellerId)));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_categories,"POST",info);
            Log.d("sCategories",""+jsonObject.toString());
            try
            {
                JSONArray categoriesArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    categoriesArrayList=jsonObject.getJSONArray("categories");
                    for(int count=0; count<categoriesArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=categoriesArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        String category=jsonObjectNotis.getString("category");
                        String description=jsonObjectNotis.getString("description");
                        String dateadded=jsonObjectNotis.getString("dateadded");
                        String datechanged=jsonObjectNotis.getString("datechanged");
                        SCategories sCategories=new SCategories(id,category,description,dateadded,datechanged);
                        bCategoriesList.put(id,sCategories);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {
            backgroundTasksProgress+=1;
            if(backgroundTasksProgress==finalProgressCount)
            {

            }

            if (successful)
            {
                Fragment fragment=BSCategoryF.newInstance(1);
                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.base,fragment,"Categories");
                transaction.commit();
            }
            else
            {

            }
        }
    }
    /**
     * Following code will get the sellers groups
     * The returned infos are id,  categories, groups, descriptions, dateadded, datechanged.
     * Arguments are:
     * id==boss id.
     * Returns are:
     * success==1 successful get
     * success==0 for id argument missing
     **/
    private class SGroupsTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("SGROUPS: ","starting....");
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("id",Integer.toString(sellerId)));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_groups,"POST",info);
            Log.d("sGroups",""+jsonObject.toString());
            try
            {
                JSONArray groupsArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    groupsArrayList=jsonObject.getJSONArray("groups");
                    for(int count=0; count<groupsArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=groupsArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        int category=jsonObjectNotis.getInt("category");
                        String group=jsonObjectNotis.getString("group");
                        String description=jsonObjectNotis.getString("description");
                        String dateadded=jsonObjectNotis.getString("dateadded");
                        String datechanged=jsonObjectNotis.getString("datechanged");
                        SGroups sGroups=new SGroups(id,category,group,description,dateadded,datechanged);
                        bGroupsList.put(id,sGroups);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {
            backgroundTasksProgress+=1;
            if(backgroundTasksProgress==finalProgressCount)
            {

            }

            if (successful)
            {

            }
            else
            {

            }
        }
    }
    /**
     * Following code will get the sellers groups
     * The returned infos are id,  categories, groups, items, descriptions, sellingprice, available, dateadded, datechanged.
     * Arguments are:
     * id==boss id.
     * Returns are:
     * success==1 successful get
     * success==0 for id argument missing
     **/
    private class SItemsTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("SITEMS: ","starting....");
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("id",Integer.toString(sellerId)));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_items,"POST",info);
            Log.d("sItems",""+jsonObject.toString());
            try
            {
                JSONArray itemsArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    itemsArrayList=jsonObject.getJSONArray("items");
                    for(int count=0; count<itemsArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=itemsArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        int category=jsonObjectNotis.getInt("category");
                        int group=jsonObjectNotis.getInt("group");
                        String item=jsonObjectNotis.getString("item");
                        String description=jsonObjectNotis.getString("description");
                        double selling_price=jsonObjectNotis.getDouble("sellingprice");
                        int available=jsonObjectNotis.getInt("available");
                        String dateadded=jsonObjectNotis.getString("dateadded");
                        String datechanged=jsonObjectNotis.getString("datechanged");
                        SItems sItems=new SItems(id,category,group,item,description,selling_price,available,dateadded,datechanged);
                        bItemsList.put(id,sItems);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {
            backgroundTasksProgress+=1;
            if(backgroundTasksProgress==finalProgressCount)
            {

            }

            if (successful)
            {

            }
            else
            {

            }
        }
    }
    private class OrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String itemsIds="";
        private int tableNumber=0;
        public OrderTask(int tableNumber)
        {
            this.tableNumber=tableNumber;
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();


            Log.d("ORDERING","starting....");
            for(int c=0; c<items.size(); c++)
            {
                itemsIds+=String.valueOf(items.get(c));
                if(c!=items.size()-1)
                    itemsIds+=",";
            }
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_id",Integer.toString(sellerId)));
            info.add(new BasicNameValuePair("buyer_id",Integer.toString(LoginA.serverAccount.getId())));
            info.add(new BasicNameValuePair("items_ids",itemsIds));
            info.add(new BasicNameValuePair("table_number",String.valueOf(tableNumber)));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_place_order,"POST",info);
            Log.d("sItems",""+jsonObject.toString());
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {

            if (successful)
            {
                Snackbar.make(fab,"Order Placed",Snackbar.LENGTH_LONG).show();
                cartCount=0;
                totalPrice=0;
                items.clear();
                names.clear();
                price.clear();
                onBackPressed();
                fab.setText(String.valueOf(cartCount));
            }
            else
            {
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
