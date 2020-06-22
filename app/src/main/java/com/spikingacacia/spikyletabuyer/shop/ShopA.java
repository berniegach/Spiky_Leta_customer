package com.spikingacacia.spikyletabuyer.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.Categories;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.database.SCategories;
import com.spikingacacia.spikyletabuyer.database.SGroups;
import com.spikingacacia.spikyletabuyer.database.SItems;
import com.spikingacacia.spikyletabuyer.shop.cart.CartContent;
import com.spikingacacia.spikyletabuyer.shop.cart.CartFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;

public class ShopA extends AppCompatActivity
        implements
        BSOrderFragment.OnFragmentInteractionListener,
        menuFragment.OnListFragmentInteractionListener,
        CartFragment.OnListFragmentInteractionListener
{
    public static LinkedHashMap<Integer, Categories> categoriesLinkedHashMap;
    public static LinkedHashMap<Integer, DMenu> menuLinkedHashMap;
    private String TAG="ShopA";
    private JSONParser jsonParser;
    public static String sellerEmail;
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
    public static LinkedHashMap<Integer,Integer> cartLinkedHashMap;
    public static LinkedHashMap<Integer,Integer> tempCartLinkedHashMap;
    public static LinkedHashMap<Integer,Integer> itemPriceSizeLinkedHashMap;
    public static Double tempTotal =0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_shop);
        //preference
        preferences=new Preferences(getBaseContext());
        sellerEmail =getIntent().getStringExtra("seller_email");
        sellerOrderRadius=getIntent().getIntExtra("order_radius",2);
        buyerDistance=getIntent().getDoubleExtra("buyer_distance",0);
        numberOfTables=getIntent().getIntExtra("number_of_tables",10);
        setTitle("Menu");

        Fragment fragment= menuFragment.newInstance();
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"menu");
        transaction.commit();
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*if (items.size()==0)
                {
                    Snackbar.make(fab,"Cart is empty",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                whichFragment=4;
                String title="cart";
                getSupportActionBar().setTitle(title);
                //Fragment fragment=BSOrderFragment.newInstance("","");
                Fragment fragment=CartFragment.newInstance(1);
                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.base,fragment,title);
                transaction.addToBackStack(title);
                transaction.commit();
                fab.hide();*/
                cartClicked();
            }
        });
        jsonParser=new JSONParser();
        categoriesLinkedHashMap = new LinkedHashMap<>();
        menuLinkedHashMap = new LinkedHashMap<>();
        items=new ArrayList<>();
        names=new ArrayList<>();
        price=new ArrayList<>();
        cartCount=0;
        totalPrice=0;
        cartLinkedHashMap = new LinkedHashMap<>();
        tempCartLinkedHashMap = new LinkedHashMap<>();
        itemPriceSizeLinkedHashMap = new LinkedHashMap<>();
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (items.size()>0)
        {
            if(!fab.isShown())
            {
                fab.show();
                fab.setText(String.valueOf(cartCount));
            }
        }
        /*if(whichFragment==2)
            getSupportActionBar().setTitle("Order");
        else
        {
            getSupportActionBar().setTitle(previousTitle[whichFragment-3]);
            whichFragment-=1;

        }*/

    }
    void cartClicked()
    {
        tempCartLinkedHashMap=cartLinkedHashMap;
        getTotal();
        Fragment fragment= CartFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"cart");
        transaction.addToBackStack("cart");
        transaction.commit();
    }


    /**
     * implementation of BSOrderFragment.java*/
    @Override
    public void onOrderItems()
    {

    }

    @Override
    public void onMenuItemInteraction(final DMenu item)
    {
        /*items.add(item.getId());
        names.add(item.getItem());
        price.add(item.getSellingPrice());
        String name=item.getItem();
        cartCount+=1;
        totalPrice+=item.getSellingPrice();
        if(!fab.isShown())
            fab.show();
        fab.setText(String.valueOf(cartCount));
        Snackbar.make(fab,name+" added.\nTotal: "+totalPrice,Snackbar.LENGTH_LONG)
                .setAction("Remove", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        cartCount-=1;
                        totalPrice-=item.getSellingPrice();
                        items.remove(items.size()-1);
                        names.remove(names.size()-1);
                        price.remove(price.size()-1);
                        fab.setText(String.valueOf(cartCount));
                    }
                }).show();*/
        cartLinkedHashMap.put(item.getId(),1);
        itemPriceSizeLinkedHashMap.put(item.getId(), 0);
        if(getCartCount()>0)
            fab.setVisibility(View.VISIBLE);
        fab.setText(Integer.toString(getCartCount()));
    }
    /*************************************************************************************************************************************************************************************
     * implementation of CartFragment.java
     * ************************************************************************************************************************************************************************************/
    @Override
    public void onListFragmentInteraction(CartContent.CartItem item)
    {

    }


    @Override
    public void onProceed()
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

    @Override
    public void totalItemsChanged()
    {
        fab.setText(Integer.toString(getCartCount()));
        if(getCartCount()>0)
            fab.setVisibility(View.VISIBLE);
        else
            fab.setVisibility(View.GONE);
    }
    private int getCartCount()
    {
        int total_count=0;
        Iterator iterator = cartLinkedHashMap.entrySet().iterator();
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, Integer>set = (LinkedHashMap.Entry<Integer, Integer>) iterator.next();
            int count = set.getValue();
            total_count += count;
        }
        return total_count;
    }
    private void getTotal()
    {
        Iterator iterator= tempCartLinkedHashMap.entrySet().iterator();
        Double total=0.0;
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, Integer>set = (LinkedHashMap.Entry<Integer, Integer>) iterator.next();
            int id=set.getKey();
            int count=set.getValue();
            DMenu inv = menuLinkedHashMap.get(id);

           // int pos = itemPriceSizeLinkedHashMap.get(id);
            //String priceString = inv.getPrices();
           // final String[] prices = priceString.split(":");
            Double price = inv.getSellingPrice();//Double.parseDouble( prices[pos].contentEquals("null")?"0":prices[pos]);

            total += count*price;

        }
        tempTotal = total;
    }

    private class OrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_place_order = base_url + "place_order.php";
        private String TAG_MESSAGE = "message";
        private String TAG_SUCCESS = "success";
        private String itemsIds="";
        private int tableNumber=0;


        public OrderTask(int tableNumber)
        {
            this.tableNumber=tableNumber;
            formData();
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
            info.add(new BasicNameValuePair("seller_email",sellerEmail));
            info.add(new BasicNameValuePair("user_email",serverAccount.getEmail()));
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
                //cartCount=0;
                //totalPrice=0;
                //items.clear();
                //names.clear();
                //price.clear();
                //////
                cartLinkedHashMap.clear();;
                tempCartLinkedHashMap.clear();
                tempTotal = 0.0;
                fab.setText(Integer.toString(getCartCount()));
                fab.setVisibility(View.GONE);
                onBackPressed();
            }
            else
            {
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
        void  formData()
        {
            Iterator iterator= ShopA.tempCartLinkedHashMap.entrySet().iterator();
            while(iterator.hasNext())
            {
                LinkedHashMap.Entry<Integer, Integer>set = (LinkedHashMap.Entry<Integer, Integer>) iterator.next();
                int id=set.getKey();
                DMenu inv = menuLinkedHashMap.get(id);
                int quantity = set.getValue();

                //int pos = StoreActivity.itemPriceSizeLinkedHashMap.get(id);
                //String priceString = inv.getPrices();
                //final String[] prices = priceString.split(":");
                //String[] sizes = inv.getSizes().split(":");
                //String  price =  prices[pos].contentEquals("null")?"0":prices[pos];
                //String size = sizes[pos];
                for(int c=0; c<quantity; c++)
                {
                    if (!itemsIds.contentEquals(""))
                    {
                        itemsIds += ",";
                        //itemPrices += ",";
                        //itemSizes += ",";
                    }
                    itemsIds += String.valueOf(inv.getId());
                    //itemPrices += price;
                    //itemSizes += size;
                }
            }

        }
    }
}
