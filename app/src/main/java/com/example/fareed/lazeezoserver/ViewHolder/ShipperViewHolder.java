package com.example.fareed.lazeezoserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.R;

import info.hoang8f.widget.FButton;

/**
 * Created by fareed on 15/04/2018.
 */

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView shipperName,shipperPhone;
    public FButton edit,remove;
    private ItemClickListener itemClickListener;

    public ShipperViewHolder(View itemView) {
        super(itemView);
        shipperName=(TextView)itemView.findViewById(R.id.shipper_name);
        shipperPhone=(TextView)itemView.findViewById(R.id.shipper_phone);

        edit=(FButton)itemView.findViewById(R.id.btnEdit);
        remove=(FButton)itemView.findViewById(R.id.btnRemove);


    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
