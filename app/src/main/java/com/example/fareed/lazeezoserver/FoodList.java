package com.example.fareed.lazeezoserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.Model.Food;
import com.example.fareed.lazeezoserver.ViewHolder.FoodViewHolder;
import com.example.fareed.lazeezoserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodList extends AppCompatActivity {



    Uri imgUri;
    Food addFood;
    Button btnChoose,btnDone;
    AutoCompleteTextView foodName,price,discount,descp;
    View menu_layout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton floatingActionButton;

    RelativeLayout relativeLayout;
    FirebaseDatabase database;
    DatabaseReference foodList;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    int imgReq=10;
    //search
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    String categoryId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        //setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        relativeLayout=(RelativeLayout)findViewById(R.id.rootLayout);
        floatingActionButton=(FloatingActionButton)findViewById(R.id.fab);

        //Initialize Database
        database=FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");


        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();


        recyclerView=(RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFloating();
            }
        });
        //get CategoryId here
        if(getIntent()!=null)
            categoryId=getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty() && categoryId!=null){
            loadList(categoryId);
        }
    }



    private void makeFloating() {
        AlertDialog.Builder alertDialg=new AlertDialog.Builder(FoodList.this);
        alertDialg.setTitle("Add Food");
        alertDialg.setMessage("Insert the Data");

        LayoutInflater inflater=this.getLayoutInflater();
        menu_layout=inflater.inflate(R.layout.add_newfood_layout,null);


        btnChoose=menu_layout.findViewById(R.id.choose);
        btnDone=menu_layout.findViewById(R.id.done);
        foodName=menu_layout.findViewById(R.id.txtfoodName);
        price=menu_layout.findViewById(R.id.txtprice);
        descp=menu_layout.findViewById(R.id.txtdesc);
        discount=menu_layout.findViewById(R.id.txtdiscount);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImage();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImgInDb();
            }
        });



        alertDialg.setView(menu_layout);
        alertDialg.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        alertDialg.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(addFood!=null){
                    foodList.push().setValue(addFood);
                    Snackbar.make(relativeLayout,"Food Added "+addFood.getName()+"successfully",Snackbar.LENGTH_SHORT).show();

                }
            }
        });

        alertDialg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialg.show();
    }

    private void saveImgInDb() {
        final ProgressDialog dialog=new ProgressDialog(FoodList.this);
        dialog.setMessage("Uploading Data ....");
        dialog.show();

        String imName= UUID.randomUUID().toString();
        final StorageReference fldrImg=storageReference.child("images/"+imName);
        fldrImg.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Toast.makeText(FoodList.this, "Saved", Toast.LENGTH_SHORT).show();
                fldrImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        addFood=new Food();
                        addFood.setImage(uri.toString());
                        addFood.setName(foodName.getText().toString());
                        addFood.setDescription(descp.getText().toString());
                        addFood.setDiscount(discount.getText().toString());
                        addFood.setPrice(price.getText().toString());
                        addFood.setMenuId(categoryId);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(FoodList.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                final double prgr=(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                dialog.setMessage("Saved "+prgr+ "%" );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==imgReq && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imgUri=data.getData();
            btnChoose.setText("Image Choosed");
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.MODIFY)){
            updateOption(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.REMOVE)){
            removeFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);

    }

    private void removeFood(String key) {
        foodList.child(key).removeValue();
        Snackbar.make(relativeLayout,"Item Removed Permanently",Snackbar.LENGTH_SHORT).show();
    }


    private void updateOption(final String key, final Food item) {
        AlertDialog.Builder alertDialg=new AlertDialog.Builder(FoodList.this);
        alertDialg.setTitle("Update Food");
        alertDialg.setMessage("Insert the Data");

        LayoutInflater inflater=this.getLayoutInflater();
        menu_layout=inflater.inflate(R.layout.add_newfood_layout,null);


        btnChoose=menu_layout.findViewById(R.id.choose);
        btnDone=menu_layout.findViewById(R.id.done);
        foodName=menu_layout.findViewById(R.id.txtfoodName);
        price=menu_layout.findViewById(R.id.txtprice);
        descp=menu_layout.findViewById(R.id.txtdesc);
        discount=menu_layout.findViewById(R.id.txtdiscount);

        foodName.setText(item.getName());
        price.setText(item.getPrice());
        descp.setText(item.getDescription());
        discount.setText(item.getDiscount());


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImage();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImage(item);
            }
        });



        alertDialg.setView(menu_layout);
        alertDialg.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        alertDialg.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                    item.setName(foodName.getText().toString());
                    item.setDescription(descp.getText().toString());
                    item.setDiscount(discount.getText().toString());
                    item.setPrice(price.getText().toString());
                    foodList.child(key).setValue(item);
                    Snackbar.make(relativeLayout,"Food "+item.getName()+" Updated",Snackbar.LENGTH_SHORT).show();
            }
        });

        alertDialg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialg.show();
    }


    private void updateImage(final Food item) {
        final ProgressDialog dialog=new ProgressDialog(FoodList.this);
        dialog.setMessage("Uploading Data ....");

        final String imName= UUID.randomUUID().toString();
        final StorageReference fldrImg=storageReference.child("images/"+imName);
        if(imgUri!=null){
            fldrImg.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(FoodList.this, "Saved", Toast.LENGTH_SHORT).show();
                    fldrImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(FoodList.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    final double prgr=(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Saved "+prgr+ "%" );
                }
            });
            dialog.show();
        }else
            Toast.makeText(this, "Choose Image First", Toast.LENGTH_SHORT).show();

    }

    private void setImage() {
        Intent openGallery=new Intent();
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(openGallery,"Browse Image"),imgReq);
    }

    private void loadSuggest() {
        foodList.orderByChild("MenuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            Food food=postSnapshot.getValue(Food.class);
                            suggestList.add(food.getName());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void loadList(String categoryId) {
        Query query=foodList.orderByChild("menuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Food> options =new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query,Food.class)
                .build();
        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.foodImage);
                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Starting new activity
//                        Intent foodDetail=new Intent(FoodList.this,FoodDetail.class);
//                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());
//                        startActivity(foodDetail);
                    }
                });}


                @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
