package com.spikingacacia.spikyletabuyer.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;

/**
 * A fragment representing a single Message detail screen.
 * This fragment is either contained in a {@link BMMessageListActivity}
 * in two-pane mode (on tablets) or a {@link BMMessageDetailActivity}
 * on handsets.
 */
public class BMMessageDetailFragment extends Fragment
{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private BMMessageContent.MessageItem mItem;
    private String[]content;
    private Preferences preferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BMMessageDetailFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        content=getArguments().getStringArray("items");
        preferences = new Preferences(getContext());


        /*if (getArguments().containsKey(ARG_ITEM_ID))
        {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = BMMessageContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.bmmessage_detail, container, false);
        // Show the dummy content as text in a TextView.
        if (content != null)
        {
            ((TextView) rootView.findViewById(R.id.message_detail)).setText(content[3]);
            ((TextView) rootView.findViewById(R.id.date)).setText(content[4]);
        }

        return rootView;
    }
}
