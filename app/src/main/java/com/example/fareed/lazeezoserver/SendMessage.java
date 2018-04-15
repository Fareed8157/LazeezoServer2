package com.example.fareed.lazeezoserver;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Model.DataMessage;
import com.example.fareed.lazeezoserver.Model.MyResponse;
import com.example.fareed.lazeezoserver.Model.Notification;
import com.example.fareed.lazeezoserver.Model.Sender;
import com.example.fareed.lazeezoserver.Model.User;
import com.example.fareed.lazeezoserver.Remote.APIService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessage extends AppCompatActivity {

    AutoCompleteTextView title,msg;
    CircularProgressButton send;

    APIService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        mService=Common.getFCMClient();

        title=(AutoCompleteTextView)findViewById(R.id.msgtitle);
        msg=(AutoCompleteTextView)findViewById(R.id.msg);

        send=(CircularProgressButton)findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<String,String,String> register=new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        return "Done";
                    }
                    @Override
                    protected void onPostExecute(String s) {
                        if(s.equals("Done")){
                            String  abc =new StringBuilder("/topics/").append(Common.topicName).toString();
                            Map<String,String> dataSend=new HashMap<>();
                            dataSend.put("title",title.getText().toString());
                            dataSend.put("message",msg.getText().toString());
                            DataMessage dataMessage=new DataMessage(abc,dataSend);

//                            Notification notification=new Notification(msg.getText().toString(),title.getText().toString());
//                            Sender toTopic=new Sender();
//                            toTopic.to=new StringBuilder("/topics/").append(Common.topicName).toString();
//                            toTopic.notification=notification;
                            mService.sendNotification(dataMessage).enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.isSuccessful()){
                                        finish();
                                        Toast.makeText(SendMessage.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(SendMessage.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        super.onPostExecute(s);
                    }
                };
                send.startAnimation();
                register.execute();
//
            }
        });
    }
}
