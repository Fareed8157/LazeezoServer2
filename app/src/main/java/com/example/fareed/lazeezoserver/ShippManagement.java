package com.example.fareed.lazeezoserver;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Model.Shipper;
import com.example.fareed.lazeezoserver.ViewHolder.ShipperViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class ShippManagement extends AppCompatActivity {

    FloatingActionButton fab;

    FirebaseDatabase database;
    DatabaseReference shipperTable;

    TextView title;
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager manager;

    FirebaseRecyclerAdapter<Shipper,ShipperViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipp_management);

        fab=(FloatingActionButton)findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShipperLayout();
            }
        });

        recyclerView=(RecyclerView)findViewById(R.id.recycler_shipper);
        recyclerView.setHasFixedSize(true);
        manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        database=FirebaseDatabase.getInstance();
        shipperTable=database.getReference(Common.SHIPPER_TABLE);

        loadAllShippers();
    }

    private void loadAllShippers() {
        FirebaseRecyclerOptions<Shipper> allShipper=new FirebaseRecyclerOptions.Builder<Shipper>()
                .setQuery(shipperTable,Shipper.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(allShipper) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder holder, final int position, @NonNull final Shipper model) {
                holder.shipperPhone.setText(model.getPhone());
                holder.shipperName.setText(model.getName());
                Log.i("inshipper", "onCreateViewHolder: ");


                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(adapter.getRef(position).getKey(),model);
                    }
                });

                holder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeShipper(adapter.getRef(position).getKey());
                    }
                });
            }

            @Override
            public ShipperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shipper_layout,parent,false);
                return new ShipperViewHolder(view);
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void removeShipper(String key) {
        shipperTable.child(key)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShippManagement.this, "Removed Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShippManagement.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }



    private void showEditDialog(String key, Shipper model) {
            AlertDialog.Builder shipperDialog=new AlertDialog.Builder(ShippManagement.this);
            LayoutInflater inflater=this.getLayoutInflater();
            View view=inflater.inflate(R.layout.create_shipper_layout,null);

            final AutoCompleteTextView edtName=(AutoCompleteTextView)view.findViewById(R.id.edtName);
            final AutoCompleteTextView edtPhone=(AutoCompleteTextView)view.findViewById(R.id.edtPhone);
            final AutoCompleteTextView edtPass=(AutoCompleteTextView)view.findViewById(R.id.edtPassword);
            shipperDialog.setTitle("Update Shipper Account");

            edtName.setText(model.getName());
            edtPass.setText(model.getPassword());
            edtPhone.setText(model.getPhone());


            shipperDialog.setView(view);
            shipperDialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

            shipperDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();


                    Map<String,Object> update=new HashMap<>();
                    update.put("name",edtName.getText().toString());
                    update.put("phone",edtPhone.getText().toString());
                    update.put("password",edtPass.getText().toString());

                     shipperTable.child(edtPhone.getText().toString())
                            .updateChildren(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ShippManagement.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ShippManagement.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            shipperDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            shipperDialog.show();

    }

    private void showCreateShipperLayout() {
        AlertDialog.Builder shipperDialog=new AlertDialog.Builder(ShippManagement.this);
        shipperDialog.setTitle("Create Shipper Account");

        LayoutInflater inflater=this.getLayoutInflater();
        View view=inflater.inflate(R.layout.create_shipper_layout,null);

        final AutoCompleteTextView edtName=(AutoCompleteTextView)view.findViewById(R.id.edtName);
        final AutoCompleteTextView edtPhone=(AutoCompleteTextView)view.findViewById(R.id.edtPhone);
        final AutoCompleteTextView edtPass=(AutoCompleteTextView)view.findViewById(R.id.edtPassword);
        shipperDialog.setView(view);
        shipperDialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

        shipperDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                Shipper shipper=new Shipper();
                shipper.setName(edtName.getText().toString());
                shipper.setPhone(edtPass.getText().toString());
                shipper.setPassword(edtPass.getText().toString());

                shipperTable.child(edtPhone.getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShippManagement.this, "Shipper Account Created", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShippManagement.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        shipperDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        shipperDialog.show();
    }
}
