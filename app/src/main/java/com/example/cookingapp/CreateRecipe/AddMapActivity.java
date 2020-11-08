package com.example.cookingapp.CreateRecipe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.Ingredientv2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMapActivity extends AppCompatActivity implements View.OnClickListener {
    private CheckBox ch1,ch2 ;
    private TextInputEditText fooddetail,lattitle,lngtitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button bt1;
    private AutoCompleteTextView autoCompleteTextView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Maps");
    DatabaseReference myLatLng = database.getReference("user_LatLng");
    DatabaseReference myVip = database.getReference("vip_point");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore ff = FirebaseFirestore.getInstance();
    private String vip ="";
    private int point = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_map);
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.recipe_list);
        //foodname = findViewById(R.id.recipe_name);
        lattitle = findViewById(R.id.recipe_lat);
        lngtitle = findViewById(R.id.recipe_lng);
        fooddetail = findViewById(R.id.recipe_detail);
        bt1 = findViewById(R.id.recipe_btn);
        ch1 = findViewById(R.id.checkBox);
        ch2 = findViewById(R.id.checkBox2);
        ch1.setChecked(true);
        fooddetail.setEnabled(false);
        lattitle.setEnabled(false);
        lngtitle.setEnabled(false);
        setTitle("เพิ่มวัตถุดิบ");
        ch1.setOnClickListener(this);
        ch2.setOnClickListener(this);



        List<String> v2 = new ArrayList<>();
        List<Ingredientv2> v1 = new ArrayList<>();
        Query query =  db.collection("ingredient").orderBy("name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()){
                    v2.clear();
                    v1.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        Ingredientv2 ingredientv2 = queryDocumentSnapshot.toObject(Ingredientv2.class);
                        v2.add(ingredientv2.getName());
                        v1.add(ingredientv2);
                    }

                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddMapActivity.this,android.R.layout.select_dialog_item,v2);

        autoCompleteTextView.setAdapter(adapter);



                bt1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ch1.isChecked() == true){
                            if (!TextUtils.isEmpty(autoCompleteTextView.getText())){

                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(AddMapActivity.this);
                                builder.setMessage("คุณต้องการเพิ่มรายการ");
                                builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        myRef.orderByChild("name").equalTo(autoCompleteTextView.getText().toString().trim())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (!dataSnapshot.exists()){

                                                            final Map<String, String> meterial = new HashMap<>();
                                                            meterial.put("name", autoCompleteTextView.getText().toString().trim());
                                                            meterial.put("user",mAuth.getUid());

                                                            db.collection("ingredient").add(meterial).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    String id = documentReference.getId();
                                                                    db.collection("ingredient").document(id).update("id",id);
                                                                    myRef.child(id).setValue(meterial);
                                                                    myRef.child(id).child("id").setValue(id).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(AddMapActivity.this, "สำเร็จ", Toast.LENGTH_LONG).show();
                                                                            finish();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }else {
                                                            Toast.makeText(AddMapActivity.this, "มีรายการนี้อยู่แล้ว", Toast.LENGTH_LONG).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                });

                                builder.setPositiveButton("ยกเเลิก", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();

                            }else {
                                Toast.makeText(AddMapActivity.this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_LONG).show();
                            }
                        }else {

                            if (!TextUtils.isEmpty(fooddetail.getText()) && !TextUtils.isEmpty(lattitle.getText()) && !TextUtils.isEmpty(lngtitle.getText())
                                    && !TextUtils.isEmpty(autoCompleteTextView.getText().toString())) {

                                //bt1.setText(autoCompleteTextView.getText().toString().trim());
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(AddMapActivity.this);
                                builder.setMessage("คุณต้องการเพิ่มรายการ");
                                builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String IatLngId = myRef.push().getKey();

                                        HashMap<String, String> hashMap = new HashMap<>();


                                        HashMap<String, String> hashMap2 = new HashMap<>();
                                        HashMap<String, String> hashMap3 = new HashMap<>();

                                        myRef.orderByChild("name").equalTo(autoCompleteTextView.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {

                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        Ingredientv2 in = ds.getValue(Ingredientv2.class);
                                                        if (in.getName().equals(autoCompleteTextView.getText().toString().trim())) {
                                                            hashMap2.put("idname", in.getId());
                                                            hashMap2.put("idlatlng", IatLngId);

                                                            hashMap.put("title", fooddetail.getText().toString().trim());
                                                            hashMap.put("lat", lattitle.getText().toString().trim());
                                                            hashMap.put("lng", lngtitle.getText().toString().trim());
                                                            hashMap.put("id_user", user.getUid());
                                                            hashMap.put("name", autoCompleteTextView.getText().toString().trim());
                                                            hashMap.put("id_ingredient", in.getId());
                                                            hashMap.put("id_latlng", IatLngId);

                                                            myRef.child(in.getId()).child("LatLng").child(IatLngId).setValue(hashMap);


                                                            myLatLng.child(user.getUid()).child(IatLngId).setValue(hashMap2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    //myVip.child(user.getUid()).child("point").setValue((point - 1));
                                                                    Toast.makeText(AddMapActivity.this, "สำเร็จ", Toast.LENGTH_LONG).show();
                                                                    finish();
                                                                }
                                                            });
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(AddMapActivity.this, "ไม่พบรายการ", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });

                                builder.setPositiveButton("ยกเเลิก", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            } else {
                                Toast.makeText(AddMapActivity.this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });


        checkVip();
    }

    private void checkVip() {
        myVip.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    VipPointModel vipPointModel = dataSnapshot.getValue(VipPointModel.class);
                    vip = vipPointModel.getVip();
                    point = vipPointModel.getPoint();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void openMap(View view) {
        Intent intent = new Intent(AddMapActivity.this,MapsActivity.class);
        startActivityForResult(intent,5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( data != null ) {
            if (requestCode == 5) {
                Double txtlat = data.getDoubleExtra("number1", 0);
                Double txtlng = data.getDoubleExtra("number2", 0);

                lattitle.setText(String.valueOf(txtlat));
                lngtitle.setText(String.valueOf(txtlng));
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkBox:
                if (ch1.isChecked()) {
                    //Toast.makeText(getApplicationContext(), "ch1", Toast.LENGTH_LONG).show();
                    ch2.setChecked(false);
                    fooddetail.setEnabled(false);
                    lattitle.setEnabled(false);
                    lngtitle.setEnabled(false);
                    bt1.setClickable(false);
                }else {
                    ch1.setChecked(true);
                }
                break;
            case R.id.checkBox2:
                if (ch2.isChecked()){
                    //Toast.makeText(getApplicationContext(), "ch2", Toast.LENGTH_LONG).show();
                    ch1.setChecked(false);
                    fooddetail.setEnabled(true);
                    lattitle.setEnabled(true);
                    lngtitle.setEnabled(true);
                    bt1.setClickable(true);
                }else {
                    ch2.setChecked(true);
                }
                break;
        }
    }
}
