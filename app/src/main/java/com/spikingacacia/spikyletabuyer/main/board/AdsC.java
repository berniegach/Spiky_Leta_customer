package com.spikingacacia.spikyletabuyer.main.board;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class AdsC
{

    /**
     * An array of sample (dummy) items.
     */
    public static final List<AdItem> ITEMS = new ArrayList<AdItem>();


    public AdItem createItem(String id, String title, Bitmap bitmap,  Bitmap bitmap_seller, String seller_name, String content, String views, String likes, String comments, String date)
    {
        return new AdItem(id, title, bitmap, bitmap_seller, seller_name, content, views, likes, comments, date);
    }

    public  class AdItem
    {
        public final String id;
        public final Bitmap bitmap;
        public final Bitmap bitmap_seller;
        public final String seller_name;
        public final String title;
        public final String content;
        public final String views;
        public final String likes;
        public final String comments;
        public final String date;

        public AdItem(String id, String title, Bitmap bitmap, Bitmap bitmap_seller, String seller_name, String content, String views, String likes, String comments, String date)
        {
            this.id = id;
            this.title = title;
            this.bitmap=bitmap;
            this.bitmap_seller=bitmap_seller;
            this.seller_name=seller_name;
            this.content=content;
            this.likes = likes;
            this.views=views;
            this.comments=comments;
            this.date=date;
        }

        @Override
        public String toString()
        {
            return title;
        }
    }
}
