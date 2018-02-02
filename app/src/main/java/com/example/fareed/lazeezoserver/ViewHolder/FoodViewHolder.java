package com.example.fareed.lazeezoserver.ViewHolder;


import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.R;

/**
 * Created by fareed on 1/19/2018.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,View.OnCreateContextMenuListener{

    public TextView foodName;
    public ImageView foodImage;

    private ItemClickListener itemClickListener;
    public FoodViewHolder(View itemView) {
        super(itemView);

        foodName=(TextView)itemView.findViewById(R.id.food_name);
        foodImage=(ImageView)itemView.findViewById(R.id.food_image);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);


    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Choose Operation");

        contextMenu.add(0,0,getAdapterPosition(), Common.REMOVE);
        contextMenu.add(0,1,getAdapterPosition(), Common.MODIFY);

    }
}
