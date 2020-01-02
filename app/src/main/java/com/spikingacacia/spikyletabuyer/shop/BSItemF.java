package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BSItemF extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_GROUP_ID = "group_id";
    // TODO: Customize parameters
    private int mCategoryId;
    private int mGroupId;
    private OnListFragmentInteractionListener mListener;
    private  RecyclerView recyclerView;

    public BSItemF() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BSItemF newInstance(int categoryId, int groupId) {
        BSItemF fragment = new BSItemF();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID,categoryId);
        args.putInt(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mCategoryId=getArguments().getInt(ARG_CATEGORY_ID);
            mGroupId = getArguments().getInt(ARG_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_biitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
            //recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.HORIZONTAL));
            BSItemC content=new BSItemC(mCategoryId,mGroupId);
            recyclerView.setAdapter(new BSItemRecyclerViewAdapter(content.ITEMS, mListener,getContext(), mCategoryId,mGroupId));
        }
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.biitem_menu, menu);
        final MenuItem searchItem=menu.findItem(R.id.action_search);
        final SearchView searchView=(SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                BSItemRecyclerViewAdapter adapter=(BSItemRecyclerViewAdapter) recyclerView.getAdapter();
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                BSItemRecyclerViewAdapter adapter=(BSItemRecyclerViewAdapter) recyclerView.getAdapter();
                adapter.filter(newText);
                return true;
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onItemClicked(BSItemC.InventoryItem item);
    }
}
