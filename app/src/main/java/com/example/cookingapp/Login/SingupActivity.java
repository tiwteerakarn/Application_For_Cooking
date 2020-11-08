package com.example.cookingapp.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SingupActivity extends AppCompatActivity {
    private EditText username,email,password;
    private TextView textView;
    private Button button;
    private FirebaseAuth mAuth ;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mfollowers = mRootRef.child("followers");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        username = findViewById(R.id.fielduser);
        email = findViewById(R.id.fieldEmail);
        password = findViewById(R.id.fieldPassword);
        textView = findViewById(R.id.txt_login);
        mAuth = FirebaseAuth.getInstance();
        button = findViewById(R.id.loginButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(username.getText())) {
                    username.setError("กรุณาป้อน username");
                } else if (TextUtils.isEmpty(email.getText()) || !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
                    email.setError("กรุณาป้อน email");
                } else if (TextUtils.isEmpty(password.getText())) {
                    password.setError("กรุณาป้อน password");
                } else {

                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(SingupActivity.this, "สำเร็จ",
                                                Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(SingupActivity.this,"Verification Email Has been sent",Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SingupActivity.this,"On Fail : Email not sent",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(username.getText().toString().trim()).build();
                                        user.updateProfile(profileUpdates);
                                        updateUI(user, username.getText().toString().trim());
                                        Intent intent = new Intent(SingupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {

                                        Toast.makeText(SingupActivity.this, "มีผู้ใช้นี้แล้ว",
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null, null);
                                    }
                                }
                            });
                }
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingupActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void updateUI(FirebaseUser user,String username) {
        if (user != null && username != null) {
            final Users users1 = new Users(user.getUid(), user.getEmail(), username);
            users1.setProfile_photo("");
            mUsersRef.child(users1.getUser_id()).setValue(users1);
            mfollowers.child(user.getUid()).child(user.getUid()).child("user_id").setValue(user.getUid());
            mfollowing.child(user.getUid()).child(user.getUid()).child("user_id").setValue(user.getUid());
        }
    }
}
