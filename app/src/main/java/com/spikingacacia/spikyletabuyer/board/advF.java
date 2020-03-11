package com.spikingacacia.spikyletabuyer.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.spikingacacia.spikyletabuyer.board.AdsC.AdItem;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class advF extends Fragment
{

    private String url_add_advert= base_url+"add_advert.php";
    private String url_get_ads= base_url+"get_ads.php";
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private  RecyclerView recyclerView;
    private JSONParser jsonParser;
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private static int static_last_id=0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public advF()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static advF newInstance(int columnCount)
    {
        advF fragment = new advF();
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
        jsonParser=new JSONParser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.f_adv_list, container, false);

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
            final advRVA my_advRVA=new advRVA(AdsC.ITEMS, mListener, getContext());
            my_advRVA.clearData();
            recyclerView.setAdapter(my_advRVA);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
                {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (!recyclerView.canScrollVertically(1)) {
                        if(static_last_id==1)
                            return;
                        new AdsTask(static_last_id-1, my_advRVA).execute((Void)null);
                    }
                }
            });
            new AdsTask(0, my_advRVA).execute((Void)null);
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


    public interface OnListFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onAdClicked(AdItem item);
    }
    private class AdsTask extends AsyncTask<Void, Void, Boolean>
    {
        final int last_id;
        final advRVA my_advra;

        @Override
        protected void onPreExecute()
        {
            Log.d("SCATEGORIES: ","starting....");
            super.onPreExecute();
        }
        public AdsTask(int last_id, advRVA advRVA)
        {
            this.last_id=last_id;
            my_advra=advRVA;
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_id",Integer.toString(0)));
            info.add(new BasicNameValuePair("last_id",Integer.toString(last_id)));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_ads,"POST",info);
            // Log.d("",""+jsonObject.toString());
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
                        if(count==0)
                            static_last_id=id;
                        int seller_id=jsonObjectNotis.getInt("seller_id");
                        String seller_name=jsonObjectNotis.getString("seller_name");
                        String title=jsonObjectNotis.getString("title");
                        String content=jsonObjectNotis.getString("content");
                        int views=jsonObjectNotis.getInt("ad_views");
                        int likes=jsonObjectNotis.getInt("ad_likes");
                        int comments=jsonObjectNotis.getInt("ad_comments");
                        String date=jsonObjectNotis.getString("date_added");

                        find_ad_image(id, seller_name,seller_id,title, content, views, likes, comments, date);

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

            }
            else
            {

            }
        }
        private void find_ad_image(final int id, final String seller_name, final int seller_id, final String title, final String content, final int views, final int likes, final int comments, final String date)
        {
            String url= LoginA.base_url+"src/ads/"+String.format("%d",id)+".jpg";
            ImageRequest request=new ImageRequest(
                    url,
                    new Response.Listener<Bitmap>()
                    {
                        @Override
                        public void onResponse(Bitmap response)
                        {

                            find_seller_image( id, response, seller_name, seller_id,  title, content, views,  likes,  comments, date);
                            //profilePic=response;
                            //imageView.setImageBitmap(response);
                            Log.d("volley","succesful");
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError e)
                        {
                            find_seller_image( id, null, seller_name, seller_id,  title, content, views,  likes,  comments, date);
                            Log.e("voley",""+e.getMessage()+e.toString());
                        }
                    });
            RequestQueue request2 = Volley.newRequestQueue(getContext());
            request2.add(request);
        }
        private void find_seller_image(final int id, final Bitmap bitmap, final String seller_name, int seller_id, final String title, final String content, final int views, final int likes, final int comments, final String date)
        {
            String url= LoginA.base_url+"src/sellers/"+String.format("%s/pics/prof_pic",makeName(seller_id))+".jpg";
            ImageRequest request=new ImageRequest(
                    url,
                    new Response.Listener<Bitmap>()
                    {
                        @Override
                        public void onResponse(Bitmap response)
                        {

                            my_advra.add_ads(id,title,bitmap,response,seller_name,content,views,likes,comments,date);
                            //profilePic=response;
                            //imageView.setImageBitmap(response);
                            Log.d("volley","succesful");
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError e)
                        {
                            my_advra.add_ads(id,title,bitmap,null,seller_name,content,views,likes,comments,date);
                            Log.e("voley",""+e.getMessage()+e.toString());
                        }
                    });
            RequestQueue request2 = Volley.newRequestQueue(getContext());
            request2.add(request);
        }

        private String makeName(int id)
        {
            String letters=String.valueOf(id);
            char[] array=letters.toCharArray();
            String name="";
            for(int count=0; count<array.length; count++)
            {
                switch (array[count])
                {
                    case '0':
                        name+="zero";
                        break;
                    case '1':
                        name+="one";
                        break;
                    case '2':
                        name+="two";
                        break;
                    case '3':
                        name+="three";
                        break;
                    case '4':
                        name+="four";
                        break;
                    case '5':
                        name+="five";
                        break;
                    case '6':
                        name+="six";
                        break;
                    case '7':
                        name+="seven";
                        break;
                    case '8':
                        name+="eight";
                        break;
                    case '9':
                        name+="nine";
                        break;
                    default :
                        name+="NON";
                }
            }
            return name;
        }
    }

}
