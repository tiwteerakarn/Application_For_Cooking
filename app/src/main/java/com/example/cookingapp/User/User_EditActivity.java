package com.example.cookingapp.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.MainActivity;
import com.example.cookingapp.Model.FileCompressor;
import com.example.cookingapp.Model.FirebaseMethods;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Post.NewPostActivity;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class User_EditActivity extends AppCompatActivity {
    private TextView username;
    private ImageView imageUser;
    private Button newEditBtn,choose;
    private Uri postImageUri = null;
    private FirebaseAuth firebaseAuth;
    private Bitmap compressedImageFile;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private StorageReference mStorageRef;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String mUID = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__edit);

        setTitle("แก้ไขโปรไฟล์");

        username = findViewById(R.id.user_name);
        imageUser = findViewById(R.id.profile_image);
        newEditBtn = findViewById(R.id.choose_save);
        choose = findViewById(R.id.choose_image);

        final DatabaseReference myRef = database.getReference();
        final FirebaseMethods firebaseMethods = new FirebaseMethods(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        showProfile();


        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(User_EditActivity.this);
            }
        });

        newEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = username.getText().toString();

                if (!TextUtils.isEmpty(name) && postImageUri != null) {


                    final String randomName = UUID.randomUUID().toString();

                    // PHOTO UPLOAD
                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(User_EditActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();


                    // PHOTO UPLOAD

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            final String FIREBASE_IMAGE_STORAGE = "profile/users/";
                            FileCompressor compressor = new FileCompressor(User_EditActivity.this);
                            final StorageReference storageReference;

                            storageReference =  mStorageRef.child(FIREBASE_IMAGE_STORAGE+mUID+"/"+ UUID.randomUUID().toString());
                            final UploadTask uploadTask = storageReference.putFile(Uri.fromFile(compressor.compressImage(postImageUri.getPath())));

                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw Objects.requireNonNull(task.getException());
                                    }
                                    // Continue with the task to get the download URL
                                    return storageReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {


                                        Uri downloadUri = task.getResult();
                                        addPhotoToDatabase(name,downloadUri.toString());

                                        Toast.makeText(User_EditActivity.this, "แก้ไขสำเร็จ", Toast.LENGTH_LONG).show();


                                        finish();
                                    } else {


                                        Toast.makeText(User_EditActivity.this, "ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }


        });

    }

    private void showProfile() {
        database.getReference("users").child(mUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Users users = dataSnapshot.getValue(Users.class);
                    try {
                        username.setText(users.getUsername());
                        Picasso.get().load(users.getProfile_photo()).placeholder(R.drawable.ic_user).into(imageUser);

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPhotoToDatabase(String name, String pic) {
        database.getReference("users").child(mUID).child("profile_photo").setValue(pic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                database.getReference("users").child(mUID).child("username").setValue(name);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                imageUser.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProfile();
    }
}
