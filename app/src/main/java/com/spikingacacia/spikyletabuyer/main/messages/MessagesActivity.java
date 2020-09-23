package com.spikingacacia.spikyletabuyer.main.messages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardFragment;

public class MessagesActivity extends AppCompatActivity
{
    private ProgressBar progressBar;
    private View mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        setTitle("Messages");
        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);

        Fragment fragment= MessagesFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"messages");
        transaction.commit();

    }
}