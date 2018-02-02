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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Interface.ItemClickListener;
import com.example.fareed.lazeezoserver.Model.Category;
import com.example.fareed.lazeezoserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference category;
    TextView name;

    DrawerLayout drawer;
    Category addCatg;
    Uri imgUri;
    private final int imgReq=10;

    AutoCompleteTextView catName;
    Button btnChoose,btnDone;

    FirebaseStorage storage;
    StorageReference storageReference;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    View menu_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Menu Operation");

        //initialize firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");

        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFloating();
            }
        });


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView=navigationView.getHeaderView(0);
        name=(TextView)headerView.findViewById(R.id.fullName);
        name.setText(Common.currentUser.getName().toString());


        //load Menu
        recycler_menu=(RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

        loadMenu();
    }

    private void saveImgInDb() {
        final ProgressDialog dialog=new ProgressDialog(Home.this);
        dialog.setMessage("Uploading Data ....");
        dialog.show();

        String imName= UUID.randomUUID().toString();
        final StorageReference fldrImg=storageReference.child("images/"+imName);
        fldrImg.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Toast.makeText(Home.this, "Saved", Toast.LENGTH_SHORT).show();
                fldrImg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        addCatg=new Category(catName.getText().toString(),uri.toString());

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(Home.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
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

    private void setImage() {
        Intent openGallery=new Intent();
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(openGallery,"Browse Image"),imgReq);
    }

    private void makeFloating() {
        AlertDialog.Builder alertDialg=new AlertDialog.Builder(Home.this);
        alertDialg.setTitle("Add Category");
        alertDialg.setMessage("Insert the Data");

        LayoutInflater inflater=this.getLayoutInflater();
        menu_layout=inflater.inflate(R.layout.add_new_menu_layout,null);


        btnChoose=menu_layout.findViewById(R.id.choose);
        btnDone=menu_layout.findViewById(R.id.done);
        catName=menu_layout.findViewById(R.id.txtName);


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

                if(addCatg!=null){
                    category.push().setValue(addCatg);
                    Snackbar.make(drawer,"Category Added "+addCatg.getName()+" successfully",Snackbar.LENGTH_SHORT).show();

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


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.MODIFY)){
            updateOption(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.REMOVE)){
            removeCatg(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void removeCatg(String key) {
        category.child(key).removeValue();
        Snackbar.make(drawer,"Item Removed Permanently",Snackbar.LENGTH_SHORT).show();
    }


    private void updateOption(final String key, final Category item) {
        AlertDialog.Builder alertDialg=new AlertDialog.Builder(Home.this);
        alertDialg.setTitle("Update Category");
        alertDialg.setMessage("Insert the Data");

        LayoutInflater menu=this.getLayoutInflater();
        menu_layout=menu.inflate(R.layout.add_new_menu_layout,null);


        btnChoose=menu_layout.findViewById(R.id.choose);
        btnDone=menu_layout.findViewById(R.id.done);
        catName=menu_layout.findViewById(R.id.txtName);
        //default name
        catName.setText(item.getName());

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImage();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyImage(item);
            }
        });

        alertDialg.setView(menu_layout);
        alertDialg.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialg.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                item.setName(catName.getText().toString());
                category.child(key).setValue(item);
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


    private void modifyImage(final Category item) {
        final ProgressDialog dialog=new ProgressDialog(Home.this);
        dialog.setMessage("Uploading Data ....");

        final String imName= UUID.randomUUID().toString();
        final StorageReference fldrImg=storageReference.child("images/"+imName);
        if(imgUri!=null){
            fldrImg.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(Home.this, "Saved", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Home.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
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

    private void loadMenu() {
        adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //we will send categoryId from here
                        Intent categoryId=new Intent(Home.this,FoodList.class);
                        categoryId.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(categoryId);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_order) {
            startActivity(new Intent(Home.this,OrderStatus.class));
            // Handle the camera action
        } else if (id == R.id.nav_order) {
            //startActivity(new Intent(Home.this,OrderStatus.class));
        } else if (id == R.id.nav_cart) {
            //startActivity(new Intent(Home.this,Cart.class));

        } else if (id == R.id.nav_logout) {
//            Intent signIn=new Intent(Home.this,SignIn.class);
//            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(signIn);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
