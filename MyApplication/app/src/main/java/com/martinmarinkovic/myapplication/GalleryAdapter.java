package com.martinmarinkovic.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class GalleryAdapter extends BaseAdapter {

   /* public interface GalleryItemClickListener{
        void onItemClick(int position);
    }*/

    private Context context;
    private Integer[] items = new Integer[10];
    LayoutInflater inflater;
    //GalleryItemClickListener mOnClickListener;

    public GalleryAdapter(/*GalleryItemClickListener item*/ Context context) {

        this.context = context;
        this.items = new Integer[10];
        items[0] = (R.drawable.sticker_1);
        items[1] = (R.drawable.sticker_2);
        items[2] = (R.drawable.sticker_3);
        items[3] = (R.drawable.sticker_4);
        items[4] = (R.drawable.sticker_5);
        items[5] = (R.drawable.sticker_6);
        items[6] = (R.drawable.sticker_7);
        items[7] = (R.drawable.sticker_8);
        items[8] = (R.drawable.sticker_9);
        items[9] = (R.drawable.sticker_10);
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        /*if (convertView == null) {
            convertView = inflater.inflate(R.layout.note_image, null);
        }*/
        convertView = inflater.inflate(R.layout.note_image, null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv);
        imageView.setImageResource(items[position]);

        return convertView;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}