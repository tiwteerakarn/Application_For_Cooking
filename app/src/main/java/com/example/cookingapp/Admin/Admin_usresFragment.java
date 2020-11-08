package com.example.cookingapp.Admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import com.example.cookingapp.Adapter.AdminUsersAdapter;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Model.VipPointModel;
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
public class Admin_usresFragment extends Fragment {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference mUsers = mRootRef.child("users");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private DatabaseReference mฺBanned = mRootRef.child("banned");
    private List<Users> userAccount;
    private AdminUsersAdapter usersAdapter;
    private RecyclerView recyclerView;
    EditText et_searchfood;
    TextView txt_title;
    private ImageButton bt_fillter;
    ProgressDialog pDialog;
    ArrayList<String> userkey = new ArrayList<>();

    public Admin_usresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_usres, container, false);
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

        bt_fillter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arr[] = {"ทั้งหมด","สมาชิกvip","สมาชิกทั่วไป","banned"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("เลือกรายการ")
                        .setItems(arr, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String selected = arr[which];
                                txt_title.setText(selected);

                                if (selected.equals("ทั้งหมด")){
                                    filter("ทั้งหมด");
                                }
                                else
                                {
                                    filter(txt_title.getText().toString());
                                }

                            }
                        }).show();
            }

        });

        return view;
    }
    private void displayProgressDialog() {
        pDialog.setMessage("กำลังโหลดข้อมูล...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }
    private void filter(String text) {
        displayProgressDialog();
        List<Users> uu = new ArrayList<>();

        if (text.equals("ทั้งหมด")) {
            df.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        uu.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Users users = ds.getValue(Users.class);

                            if (!mUser.getUid().equals(users.getUser_id())) {
                                uu.add(users);
                            }
                            usersAdapter = new AdminUsersAdapter(uu,getActivity());
                            recyclerView.setAdapter(usersAdapter);
                        }
                        txt_title.setText("ทั้งหมด: "+uu.size()+" รายการ");
                    }
                    pDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else if (text.equals("banned"))
        {
            Query query = mฺBanned;
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){
                        userkey.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            userkey.add(ds.getKey());

                        }

                        getUsers();
                    }
                    pDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else {

            Query query = mVip;
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        userkey.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            VipPointModel vipPointModel1 = ds.getValue(VipPointModel.class);
                            if (text.equals("สมาชิกvip")){
                                if (vipPointModel1.getVip().equals("vip1")) userkey.add(ds.getKey());
                            }else if (text.equals("สมาชิกทั่วไป")){
                                if (vipPointModel1.getVip().equals("vip0")) userkey.add(ds.getKey());
                            }
                        }
                        getUsers();
                    }else {
                        pDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getUsers() {
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userAccount.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Users users = ds.getValue(Users.class);

                        for (String user : userkey) {
                            if (user.equals(users.getUser_id()) && !mUser.getUid().equals(users.getUser_id()) ){
                                userAccount.add(users);
                            }
                        }
                        usersAdapter = new AdminUsersAdapter(userAccount,getActivity());
                        usersAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(usersAdapter);
                    }
                    txt_title.setText("ทั้งหมด: "+userAccount.size()+" รายการ");
                }
                pDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        filter("ทั้งหมด");
    }
}
