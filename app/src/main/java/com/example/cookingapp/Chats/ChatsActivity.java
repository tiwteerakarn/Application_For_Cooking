package com.example.cookingapp.Chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Adapter.ChatAdapter;
import com.example.cookingapp.Model.ModelChat;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.example.cookingapp.notifications.APIServer;
import com.example.cookingapp.notifications.Client;
import com.example.cookingapp.notifications.Data5;
import com.example.cookingapp.notifications.MyResponse;
import com.example.cookingapp.notifications.Sender;
import com.example.cookingapp.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private EditText messageET;
    private Button btn_send;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private RecyclerView recyclerView;
    private List<ModelChat> chatlist;
    private ChatAdapter chatAdapter;
    private DatabaseReference  NotificationRef;
    String hisid;
    String myid;
    String img;
    APIServer apiServer;
    boolean block = false;
    boolean follow = false;
    private DatabaseReference mฺBlock = mRootRef.child("block");
    private DatabaseReference mฺFOLOW = mRootRef.child("following");
    boolean notify = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        textView = findViewById(R.id.txt_chatname);
        imageView = findViewById(R.id.img_pro);
        btn_send = findViewById(R.id.btn_send);
        messageET = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView_chat);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        apiServer = Client.getRetrofit("https://fcm.googleapis.com/").create(APIServer.class);

        Bundle bundle = getIntent().getExtras();
        String hisname = bundle.getString("name");


        Intent intent = getIntent();
        hisid = intent.getStringExtra("hisid");
        img = intent.getStringExtra("img");

        myid = user.getUid();


        textView.setText(hisname);
        try {
            Picasso.get().load(img).placeholder(R.drawable.ic_home).into(imageView);
        }catch (Exception e){

        }

        DatabaseReference df = mฺBlock.child(hisid);
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.child("user_id").getValue().equals(myid)) {
                            block = true;
                            break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference dl = mฺFOLOW.child(myid);
        dl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.child("user_id").getValue().equals(hisid)) {
                            follow = true;
                            break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (block) {

                    Toast.makeText(ChatsActivity.this,"คุณถูกบล็อค",Toast.LENGTH_SHORT).show();

                }else if (!follow){
                    Toast.makeText(ChatsActivity.this,"คุณยังไม่ได้ติดตามผู้ใช้นี้",Toast.LENGTH_SHORT).show();
                } else {
                    notify = true;
                    String message = messageET.getText().toString().trim();
                    if (TextUtils.isEmpty(message)) {
                        Toast.makeText(ChatsActivity.this, "กรุณาพิมพ์ข้อความ", Toast.LENGTH_SHORT).show();
                    } else {
                        sendMessage(message, hisid);
                        sendChatRequest(message, hisid);
                        //sendFCM(message,user.getDisplayName());

                        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(user.getUid()).child(hisid);
                        ref1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    ref1.child("id").setValue(hisid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        final DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisid).child(user.getUid());
                        ref2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    ref2.child("id").setValue(user.getUid());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });

        readMessage();
        //seenMessage();

    }

    private void sendFCM(String message,String myid) {
        Data5 data = new Data5(myid,message,"ข้อความใหม่",message);
        Sender sender = new Sender(data , "eT5TeQeFUmI:APA91bEhfcbdv6DbPqUUozsOKlm7Z3384IBTDJzX9zmHNaJtAHQZSWrdGNbZ0xqAdRAKW7o40YXnO4QtjT0C9Z6PlrX1Y4-m9CPkIIENKzFDVUAJKsD7n9RL96spQhVcm-zbwU3ujJrv");
        apiServer.sendNotification(sender)
                .enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        Toast.makeText(ChatsActivity.this, response.headers().toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {

                    }
                });
    }

    private void sendChatRequest(String message, String hisid) {
    }

    private void seenMessage() {

        final DatabaseReference df = FirebaseDatabase.getInstance().getReference("Chats");
        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(user.getUid()) && modelChat.getSender().equals(hisid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isSenn",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage() {

        chatlist = new ArrayList<>();

        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Chats");
        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);

                    if (modelChat.getReceiver().equals(myid) && modelChat.getSender().equals(hisid) ||
                            modelChat.getReceiver().equals(hisid) && modelChat.getSender().equals(myid)){

                        chatlist.add(modelChat);
                    }
                    chatAdapter = new ChatAdapter(chatlist,img);
                    //chatAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage(final String message, final String hisid) {

        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",user.getUid());
        hashMap.put("receiver",hisid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);

        mRootRef.child("Chats").push().setValue(hashMap);


        messageET.setText("");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(myid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (notify){
                    senNotification(hisid,users.getUsername(),message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void senNotification(final String hisid, final String username, final String message) {
        DatabaseReference mtoken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = mtoken.orderByKey().equalTo(hisid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data5 data = new Data5(username,"one",message,user.getDisplayName());

                    Sender sender = new Sender(data,token.getToken());
                    apiServer.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    //Toast.makeText(ChatsActivity.this,response.message(),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
