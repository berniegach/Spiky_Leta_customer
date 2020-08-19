package com.spikingacacia.spikyletabuyer.main.tasty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;

import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardFragment.*;

public class MyTastyBoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<TastyBoard> mValues;
    private List<TastyBoard>itemsCopy;
    private final OnListFragmentInteractionListener mListener;
    private Context context;

    public MyTastyBoardRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context)
    {
        this.mListener = listener;
        this.context = context;
        mValues = new LinkedList<>();
        itemsCopy = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tasty_board, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tasty_board, parent, false);
        //return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolder)
        {
            populateItemRows((ViewHolder) holder, position);
        }
        else if (holder instanceof LoadingViewHolder)
        {
            showLoadingView((LoadingViewHolder) holder, position);
        }


    }

    @Override
    public int getItemCount()
    {
        return mValues == null ? 0 : mValues.size();
    }
    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mValues.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final TextView mRestaurant;
        public final TextView mTitleView;
        public final TextView mLocation;
        public final TextView mComments;
        public final ImageView image;
        public final ImageView imageSeller;
        public TastyBoard mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mRestaurant = (TextView) view.findViewById(R.id.restaurant);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mLocation = (TextView) view.findViewById(R.id.location);
            mComments = (TextView) view.findViewById(R.id.discount);
            image = view.findViewById(R.id.image);
            imageSeller = view.findViewById(R.id.image_seller);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
    public  class LoadingViewHolder extends RecyclerView.ViewHolder
    {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

    }
    public void filter(String text)
    {
        mValues.clear();
        if(text.isEmpty())
            mValues.addAll(itemsCopy);
        else
        {
            text=text.toLowerCase();
            for(TastyBoard item:itemsCopy)
            {
                if(item.getTitle().toLowerCase().contains(text))
                    mValues.add(item);
            }
        }
        notifyDataSetChanged();
    }
    public void listUpdated(List<TastyBoard> newitems)
    {
        mValues.clear();
        mValues.addAll(newitems);
        itemsCopy.addAll(newitems);
        notifyDataSetChanged();
    }
    public void listAddProgressBar()
    {
        mValues.add(null);
        notifyDataSetChanged();
    }
    public void listRemoveProgressBar()
    {
        mValues.remove(mValues.size()-1);
        notifyDataSetChanged();
    }
    public void listAddItems(List<TastyBoard> newitems)
    {
        mValues.addAll(newitems);
        itemsCopy.addAll(newitems);
        notifyDataSetChanged();
    }


    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private void populateItemRows(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mRestaurant.setText(String.valueOf(mValues.get(position).getSellerNames()));
        String s_distance = "location not found";
        Double distance = mValues.get(position).getDistance();
        if(distance!=-1)
            s_distance = distance<1000? String.format("%s %.0f m",mValues.get(position).getLocation(),distance) : String.format("%s %.0f km", mValues.get(position).getLocation(),distance/1000);
        holder.mLocation.setText(s_distance);
        holder.mComments.setText(String.valueOf(mValues.get(position).getComments()+" Reviews"));

        String image_url= base_url+"src/tasty_board_pics/";
        // image
        String url=image_url+String.valueOf(mValues.get(position).getId())+'_'+String.valueOf(mValues.get(position).getImageType());
        Glide.with(context).load(url).into(holder.image);
        String url_seller= LoginA.base_url+"src/sellers_pics/"+ mValues.get(position).getSellerId()+'_'+mValues.get(position).getSellerImageType();
        Glide.with(context).load(url_seller).into(holder.imageSeller);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener!=null)
                    mListener.onTastyBoardItemClicked(holder.mItem);
            }
        });

    }
}