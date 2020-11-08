package com.example.cookingapp.CreateRecipe;

import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cookingapp.Model.FirebaseMethods;
import com.example.cookingapp.Model.Foodmenu;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.example.cookingapp.notifications.APIAllServer;
import com.example.cookingapp.notifications.Client;
import com.example.cookingapp.notifications.Data5;
import com.example.cookingapp.notifications.MyResponse;
import com.example.cookingapp.notifications.SenderAll;
import com.example.cookingapp.utils.Files;
import com.example.cookingapp.utils.LoadingDialog;
import com.example.cookingapp.utils.ResultCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateRecipeActivity extends AppCompatActivity implements
        RecipeImageFragment.ImageListener, RecipeDirectionsFragment.DirectionsListener,
        RecipeIngredientsFragment.IngredientListener {
    private static final int REQUEST_OPEN_GALLERY = 10;
    private static final int REQUEST_TO_ACCESS_GALLERY = 11;
    ArrayList<String> userkey = new ArrayList<>();
    private List<Users> userAccount = new ArrayList<>();
    private Foodmenu currentRecipe;
    private boolean isUpdating;
    private String currentCategory;
    //private DatabaseAdapter databaseAdapter;
    final FirebaseMethods firebaseMethods = new FirebaseMethods(this);
    private Button nextButton;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String mUID = user.getUid();
    LoadingDialog loadingDialog ;
    private  int point = 0;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private DatabaseReference mfollowers = myRef.child("followers");
    private DatabaseReference mfood_user = myRef.child("foodmenu_user");
    private DatabaseReference mvip = myRef.child("vip_point");
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    APIAllServer apiServer;
    private DatabaseReference  NotificationRef;
    private DatabaseReference mUsers = myRef.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        loadingDialog = new LoadingDialog(this);
        //databaseAdapter = DatabaseAdapter.getInstance(this);
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        apiServer = Client.getRetrofit("https://fcm.googleapis.com/").create(APIAllServer.class);

        Intent intent = getIntent();
        isUpdating = intent.getBooleanExtra("isUpdating", false);
       
            currentRecipe = new Foodmenu();
        
            currentRecipe.setUsername(mAuth.getUid());
        initializeUI();
        displayFragment(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (fragment instanceof RecipeIngredientsFragment) {
            displayFragment(0);

        }
        else if (fragment instanceof RecipeDirectionsFragment) {
            displayFragment(1);

        }
        else
            super.onBackPressed();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_OPEN_GALLERY:
                switch (resultCode) {
                    case RESULT_OK:
                        Uri imageData = data.getData();
                        String imageSrc = Files.getRealPathFromURI(this, imageData);
                        ((RecipeImageFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container))
                                .onImageSelected(imageSrc);

                        currentRecipe.setImage(imageSrc);
                        break;
                }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri postImageUri = result.getUri();
                ((RecipeImageFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container))
                        .onImageSelected(postImageUri.getPath());
                currentRecipe.setImage(postImageUri.getPath());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_TO_ACCESS_GALLERY:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openGallery();
                else
                    Toast.makeText(this, "Permission denied to access the gallery.", Toast.LENGTH_LONG)
                            .show();
                break;
        }
    }

    private void displayFragment(int position) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        String nextButtonText = "";

        switch (position) {
            case 0:
                fragment = RecipeImageFragment.newInstance(currentRecipe);
                ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                nextButtonText = "NEXT";
                break;
            case 1:
                fragment = RecipeIngredientsFragment.newInstance(currentRecipe);
                ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
                nextButtonText = "NEXT";
                break;
            case 2:
                fragment = RecipeDirectionsFragment.newInstance(currentRecipe);
                ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
                nextButtonText = "Finish";
                break;
        }

        nextButton.setText(nextButtonText);

        ft.replace(R.id.frame_container, fragment, "fragment" + position);
        ft.commit();
    }

    public void onNext(View view) {
        ((NavigableFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container)).onNext();
    }

    private void initializeUI() {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nextButton = findViewById(R.id.nextButton);

        mvip.child(mUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    VipPointModel vipPoint = dataSnapshot.getValue(VipPointModel.class);
                    point = vipPoint.getPoint();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void navigateToIngredientsFragment(String name, String description,List<String> tags) {
        currentRecipe.setFoodname(name);
        currentRecipe.setDescription(description);
        currentRecipe.setTags(tags);
        displayFragment(1);
    }

    @Override
    public void navigateToDirectionsFragment(List<String> ingredients,List<String> amount) {
        currentRecipe.setIngredients(ingredients);
        currentRecipe.setAmount(amount);
        displayFragment(2);
    }

    @Override
    public void onSelectImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_TO_ACCESS_GALLERY);
        } else {
            openGallery();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStepsFinished(List<String> directions) {

        //Toast.makeText(this,currentRecipe.getFoodname(),Toast.LENGTH_SHORT).show();

        currentRecipe.setLike(0);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(CreateRecipeActivity.this);
        builder.setMessage("คุณต้องการเพิ่มเมนู : "+currentRecipe.getFoodname());

        builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String,Object> updates = new HashMap<>();
                //displayProgressDialog();
                currentRecipe.setDirections(directions);
                currentRecipe.setLike(0);
                currentRecipe.setRating(0.0);
                db.collection("foodmenu").add(currentRecipe)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                mvip.child(mUID).child("point").setValue((point - 1));
                                updates.put("timestamp", FieldValue.serverTimestamp());
                                updates.put("documentId",documentReference.getId());
                                db.collection("foodmenu").document(documentReference.getId()).update(updates);
                                myRef.child("foodmenu").child(documentReference.getId()).child("foodid").setValue(documentReference.getId());
                                firebaseMethods.uploadNewPhotoFood(currentRecipe.getImage(),documentReference.getId());
                                mfood_user.child(mUID).child(documentReference.getId()).child("id").setValue(documentReference.getId());
                                Toast.makeText(CreateRecipeActivity.this,"สำเร็จ คะแนน: "+(point-1),Toast.LENGTH_SHORT).show();

                                senNotification();

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateRecipeActivity.this,"ล้มเหลว",Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.i("CreateRecipeActivity", "Final recipe: " + currentRecipe);
                setResult(isUpdating ? ResultCodes.RECIPE_EDITED : ResultCodes.RECIPE_ADDED);
                finish();
            }
        });
        builder.setPositiveButton("ยกเเลิก", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();


    }


    private void senNotification() {


        Query query = mfollowers.child(mUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userkey.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        userkey.add(ds.getKey());

                    }

                    sendNotifi();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendNotifi() {

        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userAccount.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Users users = ds.getValue(Users.class);

                        for (String user : userkey) {
                            if (user.equals(users.getUser_id()) && !mUID.equals(users.getUser_id()) ){
                                userAccount.add(users);
                            }
                        }
                    }

                    String[] userToken = new String[userAccount.size()];
                    for (int i=0;i<userAccount.size();i++){
                        userToken[i] = userAccount.get(i).getDevice_token();
                    }

                    Data5 data5 = new Data5(user.getDisplayName(),"all",user.getDisplayName(),user.getDisplayName());

                    SenderAll senderAll = new SenderAll(data5,userToken);
                    //Sender sender = new Sender(data5,"eKCS5UxhCJ0:APA91bHf51PNH-_YD75825xgaSzeI7vF3RCI4APwbk2ErDo-el7Gv9lQgzLOpgwMrN-2ZgMW7FG4V8aNEmRPSR6z7vGISJGc_Wkc734VgCDefxiRaN5wKm1g3Wn6lIxmm6NluUWAI6iY");

                    apiServer.sendNotification(senderAll)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    //Toast.makeText(CreateRecipeActivity.this,""+userToken.length,Toast.LENGTH_SHORT).show();
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

    private void openGallery() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(CreateRecipeActivity.this);
    }




}
