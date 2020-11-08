package com.example.cookingapp.Model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.cookingapp.MainActivity;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class FirebaseMethods {
    private List<String> ingredient5;
    private static final String TAG = "FirebaseMethods";
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private Activity mActivity;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    ArrayList<String> userkey = new ArrayList<>();
    ArrayList<String> userkey2 = new ArrayList<>();
    private long mediaCount = 0;
    public boolean verify = false;
    private Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseMethods() {

    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FirebaseMethods(String userID) {
        this.userID = userID;
    }

    public FirebaseMethods(Activity activity) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        myRef = database.getReference();
        mStorageRef = mStorage.getReference();
        mActivity = activity;
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }





    public void uploadNewPhoto(final String caption, String imageUrl, final ProgressBar progressBar){

        final String FIREBASE_IMAGE_STORAGE = "post/users/";
        FileCompressor compressor = new FileCompressor(mActivity);
        final StorageReference storageReference;

        //If it is not a profile photo
        progressBar.setVisibility(View.VISIBLE);

            storageReference =  mStorageRef.child(FIREBASE_IMAGE_STORAGE+userID+"/"+ UUID.randomUUID().toString());
            final UploadTask uploadTask = storageReference.putFile(Uri.fromFile(compressor.compressImage(imageUrl)));

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
                        addPhotoToDatabase(caption,downloadUri.toString());
                        progressBar.setVisibility(View.GONE);

                        mActivity.finish();
                        mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                        Toast.makeText(mActivity, "โพสต์สำเร็จ", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(mActivity, "โพสต์ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Tracking progress
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    long uploadPercentage = (taskSnapshot.getBytesTransferred()*100)/taskSnapshot.getTotalByteCount();

                }
            });
        //If it is a profile photo
    }


    public void uploadNewPhotoFood(String imageUrl, final String docid){
        final String FIREBASE_IMAGE_STORAGE = "FoodImg/";
        FileCompressor compressor = new FileCompressor(mActivity);
        final StorageReference storageReference;

        storageReference =  mStorageRef.child(FIREBASE_IMAGE_STORAGE+docid+"/"+ UUID.randomUUID().toString());
        final UploadTask uploadTask = storageReference.putFile(Uri.fromFile(compressor.compressImage(imageUrl)));
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
                    db.collection("foodmenu").document(docid).update("image",downloadUri.toString());


                    mActivity.finish();
                    //mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                    //Toast.makeText(mActivity, "โสำเร็จ", Toast.LENGTH_LONG).show();
                } else {


                    //Toast.makeText(mActivity, "ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Tracking progress
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                long uploadPercentage = (taskSnapshot.getBytesTransferred()*100)/taskSnapshot.getTotalByteCount();

            }
        });

    }

    public void setPostCount(final String uid, final TextView postCount){

        Query query = myRef.child(mActivity.getString(R.string.user_photos_node)).child(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mediaCount = dataSnapshot.getChildrenCount();
                Query query = myRef.child(mActivity.getString(R.string.user_videos_node)).child(uid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        mediaCount+=dataSnapshot.getChildrenCount();
                        postCount.setText(String.valueOf(mediaCount));
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

    public long getImageCount(DataSnapshot dataSnapshot) {
        Log.d(TAG,"image_count: "+dataSnapshot.getChildrenCount());
        return dataSnapshot.getChildrenCount();
    }





    private void addPhotoToDatabase(String caption, String imageUrl){

        String photoId = myRef.push().getKey();
        String dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        BlogPost blogPost = new BlogPost(userID,imageUrl,caption,dateAdded);


        myRef.child(mActivity.getString(R.string.user_photos_node)).child(userID).child(photoId).setValue(blogPost);
        myRef.child(mActivity.getString(R.string.photos_node)).child(photoId).setValue(blogPost);
    }

    public void addFollowingAndFollowers(String uid){

        FirebaseDatabase.getInstance().getReference()
                .child("following")
                .child(userID)
                .child(uid)
                .child("user_id")
                .setValue(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference()
                        .child("following")
                        .child(userID);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            userkey2.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                if (!ds.getKey().equals("total") && !ds.getKey().equals(userID) )
                                {
                                    userkey2.add(ds.getKey());
                                }

                            }
                            df.child(userID).child("following").setValue(userkey2.size());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(uid)
                .child(userID)
                .child("user_id")
                .setValue(userID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference()
                        .child("followers")
                        .child(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            userkey.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                if (!ds.getKey().equals("total") && !ds.getKey().equals(uid) )
                                {
                                    userkey.add(ds.getKey());
                                }

                            }
                            df.child(uid).child("follower").setValue(userkey.size());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }


    public void removeFollowingAndFollowers(String uid){

        FirebaseDatabase.getInstance().getReference()
                .child("following")
                .child(userID)
                .child(uid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference()
                        .child("followers")
                        .child(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            userkey.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                if (!ds.getKey().equals("total") && !ds.getKey().equals(uid) )
                                {
                                    userkey.add(ds.getKey());
                                }

                            }
                            df.child(uid).child("follower").setValue(userkey.size());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(uid)
                .child(userID)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference()
                        .child("followers")
                        .child(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            userkey.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                if (!ds.getKey().equals("total") && !ds.getKey().equals(uid) )
                                {
                                    userkey.add(ds.getKey());
                                }

                            }
                            df.child(uid).child("follower").setValue(userkey.size());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void AddblockUser(String uid){

        FirebaseDatabase.getInstance().getReference()
                .child("block")
                .child(userID)
                .child(uid)
                .child("user_id")
                .setValue(uid);

    }
    public void removeblockUser(String uid){

        FirebaseDatabase.getInstance().getReference()
                .child("block")
                .child(userID)
                .child(uid)
                .removeValue();

    }




}
