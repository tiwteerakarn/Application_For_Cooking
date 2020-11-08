package com.example.cookingapp.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cookingapp.Adapter.UserLikeListAdapter;
import com.example.cookingapp.FoodDetail.FoodDetailActivity;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Food_UserActivity extends AppCompatActivity {
    TextView txt_title;
    EditText et_searchfood;
    private List<String> list_id;
    private RecyclerView recyclerView;
    private UserLikeListAdapter userLikeListAdapter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference foodRef = db.collection("foodmenu");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference mUsers = mRootRef.child("users");
    private DatabaseReference Usersfav = mRootRef.child("foodmenu_user");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food__user);

        et_searchfood =findViewById(R.id.search_food);
        txt_title = findViewById(R.id.txt_title);
        recyclerView = findViewById(R.id.recyclerView_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        et_searchfood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    userLikeListAdapter.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Bundle bundle = getIntent().getExtras();


        if (bundle != null){
            String text = bundle.getString("id");

            list_id = new ArrayList<>();
            Query query = Usersfav.child(text);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list_id.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String id_key = snapshot.getKey();
                            list_id.add(id_key);
                        }
                        getAllList(list_id,"off");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
            list_id = new ArrayList<>();
            Query query = Usersfav.child(mUser.getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list_id.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String id_key = snapshot.getKey();
                            list_id.add(id_key);
                        }
                        getAllList(list_id,"on");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getAllList(List<String> list_id,String on) {
        ArrayList<Foodmenu> ff = new ArrayList<>();

        foodRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ff.clear();
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        Foodmenu foodmenu = document.toObject(Foodmenu.class);
                        for (String s : list_id ){
                            if (foodmenu.getDocumentId().equals(s)){
                                ff.add(foodmenu);
                            }
                        }
                    }
                    userLikeListAdapter = new UserLikeListAdapter(Food_UserActivity.this,ff,on);
                    recyclerView.setAdapter(userLikeListAdapter);
                    userLikeListAdapter.notifyDataSetChanged();

                    userLikeListAdapter.setOnItemClickListener(new UserLikeListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            Intent intent = new Intent(Food_UserActivity.this, FoodDetailActivity.class);
                            intent.putExtra("id", ff.get(position).getDocumentId());
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }
}
