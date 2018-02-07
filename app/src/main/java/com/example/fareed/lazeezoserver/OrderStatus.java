package com.example.fareed.lazeezoserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.Model.MyResponse;
import com.example.fareed.lazeezoserver.Model.Notification;
import com.example.fareed.lazeezoserver.Model.Order;
import com.example.fareed.lazeezoserver.Model.Request;
import com.example.fareed.lazeezoserver.Model.Sender;
import com.example.fareed.lazeezoserver.Model.Token;
import com.example.fareed.lazeezoserver.Remote.APIService;
import com.example.fareed.lazeezoserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    APIService mService;
    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    MaterialSpinner materialSpinner;
    FirebaseDatabase db;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        db=FirebaseDatabase.getInstance();
        requests=db.getReference("Requests");
        mService=Common.getFCMClient();

        recyclerView=(RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();

    }

    private void loadOrders() {
        adapter=new FirebaseRecyclerAdapter<Request,OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ){

            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());


                viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        floatingWinForUpdate(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        floatingWinForDel(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail=new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest=model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                viewHolder.direction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent trackingOrder=new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest=model;
                        startActivity(trackingOrder);
                    }
                });
//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int position, boolean isLongClick) {
//                        if(!isLongClick){
//                            Intent trackingOrder=new Intent(OrderStatus.this,TrackingOrder.class);
//                            Common.currentRequest=model;
//                            startActivity(trackingOrder);
//                        }
//
////                        else{
////                            Intent orderDetail=new Intent(OrderStatus.this,OrderDetail.class);
////                            Common.currentRequest=model;
////                            orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
////                            startActivity(orderDetail);
////                        }
//                    }
//                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if (item.getTitle().equals(Common.MODIFY))
//            floatingWinForUpdate(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
//        else if(item.getTitle().equals(Common.REMOVE))
//            floatingWinForDel(adapter.getRef(item.getOrder()).getKey());
//        return super.onContextItemSelected(item);
//    }

    private void floatingWinForDel(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void floatingWinForUpdate(String key, final Request item) {
        final AlertDialog.Builder alertSpinner=new AlertDialog.Builder(OrderStatus.this);
        alertSpinner.setTitle("Modify Status");
        alertSpinner.setMessage("Kindly Choose Status");

        LayoutInflater layout=this.getLayoutInflater();
        final View view=layout.inflate(R.layout.update_order_layout,null);
        materialSpinner=(MaterialSpinner)view.findViewById(R.id.statusSpinner);

        materialSpinner.setItems("Placed","On the way","Shipped");
        alertSpinner.setView(view);

        final String finalKey=key;
        alertSpinner.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(materialSpinner.getSelectedIndex()));
                requests.child(finalKey).setValue(item);
                adapter.notifyDataSetChanged();

                sendOrderStatusToUser(finalKey,item);
            }
        });
        alertSpinner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertSpinner.show();

    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens=db.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                            Token token=postSnapshot.getValue(Token.class);

                            Notification notification=new Notification("Lazeezo","Your Order "+key+" was updated");
                            Sender content=new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1){
                                                Toast.makeText(OrderStatus.this, "Order Update", Toast.LENGTH_SHORT).show();
                                            }else
                                                Toast.makeText(OrderStatus.this, "Order Updated but Failed To Send Notification", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("Error",t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
