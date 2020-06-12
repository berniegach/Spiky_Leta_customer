package com.spikingacacia.spikyletabuyer.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;

import java.util.List;

/**
 * An activity representing a list of Messages. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BMMessageDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BMMessageListActivity extends AppCompatActivity
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    Preferences preferences;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_bmmessage_list);
        preferences = new Preferences(getBaseContext());

        if (findViewById(R.id.message_detail_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messages");


        RecyclerView recyclerView = findViewById(R.id.message_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),DividerItemDecoration.VERTICAL));
        assert recyclerView != null;
        setupRecyclerView(recyclerView);
        context=getBaseContext();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView)
    {
        BMMessageContent content=new BMMessageContent();
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, content.ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>
    {

        private final BMMessageListActivity mParentActivity;
        private final List<BMMessageContent.MessageItem> mValues;
        private final boolean mTwoPane;
        Preferences preferences;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                BMMessageContent.MessageItem item = (BMMessageContent.MessageItem) view.getTag();
                if (mTwoPane)
                {
                    Bundle arguments = new Bundle();
                    arguments.putStringArray("items",new String[]{item.position,String.valueOf(item.id),String.valueOf(item.classes),item.message,item.dateAdded});
                   // arguments.putString(BMMessageDetailFragment.ARG_ITEM_ID, item.position);
                    BMMessageDetailFragment fragment = new BMMessageDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.message_detail_container, fragment)
                            .commit();
                }
                else
                {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, BMMessageDetailActivity.class);
                    intent.putExtra("items",new String[]{item.position,String.valueOf(item.id),String.valueOf(item.classes),item.message,item.dateAdded});
                   // intent.putExtra(BMMessageDetailFragment.ARG_ITEM_ID, item.position);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(BMMessageListActivity parent,
                                      List<BMMessageContent.MessageItem> items,
                                      boolean twoPane)
        {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bmmessage_list_content, parent, false);
            preferences = new Preferences(context);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position)
        {
            String message="";
            if((mValues.get(position).message).length()>40)
                message=(mValues.get(position).message).substring(0,40)+"...";
            else
                message=mValues.get(position).message;

            holder.mPositionView.setText(mValues.get(position).position);
            holder.mMessageView.setText(message);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount()
        {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            final TextView mPositionView;
            final TextView mMessageView;

            ViewHolder(View view)
            {
                super(view);
                mPositionView = (TextView) view.findViewById(R.id.position);
                mMessageView = (TextView) view.findViewById(R.id.message);
            }
        }
    }
}
