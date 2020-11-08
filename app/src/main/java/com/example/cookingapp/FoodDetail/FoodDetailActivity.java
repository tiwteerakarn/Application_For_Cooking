package com.example.cookingapp.FoodDetail;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Admin.Report_model;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Ingredients;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.Ingredientv2;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FoodDetailActivity extends AppCompatActivity implements
        RecognitionListener {
    String id_food;

    @BindView(R.id.rating) RatingBar rating;
    @BindView(R.id.bt_card)
    CardView cardView;
    @OnClick(R.id.bt_card)
    void onRatingClick(){
        showRating();
    }
    @BindView(R.id.txt_rating) TextView textrating;
    boolean singleResult = false;
    boolean runspeech = false;
    private void showRating() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodDetailActivity.this);
        alertDialog.setTitle("ให้คะแนน");
        View v = LayoutInflater.from(FoodDetailActivity.this).inflate(R.layout.layout_rating,null);
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingbar);
        alertDialog.setView(v);
        alertDialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FoodDetailActivity.this,"rating"+ratingBar.getRating(),Toast.LENGTH_LONG).show();
                setRating((double) ratingBar.getRating());
                rating.setRating(ratingBar.getRating());

            }
        });
        alertDialog.show();

    }

    private void setRating(Double rating1) {

        mfood.child(id_food).child("rating_user").child(user.getUid()).child("rating").setValue(rating1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    mfood.child(id_food).child("rating_user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double total = 0.0;
                            double count = 0.0;
                            double average = 0.0;
                            for(DataSnapshot ds: dataSnapshot.getChildren()) {
                                double rating2 = ds.child("rating").getValue(Double.class);
                                total = total + rating2;
                                count = count + 1;
                                average = total / count;
                            }
                            FoodsRef.document(id_food).update("rating",average);
                            mfood.child(id_food).child("Avarage").setValue(average);
                            textrating.setText("คะแนน : "+ average+ " ("+ (int)count + " vote)");
                            //Toast.makeText(FoodDetailActivity.this,"rating : "+average,Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }


    private static final String UTTERANCE_ID = "PastedText";
    private final int MY_DATA_CHECK_CODE = 111;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference Userfavorite = mRootRef.child("favorite");
    private DatabaseReference Usersfav = mRootRef.child("favorite_user");
    private DatabaseReference mfood = mRootRef.child("foodmenu");
    DatabaseReference myRef = mRootRef.child("Maps");
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private ArrayList<String> ingredients = new ArrayList<>();
    private ArrayList<String> directions = new ArrayList<>();
    private ArrayList<String> amount = new ArrayList<>();
    private TextView food_name, food_user, txt_direction,food_detail;
    private ImageView food_img,imguser;
    private FloatingActionButton btnlike;
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    boolean like = false;
    boolean speechCheck = false;
    private int num=0;
    int countLike;
    CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    private TextView txt_ratingBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Ingredients");
    private CollectionReference FoodsRef = db.collection("foodmenu");

    RecyclerView listView;
    private ArrayList<Ingredients> foodmenu5 = new ArrayList<>();
    private boolean initialized;
    private String queuedText;
    private boolean statusstart , next = false;
    ProgressDialog pDialog;
    String picurl = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        ButterKnife.bind(this);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(FoodDetailActivity.this);
        pDialog = new ProgressDialog(FoodDetailActivity.this);

        initializeTextToSpeech();
        resetSpeechRecognizer();


        //btnshare = findViewById(R.id.btnShare);
        food_detail = findViewById(R.id.food_detail);
        imguser = findViewById(R.id.img_user);
        btnlike = findViewById(R.id.btnlike);
        food_name = findViewById(R.id.food_name);
        food_user = findViewById(R.id.food_user);
        food_img = findViewById(R.id.food_detail_img);
        txt_direction = findViewById(R.id.txt_direction_detail);


        listView = (RecyclerView) findViewById(R.id.listView1);
        listView.setLayoutManager(new LinearLayoutManager(this));

        final Intent intentfood = getIntent();
        String id = intentfood.getStringExtra("id");
        id_food = id;

        //displayProgressDialog();
        FoodsRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists())
                {

                     Foodmenu foodmenu = documentSnapshot.toObject(Foodmenu.class);
                     picurl = foodmenu.getImage();
                     food_detail.setText(""+foodmenu.getDescription());
                    df.child(foodmenu.getUsername()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Users users = dataSnapshot.getValue(Users.class);
                                food_user.setText(users.getUsername());

                                try {
                                    Picasso.get().load(users.getProfile_photo()).placeholder(R.drawable.ic_home).into(imguser);
                                }catch (Exception e){

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    setTitle(foodmenu.getFoodname());
                    food_name.setText(foodmenu.getFoodname());
                    //textrating.setText(""+foodmenu.getRating());

                    mfood.child(id_food).child("rating_user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            double count = 0.0;
                            double rat =0.0;
                            for(DataSnapshot ds: dataSnapshot.getChildren()) {
                                double rating2 = ds.child("rating").getValue(Double.class);
                                count = count + 1;
                                if (ds.getKey().equals(user.getUid())){
                                    rat = ds.child("rating").getValue(Double.class);
                                }
                            }
                            rating.setRating((float)rat);
                            textrating.setText("คะแนน : "+ new DecimalFormat("0.00").format(foodmenu.getRating())+ " ("+ (int)count + " vote)");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    try {
                        Picasso.get().load(foodmenu.getImage()).placeholder(R.drawable.ic_launcher_background).into(food_img);
                    } catch (Exception e) {

                    }



                    for (String s : foodmenu.getIngredients()) {
                        ingredients.add(s);
                    }
                    for (String s : foodmenu.getDirections()) {
                        directions.add(s);
                    }

                    for (String s : foodmenu.getAmount()) {
                        amount.add(s);
                    }

                    String step = "";
                    StringBuilder direction = new StringBuilder("");


                    for (int i = 0; i < directions.size(); i++) {
                        direction.append((i + 1) + ". " + directions.get(i));
                        direction.append("\n\n");
                    }



                    txt_direction.setText(direction);
                    CustomAdapter customAdapter = new CustomAdapter(ingredients,amount);
                    listView.setAdapter(customAdapter);

                    customAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            //Toast.makeText(FoodDetailActivity.this,""+ingredients.size(),Toast.LENGTH_SHORT).show();

                            //Searchingredients(ingredients.get(position));
                            //SerachIn(ingredients.get(position));
                            //Searchh();

                            myRef.orderByChild("name").equalTo(ingredients.get(position).trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                                            Ingredientv2 in = ds.getValue(Ingredientv2.class);
                                            if (in.getName().equals(ingredients.get(position).trim())){
                                                Intent intent = new Intent(FoodDetailActivity.this,IngredientMapsActivity.class);
                                                intent.putExtra("id",in.getId());
                                                startActivity(intent);
                                                break;
                                            }
                                        }
                                    }else {
                                        Toast.makeText(FoodDetailActivity.this,"ไม่พบรายการ",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    });
                }
                pDialog.dismiss();
            }
        });



        Query query = Userfavorite.child(id);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    like = false;

                } else {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.child("user_id").getValue().equals(user.getUid())) {

                            like = true;

                            btnlike.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));


                        }
                    }
                }

                if (!like) {
                    like = false;
                    btnlike.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        btnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!like) {
                    like = true;
                    btnlike.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                    Toast.makeText(FoodDetailActivity.this, "Like", Toast.LENGTH_SHORT).show();
                    mRootRef.child("favorit_list").child(id).child(user.getUid()).child("user_id").setValue(user.getUid());
                    Userfavorite.child(id).child(user.getUid()).child("user_id").setValue(user.getUid());
                    Usersfav.child(user.getUid()).child(id).child("user_id").setValue(user.getUid());

                    Query query = Userfavorite.child(id);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                countLike = (int) dataSnapshot.getChildrenCount();
                            }else {
                                countLike = 0;
                            }
                            FoodsRef.document(id).update("like",countLike);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    btnlike.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    like = false;
                    Toast.makeText(FoodDetailActivity.this, "Unlike", Toast.LENGTH_SHORT).show();
                    Userfavorite.child(id).setValue(null);
                    Usersfav.child(user.getUid()).child(id).setValue(null);
                    mRootRef.child("favorit_list").child(id).child(user.getUid()).setValue(null);

                    Query query = Userfavorite.child(id);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                countLike = (int) dataSnapshot.getChildrenCount();
                            }else {
                                countLike = 0;
                            }
                            FoodsRef.document(id).update("like",countLike);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
        });




    }

    private void Report() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(FoodDetailActivity.this);
        dialog.setTitle("REPORT");
        dialog.setMessage("ข้อความ");
        final EditText input = new EditText(FoodDetailActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialog.setView(input);



        dialog.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    Toast.makeText(FoodDetailActivity.this, "กรุณาใส่ข้อความ", Toast.LENGTH_SHORT).show();
                } else {


                    Report_model report_model = new Report_model();
                    report_model.setDetail(input.getText().toString());
                    report_model.setId_food(id_food);
                    report_model.setId_user(user.getUid().toString());

                mRootRef.child("report").push().setValue(report_model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetailActivity.this, "ส่งแล้ว...", Toast.LENGTH_LONG).show();
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(FoodDetailActivity.this, "ส่งไม่สำเร็จ...", Toast.LENGTH_LONG).show();
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

    private void btSpeech(){
        if (!speechCheck){
            AlertDialog.Builder dialog = new AlertDialog.Builder(FoodDetailActivity.this);
            //dialog.setTitle("ออกจากโปรแกรม !!!");
            dialog.setCancelable(true);
            dialog.setMessage("พูด: *เริ่มต้น* เพื่อเริ่มขั้นตอน"+"\n"+"พูด: *อีกครั้ง* เพื่อฟังขั้นตอนอีกครั้ง"+"\n"+"พูด: *ต่อไป* เพื่อฟังขั้นตอนต่อไป");
            dialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    speechCheck = true;

                    num =0;
                    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FoodDetailActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
                        return;
                    }
                    setRecogniserIntent();
                    speech.startListening(recognizerIntent);
                }
            });
            dialog.show();


        }else {
            num = 0;
            speechCheck = false;
            statusstart = false;

            speech.stopListening();
            speech.destroy();
            initializeTextToSpeech();
            resetSpeechRecognizer();
        }
    }

    private void btoff(){
        num = 0;
        singleResult =false;
        speechCheck = false;
        statusstart = false;

        speech.stopListening();
        speech.destroy();
        initializeTextToSpeech();
        resetSpeechRecognizer();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    initialized = true;
                    int result = textToSpeech.setLanguage(new Locale("th"));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(FoodDetailActivity.this, "Language not supported!", Toast.LENGTH_SHORT).show();
                    }

                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(final String utteranceId) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        singleResult = true;
                                        //Toast.makeText(getApplicationContext(), "Started reading " + utteranceId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onDone(final String utteranceId) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        singleResult = false;
                                        //Toast.makeText(getApplicationContext(), "Finished Speaking " + utteranceId, Toast.LENGTH_SHORT).show();
                                        resetSpeechRecognizer();
                                        speech.startListening(recognizerIntent);

                                    }
                                });
                            }

                            @Override
                            public void onError(final String utteranceId) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Error with " + utteranceId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });


                    if (queuedText != null) {
                        speak(queuedText);
                    }


                        // Stay silent for 1000 ms
                        //textToSpeech.playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, UTTERANCE_ID);
                    }
                }

        });
    }
    private void displayProgressDialog() {
        pDialog.setMessage("กำลังโหลดข้อมูล...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }
    private void speak(String text) {

            if (!initialized) {
                queuedText = text;
                return;
            }
            singleResult = true;
            queuedText = null;

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UtteredWord");

            //Toast.makeText(getApplicationContext(), "text" + text, Toast.LENGTH_SHORT).show();
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params, UTTERANCE_ID);

    }


    private void resetSpeechRecognizer() {

        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);

        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }
    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en-EN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }
    private void restart(){
        ActivityCompat.requestPermissions
                (FoodDetailActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_PERMISSION);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(FoodDetailActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onResults(Bundle bundle) {

        ArrayList<String> matches = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);
        //Toast.makeText(FoodDetailActivity.this, "" + text, Toast.LENGTH_SHORT).show();

        if ( singleResult == false){
            if (text.trim().equals("เริ่ม") && statusstart == false) {
                statusstart = true;
                speak(directions.get(num));

            } else if (text.trim().equals("ถัดไป") || text.trim().equals("ต่อไป") && statusstart == true) {
                num = num + 1;
                if (num >= directions.size()) {
                    Toast.makeText(FoodDetailActivity.this, "สิ้นสุดขั้นตอน", Toast.LENGTH_SHORT).show();
                    //speak("สิ้นสุดขั้นตอน");
                    btoff();
                    //speech.destroy();
                    //textToSpeech.stop();
                } else {
                    speak(directions.get(num));

                }
            } else if (text.trim().equals("อีกครั้ง") && statusstart == true && num <= directions.size()) {
                speak(directions.get(num));

            } else if(statusstart == false){

                Toast.makeText(FoodDetailActivity.this, "พูดคำว่า *เริ่ม*", Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speech.startListening(recognizerIntent);
                        singleResult = false;
                    }
                }, 300);

            }else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speech.startListening(recognizerIntent);
                        singleResult = false;
                    }
                }, 300);

            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
    }


    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

        private LayoutInflater inflater;
        private Context context;
        private ArrayList<String> menu;
        private ArrayList<String> amount;
        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            onItemClickListener = listener;
        }

        public CustomAdapter(ArrayList<String> menu, ArrayList<String> amount) {
            this.menu = menu;
            this.amount =amount;
        }


        @NonNull
        @Override
        public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_row_detail, parent, false);
            MyViewHolder holder = new MyViewHolder(view,onItemClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, final int position) {
        holder.food.setText((position+1)+". " + menu.get(position));
        holder.amount.setText(amount.get(position));

        }

        @Override
        public int getItemCount() {
            return menu.size();
        }

        public interface OnItemClickListener {
            void onItemClick(int position);
        }
        public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView food ,amount ;
            public MyViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
                super(itemView);
                food = itemView.findViewById(R.id.ingredientText);
                amount = itemView.findViewById(R.id.wasteBin);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(position);
                            }
                        }
                    }
                });
            }
        }
    }
}
