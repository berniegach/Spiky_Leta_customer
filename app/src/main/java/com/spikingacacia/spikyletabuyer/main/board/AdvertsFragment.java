package com.spikingacacia.spikyletabuyer.main.board;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.spikingacacia.spikyletabuyer.database.Adverts;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class AdvertsFragment extends Fragment
{

    private String url_add_advert= base_url+"add_advert.php";
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private  RecyclerView recyclerView;
    private AdvertsRVA my_advertsRVA;
    private String TAG = "AdvertsF";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdvertsFragment()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdvertsFragment newInstance(int columnCount)
    {
        AdvertsFragment fragment = new AdvertsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_adverts_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
            //linearLayoutManager.setReverseLayout(true);
            //linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
             my_advertsRVA =new AdvertsRVA( mListener, getContext());
            recyclerView.setAdapter(my_advertsRVA);
        }
        return view;
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener)
        {
            mListener = (OnListFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        new AdsTask().execute((Void)null);
    }

    public interface OnListFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onAdClicked(Adverts item);
    }
    private class AdsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_ads= base_url+"get_ads.php";
        private JSONParser jsonParser;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private List<Adverts> list;

        public AdsTask()
        {
            jsonParser = new JSONParser();
            list = new ArrayList<>();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_id",""));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_ads,"POST",info);
            Log.d(TAG,""+jsonObject.toString());
            try
            {
                JSONArray array=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    array=jsonObject.getJSONArray("ads");
                    for(int count=0; count<array.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=array.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        String image_type=jsonObjectNotis.getString("image_type");
                        int seller_id=jsonObjectNotis.getInt("seller_id");
                        String seller_name=jsonObjectNotis.getString("seller_name");
                        String seller_image_type=jsonObjectNotis.getString("seller_image_type");
                        String title=jsonObjectNotis.getString("title");
                        String content=jsonObjectNotis.getString("content");
                        int views=jsonObjectNotis.getInt("ad_views");
                        int likes=jsonObjectNotis.getInt("ad_likes");
                        int comments=jsonObjectNotis.getInt("ad_comments");
                        String date=jsonObjectNotis.getString("date_added");

                        Adverts adverts = new Adverts(id,image_type, seller_image_type,seller_id,seller_name,title,content,views,likes,comments,date);
                        list.add(adverts);

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
        protected void onPostExecute(final Boolean successful)
        {

            if (successful)
            {
                Log.d(TAG,"siccesds");
                my_advertsRVA.listUpdated(list);
            }
            else
            {

            }
        }

    }

}
