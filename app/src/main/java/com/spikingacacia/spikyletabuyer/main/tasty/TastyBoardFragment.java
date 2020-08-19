package com.spikingacacia.spikyletabuyer.main.tasty;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;
import com.spikingacacia.spikyletabuyer.main.MainActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;

/**
 * A fragment representing a list of Items.
 */
public class TastyBoardFragment extends Fragment
{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RecyclerView recyclerView;
    private OnListFragmentInteractionListener mListener;
    private MyTastyBoardRecyclerViewAdapter myTastyBoardRecyclerViewAdapter;
    boolean isLoading = false;
    private int lastTastyBoardId = 0;
    private String TAG = "tasty_board_f";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TastyBoardFragment()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TastyBoardFragment newInstance(int columnCount)
    {
        TastyBoardFragment fragment = new TastyBoardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tasty_board_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            myTastyBoardRecyclerViewAdapter = new MyTastyBoardRecyclerViewAdapter(mListener,getContext());
            recyclerView.setAdapter(myTastyBoardRecyclerViewAdapter);
            initScrollListener();
        }
        new GetTastyBoardTask(-1, false).execute((Void)null);
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
        void onTastyBoardItemClicked( TastyBoard tastyBoard);

    }
    private void initScrollListener()
    {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading)
                {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == myTastyBoardRecyclerViewAdapter.getItemCount() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


    }
    private void loadMore() {
        myTastyBoardRecyclerViewAdapter.listAddProgressBar();
        new GetTastyBoardTask(lastTastyBoardId,true).execute((Void)null);

    }

    public class GetTastyBoardTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_items = base_url + "get_tasty_boards.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private int last_index;
        private List<TastyBoard>localList = new LinkedList<>();
        private boolean load_more;
        private String[] locationPieces;
        @Override
        protected void onPreExecute()
        {
            Log.d("SITEMS: ","starting....");
            jsonParser = new JSONParser();
            locationPieces = MainActivity.myLocation.split(":");
            super.onPreExecute();
        }
        public GetTastyBoardTask(int last_index, boolean load_more)
        {
            this.last_index = last_index;
            this.load_more = load_more;
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("last_index",String.valueOf(last_index)));
            info.add(new BasicNameValuePair("latitude",locationPieces.length>1 ? locationPieces[0] :"null" ));
            info.add(new BasicNameValuePair("longitude", locationPieces.length>1 ? locationPieces[1] :"null" ));

            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_items,"POST",info);
            //Log.d("sItems",""+jsonObject.toString());
            try
            {
                JSONArray itemsArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    itemsArrayList=jsonObject.getJSONArray("ads");
                    for(int count=0; count<itemsArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=itemsArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        String seller_id = jsonObjectNotis.getString("seller_id");
                        String seller_email = jsonObjectNotis.getString("seller_email");
                        String title =jsonObjectNotis.getString("title");
                        String description = jsonObjectNotis.getString("description");
                        int linked_item_id =jsonObjectNotis.getInt("linked_item_id");
                        String size_and_price = jsonObjectNotis.getString("size_and_price");
                        String discount_price = jsonObjectNotis.getString("discount_price");
                        String expiry = jsonObjectNotis.getString("expiry");
                        String image_type=jsonObjectNotis.getString("image_type");
                        int views = jsonObjectNotis.getInt("views");
                        int likes = jsonObjectNotis.getInt("likes");
                        int comments = jsonObjectNotis.getInt("comments");
                        int orders = jsonObjectNotis.getInt("orders");
                        String date_added=jsonObjectNotis.getString("date_added");
                        //String date_changed=jsonObjectNotis.getString("date_changed");
                        String seller_names = jsonObjectNotis.getString("seller_name");
                        String seller_image_type = jsonObjectNotis.getString("seller_image_type");
                        double distance = jsonObjectNotis.getDouble("distance");
                        String location = jsonObjectNotis.getString("location");
                        String country = jsonObjectNotis.getString("country");
                        lastTastyBoardId = id;

                        TastyBoard tastyBoard = new TastyBoard(id, seller_id, seller_email, title, description, linked_item_id, size_and_price, discount_price,expiry,image_type,
                                views,likes,comments,orders,date_added, seller_names, seller_image_type, distance, location, country);
                        localList.add(tastyBoard);
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


            if(load_more)
            {
                myTastyBoardRecyclerViewAdapter.listRemoveProgressBar();
                isLoading = false;
            }
            if (successful)
            {
                if(load_more)
                {
                    myTastyBoardRecyclerViewAdapter.listAddItems(localList);
                }
                else
                {
                    myTastyBoardRecyclerViewAdapter.listUpdated(localList);
                }


            }
            else
            {

            }
        }
    }
}