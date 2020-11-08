package com.example.cookingapp.Admin;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.cookingapp.Adapter.AdminVipAdapter;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Admin_user_vipFragment extends Fragment {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference mUsers = mRootRef.child("users");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private DatabaseReference mSetting = mRootRef.child("setting");
    private List<Users> userAccount;
    private AdminVipAdapter usersAdapter;
    private RecyclerView recyclerView;
    EditText et_searchfood;
    TextView txt_title;
    private ImageButton bt_fillter;
    ProgressDialog pDialog;
    ArrayList<String> userkey = new ArrayList<>();
    private DatabaseReference mUsersfood = mRootRef.child("foodmenu_user");
    private int countFollow,countFood;
    public Admin_user_vipFragment() {
        // Required empty public constructor
    }
    private void displayProgressDialog() {
        pDialog.setMessage("กำลังโหลดข้อมูล...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_user_vip, container, false);
        pDialog = new ProgressDialog(getActivity());
        userAccount = new ArrayList<>();
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerView_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bt_fillter = view.findViewById(R.id.filter_food);
        et_searchfood =view.findViewById(R.id.search_food);
        txt_title = view.findViewById(R.id.txt_title);

        et_searchfood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    usersAdapter.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }

    private void showVip(){
        displayProgressDialog();


        Query query = mUsers;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Users users = ds.getValue(Users.class);

                        mUsersfood.child(users.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                long count ;
                                if (dataSnapshot.exists()){
                                    count= dataSnapshot.getChildrenCount();
                                }else {
                                    count= 0;
                                }

                                mSetting.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String follow = dataSnapshot.child("follow").getValue(String.class);
                                            String foodmenu = dataSnapshot.child("foodmenu").getValue(String.class);

                                            countFollow = Integer.parseInt(follow);
                                            countFood = Integer.parseInt(foodmenu);

                                            if (users.getFollower() >= countFollow && count >= countFood && !users.getUser_id().equals(mAuth.getUid()) ){
                                                userAccount.add(users);
                                                usersAdapter = new AdminVipAdapter(userAccount,getActivity());
                                                usersAdapter.notifyDataSetChanged();
                                                recyclerView.setAdapter(usersAdapter);

                                                txt_title.setText("ทั้งหมด: "+userAccount.size()+" รายการ");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
                pDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pDialog.dismiss();
            }
        });

    }

    private void addVip(Users users, long count) {
        mSetting.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String follow = dataSnapshot.child("follow").getValue(String.class);
                    String foodmenu = dataSnapshot.child("foodmenu").getValue(String.class);

                    if (users.getFollower() >= Integer.parseInt(follow) && count >= Integer.parseInt(foodmenu)){
                        userAccount.add(users);

                        usersAdapter = new AdminVipAdapter(userAccount,getActivity());
                        usersAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(usersAdapter);
                    }
                    txt_title.setText("ทั้งหมด: "+userAccount.size()+" รายการ");
                }
                pDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pDialog.dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        showVip();

    }
}
