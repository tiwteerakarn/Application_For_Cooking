package com.example.cookingapp.Firebase;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cookingapp.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUsers {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mfollowers = mRootRef.child("followers");
    private DatabaseReference mUserDetail = mRootRef.child("users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    String uid,username,useremail;
    Context context;
    List<Users> usersList = new ArrayList<>();
    public FirebaseUsers(){}

    public FirebaseUsers(Context context,String uis){ this.uid = uis; this.context = context;}

    public void detailUser(){


        mUserDetail.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                if (dataSnapshot.exists()){
                    Users users = dataSnapshot.getValue(Users.class);
                    usersList.add(0,users);
                     username = users.getUsername();
                     useremail = users.getEmail();

                     Toast.makeText(context,""+usersList.size(),Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }
    public int size(){

        return usersList.size();
    }
}
