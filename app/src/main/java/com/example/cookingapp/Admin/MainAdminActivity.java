package com.example.cookingapp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cookingapp.Login.LoginActivity;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.FilterFoodFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainAdminActivity extends AppCompatActivity {
    private FrameLayout fragmentContainer;
    private LinearLayout btadmin1,btadmin2,btadmin3;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mSet = mRootRef.child("setting");
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mfollowers = mRootRef.child("followers");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private DatabaseReference mUsers = mRootRef.child("users");

    private EditText editText;
    private Button button;
    private ImageView imageView,imageView2;
    Fragment fragment = null;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        btadmin1 = findViewById(R.id.admin1);
        btadmin2 = findViewById(R.id.admin2);
        btadmin3 = findViewById(R.id.admin3);
        imageView2 = findViewById(R.id.ttt2);
        imageView = findViewById(R.id.ttt1);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        if (fragment == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Admin_usresFragment()).commit();
        }


        //AddPoin();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainAdminActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.customdialog);
                dialog.setCancelable(true);
                EditText et1 = (EditText) dialog.findViewById(R.id.followset);
                EditText et2 = (EditText) dialog.findViewById(R.id.foodnum);

                mSet.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String follow = dataSnapshot.child("follow").getValue(String.class);
                            String foodmenu = dataSnapshot.child("foodmenu").getValue(String.class);

                            et1.setText(follow);
                            et2.setText(foodmenu);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                Button button1 = (Button)dialog.findViewById(R.id.button1);
                button1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        HashMap<String,String> map = new HashMap<>();
                        map.put("follow",et1.getText().toString());
                        map.put("foodmenu",et2.getText().toString());

                        mSet.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext()
                                        , "บันทึกสำเร็จ", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.cancel();
                    }
                });

                Button button2 = (Button)dialog.findViewById(R.id.button2);
                button2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });


                dialog.show();
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainAdminActivity.this);
                dialog.setTitle("ออกจากระบบ");
                dialog.setIcon(R.drawable.ic_exit_to_app_black_24dp);
                dialog.setCancelable(true);
                dialog.setMessage("คุณต้องการออกจากระบบ");
                dialog.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent intent = new Intent(MainAdminActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });

                dialog.setNegativeButton("ไม่", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.show();
            }
        });

        btadmin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new Admin_usresFragment());
            }
        });

        btadmin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new FilterFoodFragment());
            }
        });

        btadmin3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new AdminReportFragment());
            }
        });
    }


    public void openFragment(Fragment fragment1) {
        fragment = fragment1;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainAdminActivity.this);
        dialog.setTitle("ออกจากโปรแกรม !!!");
        dialog.setIcon(R.drawable.ic_exit_to_app_black_24dp);
        dialog.setCancelable(true);
        dialog.setMessage("คุณต้องการออกจากโปรแกรม ?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }
    private void AddPoin(){
        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        Users users = ds.getValue(Users.class);
                        mVip.child(users.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    VipPointModel vipPointModel = new VipPointModel();
                                    vipPointModel.setVip("vip0");
                                    vipPointModel.setPoint(2);
                                    mVip.child(users.getUser_id()).setValue(vipPointModel);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
