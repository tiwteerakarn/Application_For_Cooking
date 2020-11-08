package com.example.cookingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cookingapp.Callback.Token;
import com.example.cookingapp.Chats.ChatsListFragment;
import com.example.cookingapp.Login.LoginActivity;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.Navigation.HomeFragment;
import com.example.cookingapp.User.Food_UserActivity;
import com.example.cookingapp.User.ListIngredientActivity;
import com.example.cookingapp.User.User_EditActivity;
import com.example.cookingapp.User.User_like_list_Activity;
import com.example.cookingapp.User.UsersFragment;
import com.example.cookingapp.SearchMenu.FilterFoodFragment;
import com.example.cookingapp.Post.PostFragment;
import com.example.cookingapp.Navigation.DarkModePrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private BottomNavigationView bottomNavigationView;
    private static final int MODE_DARK = 0;
    private static final int MODE_LIGHT = 1;
    Button btn_yess;
    private TextView username,userEdit,txt_vip;
    private ImageView imageUser,close_img;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String mUID = user.getUid();
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mfollowers = mRootRef.child("followers");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private DatabaseReference mฺBlock = mRootRef.child("block");
    private MenuItem nav_menu_like,nav_foodmenu,nav_ingredient,nav_admin;
    int countFollower;
    Dialog custom_popup;
    boolean pup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //PrintHash();
        custom_popup = new Dialog(this);


        setVip();


        setDarkMode(getWindow());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        UpdateToken(FirebaseInstanceId.getInstance().getToken());


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("A Mobile Application for Cooking");
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//
        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        //layoutParams.setBehavior(new BottomNavigationBehavior());

        bottomNavigationView.setSelectedItemId(R.id.navigationHome);

        //handling floating action menu


//inflate header layout

        View navView =  navigationView.getHeaderView(0);
//reference to views

        username = (TextView)navView.findViewById(R.id.txtUsername);
        userEdit = (TextView)navView.findViewById(R.id.txt_edit);
        imageUser = (ImageView) navView.findViewById(R.id.imageUser);
        txt_vip = (TextView) navView.findViewById(R.id.txt_vip);

        Menu menu = navigationView.getMenu();
        nav_foodmenu = menu.findItem(R.id.nav_foodmenu);
        nav_ingredient = menu.findItem(R.id.nav_ingredient);
        nav_menu_like= menu.findItem(R.id.nav_menu_like);


        userEdit.setVisibility(View.INVISIBLE);

        PrintHash();

        //useremail.setText(user.getEmail());
        username.setText(user.getDisplayName());



        try {
            Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.ic_user).into(imageUser);

        }catch (Exception e){}



    }

    private void setVip() {




        mVip.child(mUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    VipPointModel vipPointModel1 = dataSnapshot.getValue(VipPointModel.class);
                    txt_vip.setText("แต้ม : "+vipPointModel1.getPoint());
                    try {
                        if ("vip1".equals(vipPointModel1.getVip()+"")){
                            username.setBackgroundResource(R.drawable.shape_rect06);
                        }else {
                            username.setBackgroundDrawable(null);
                        }
                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void PrintHash() {
        try {

            PackageInfo info = getPackageManager().getPackageInfo("com.example.foodapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
            }

        }catch (Exception e){}
    }

    private void Showpopup() {
        if (!pup) {

            custom_popup.setContentView(R.layout.custom_popup);
            //close_img = (ImageView) custom_popup.findViewById(R.id.btn_close);
            btn_yess = (Button) custom_popup.findViewById(R.id.btn_closeyes);

            close_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    custom_popup.dismiss();
                }
            });
            btn_yess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    custom_popup.dismiss();
                }
            });

            custom_popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            custom_popup.show();
        }
    }



    private void setDarkMode(Window window) {
        if(new DarkModePrefManager(this).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            changeStatusBar(MODE_DARK,window);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            changeStatusBar(MODE_LIGHT,window);
        }

    }

    private void changeStatusBar(int mode, Window window) {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.contentStatusBar));
            //Light mode
            if(mode==MODE_LIGHT){
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            switch (item.getItemId()) {
                case R.id.navigationHome:

                    selectedFragment = new HomeFragment();
                    break;
                case R.id.navigationSearch:
                    selectedFragment = new FilterFoodFragment();

                    break;
                case R.id.navigationFeeds:
                    selectedFragment = new PostFragment();
                    break;
                case  R.id.navigationChats:
                    selectedFragment = new ChatsListFragment();

                    break;
                case  R.id.navigationUsers:
                    selectedFragment = new UsersFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();

            return true;
        }
    };
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_foodmenu) {
//            Fragment selectedFragment = new BlockFragment();
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    selectedFragment).commit();
            Intent intent = new Intent(MainActivity.this, Food_UserActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_ingredient) {
            Intent intent = new Intent(MainActivity.this, ListIngredientActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_menu_like) {
            Intent intent = new Intent(MainActivity.this, User_like_list_Activity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dark_mode) {
            //code for setting dark mode
            //true for dark mode, false for day mode, currently toggling on each click
            DarkModePrefManager darkModePrefManager = new DarkModePrefManager(this);
            darkModePrefManager.setDarkMode(!darkModePrefManager.isNightMode());
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();

        }else if (id == R.id.nav_exit) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("อกจากระบบ !!!");
            dialog.setIcon(R.drawable.ic_exit_to_app_black_24dp);
            dialog.setCancelable(true);
            dialog.setMessage("คุณต้องการออกจากระบบ ?");
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mAuth.signOut();
                    FirebaseAuth.getInstance().signOut();
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            dialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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

    }

    public void UpdateToken(String token){
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Tokens");
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("users");
        Token mtoken = new Token(token);
        df.child(mUID).setValue(mtoken);
        dr.child(mUID).child("device_token").setValue(token);

    }

}
