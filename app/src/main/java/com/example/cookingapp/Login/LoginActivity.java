package com.example.cookingapp.Login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Admin.MainAdminActivity;
import com.example.cookingapp.MainActivity;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private EditText fieldemai,fieldpassword;
    private FirebaseAuth mAuth,mface;
    private ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mfollowers = mRootRef.child("followers");
    private DatabaseReference mfacebook = mRootRef.child("facebook_users");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    ArrayList<String> userkey = new ArrayList<>();
    ArrayList<String> userkey2 = new ArrayList<>();
    CallbackManager callbackManager;
    LoginManager loginManager;
    Dialog dialog;
    Button loginButton;
    SignInButton signInButton;
    TextView textView,textReset;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private DatabaseReference mUsersEditRef = mRootRef.child("user_edit_update");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());
        sharedPreferences=getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor=sharedPreferences.edit();
        dialog=  new Dialog(this);
        //showLogin();

        textReset = findViewById(R.id.reset_pass);
        textView = findViewById(R.id.txt_singup);
        ImageView signInButton = findViewById(R.id.sign_in_button);
        ImageView signOutButton = findViewById(R.id.facebook_button);
        pDialog = new ProgressDialog(LoginActivity.this);
        loginButton = findViewById(R.id.loginButton);
        fieldemai = findViewById(R.id.fieldEmail);
        fieldpassword = findViewById(R.id.fieldPassword);


        String email=sharedPreferences.getString("email","");
        String passwords=sharedPreferences.getString("password","");

        fieldemai.setText(email);
        fieldpassword.setText(passwords);

        textReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setTitle("ลืมรหัสผ่าน");
                dialog.setMessage("กรอกอีเมล์ของคุณ");
                final EditText input = new EditText(LoginActivity.this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                dialog.setView(input);

                dialog.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText().toString().trim()) || !Patterns.EMAIL_ADDRESS.matcher(input.getText().toString().trim()).matches()) {
                            Toast.makeText(LoginActivity.this, "กรุณาใส่อีเมล์", Toast.LENGTH_SHORT).show();
                        } else {

                            mAuth.fetchSignInMethodsForEmail(input.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    boolean isNewUser = !task.getResult().getSignInMethods().isEmpty();
                                    if (!isNewUser) {

                                        Toast.makeText(LoginActivity.this, "ไม่พบอีเมล์ในระบบ", Toast.LENGTH_SHORT).show();

                                    } else {

                                        mAuth.sendPasswordResetEmail(input.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(LoginActivity.this, "กรุณาตรวจสอบที่อีเมล์", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        }
                    }
                });

                dialog.setNegativeButton("ยกเลิก",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                dialog.show();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(fieldemai.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "กรุณาป้อน email",
                            Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(fieldpassword.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "กรุณาป้อน password",
                            Toast.LENGTH_SHORT).show();
                } else {

                    mAuth.signInWithEmailAndPassword(fieldemai.getText().toString().trim(), fieldpassword.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information

                                        FirebaseUser user = mAuth.getCurrentUser();

                                        if (!user.isEmailVerified()) {
                                            Toast.makeText(LoginActivity.this, "กรุณายืนยันที่กล่องรับอีเมล์", Toast.LENGTH_SHORT).show();
                                        } else{

                                            DatabaseReference block = mRootRef.child("banned").child(mAuth.getUid());
                                        block.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    addNewUserData(user);
                                                    editor.putString("email", fieldemai.getText().toString().trim());
                                                    editor.putString("password", fieldpassword.getText().toString().trim());
                                                    editor.commit();
                                                    Toast.makeText(LoginActivity.this, "สำเร็จ",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "ถูกระงับการใช้งาน", Toast.LENGTH_SHORT).show();

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    } else {

                                        Toast.makeText(LoginActivity.this, "บัญชีผู้ใช้ไม่ถูกต้อง",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

            }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        mface = FirebaseAuth.getInstance();

        callbackManager = CallbackManager.Factory.create();




        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebook_login();
                //Gett();
            }
        });

        textView.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View v){
                Intent intent = new Intent(LoginActivity.this, SingupActivity.class);
                startActivity(intent);
            }

        });

    }




    private void facebook_login() {
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        //LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        //Toast.makeText(LoginActivity.this,"token "+loginResult.getAccessToken(),Toast.LENGTH_SHORT).show();

                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel()
                    {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        // App code
                    }
                });


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token.getToken());


        AuthCredential credential2 = FacebookAuthProvider.getCredential(token.getToken());
         mAuth.signInWithCredential(credential2)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                                //final Users users1 = new Users(user.getUid(), user.getEmail(), user.getDisplayName(),user.getPhotoUrl().toString());
                            //mfacebook.child(user.getUid()).setValue(user);
                            DatabaseReference block = mRootRef.child("banned").child(mAuth.getUid());
                            block.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()) {
                                        addNewUserData(user);
                                        Toast.makeText(LoginActivity.this,"สำเร็จ",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(LoginActivity.this,"ถูกระงับการใช้งาน",Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this,"มีผู้ใช้นี้แล้ว",Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        displayProgressDialog();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();


                            DatabaseReference block = mRootRef.child("banned").child(mAuth.getUid());
                            block.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()) {
                                        addNewUserData(user);

                                    }else {
                                        Toast.makeText(LoginActivity.this,"ถูกระงับการใช้งาน",Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed: ", Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }

                });
    }


    private void signOut() {
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void signIn() {

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    private void displayProgressDialog() {
        pDialog.setMessage("กำลังเข้าสู่ระบบ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void addNewUserData(FirebaseUser user) {

        //final Users users1 = new Users(user.getUid(), user.getEmail(), user.getDisplayName(), user.getPhotoUrl()+"");
        String dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        if (user.getDisplayName() != null && user.getPhotoUrl() != null) {
            HashMap<String,String> map = new HashMap<>();
            map.put("user_id",user.getUid());
            map.put("email",user.getEmail());
            map.put("username",user.getDisplayName());
            map.put("profile_photo",user.getPhotoUrl()+"");
            map.put("logintime",dateAdded);

            //users1.setLogintime(dateAdded);

            mUsersRef.child(user.getUid()).setValue(map);

        }else {

            mUsersRef.child(user.getUid()).child("logintime").setValue(dateAdded);
        }

        VipPointModel vipPointModel= new VipPointModel("vip0",2);
        mVip.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mVip.child(user.getUid()).setValue(vipPointModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mfollowers.child(user.getUid()).child(user.getUid()).child("user_id").setValue(user.getUid());
        mfollowing.child(user.getUid()).child(user.getUid()).child("user_id").setValue(user.getUid());

        Query query = FirebaseDatabase.getInstance().getReference()
                .child("following").child(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userkey2.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        if (!ds.getKey().equals(user.getUid()) )
                        {
                            userkey2.add(ds.getKey());
                        }

                    }
                    mUsersRef.child(user.getUid()).child("following").setValue(userkey2.size());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query2 = FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(user.getUid());
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userkey.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        if (!ds.getKey().equals(user.getUid()))
                        {
                            userkey.add(ds.getKey());
                        }

                    }
                    mUsersRef.child(user.getUid()).child("follower").setValue(userkey.size());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mVip.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    //VipPointModel vipPointModel = dataSnapshot.getValue(VipPointModel.class);
                    String vip = dataSnapshot.child("vip").getValue(String.class);
                    if (vip.equals("vip2")){
                        Intent intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                        startActivity(intent);
                        finish();

                    }else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void Gett() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Maps");
        FileInputStream fis = null;

        List<String> name = new ArrayList<>();

        try {

            InputStreamReader isr = new InputStreamReader(getAssets().open("tiwtest.txt"));
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            int i=0;
            while ((text = br.readLine()) != null ) {
                name.add(text.trim());
                i++;
            }

            Toast.makeText(LoginActivity.this,"i : "+i +"\n " + "NAME : "+ name.size(),Toast.LENGTH_LONG).show();

            for (int j=0;j<name.size();j++){
                final Map<String, Object> meterial = new HashMap<>();
                meterial.put("name", name.get(j));
                meterial.put("user","Admin");

                db.collection("ingredient").add(meterial).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        final Map<String, Object> user5 = new HashMap<>();
                        user5.put("id", id);

                        db.collection("ingredient").document(id).update("id", id);

                        myRef.child(id).setValue(meterial);
                        myRef.child(id).child("id").setValue(id);
                    }
                });
            }




        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
