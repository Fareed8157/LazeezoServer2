package com.example.fareed.lazeezoserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.R;

/**
 * Created by fareed on 2/1/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder {


    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;
    private ItemClickListener itemClickListener;

    public Button edit,remove,detail,direction;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId=(TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus=(TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView)itemView.findViewById(R.id.order_phone);
        txtOrderAddress=(TextView)itemView.findViewById(R.id.order_address);

        edit=(Button)itemView.findViewById(R.id.btnEdit);
        remove=(Button)itemView.findViewById(R.id.btnRemove);
        detail=(Button)itemView.findViewById(R.id.btnDetail);
        direction=(Button)itemView.findViewById(R.id.btnDirection);


        //itemView.setOnClickListener(this);
        //itemView.setOnCreateContextMenuListener(this);
       // itemView.setOnLongClickListener(this);
    }



//    @Override
//    public void onClick(View view) {
//        itemClickListener.onClick(view,getAdapterPosition(),false);
//    }
//
//    @Override
//    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//        contextMenu.setHeaderTitle("Choose Option");
//        contextMenu.add(0,0,getAdapterPosition(),"Update");
//        contextMenu.add(0,1,getAdapterPosition(),"Delete");
//
//    }
//
//    @Override
//    public boolean onLongClick(View view) {
//        itemClickListener.onClick(view,getAdapterPosition(),true);
//        return true;
//    }
}

