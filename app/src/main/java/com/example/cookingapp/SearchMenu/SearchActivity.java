package com.example.cookingapp.SearchMenu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.cookingapp.Adapter.FilterFoodAdapter;
import com.example.cookingapp.Adapter.FoodmenuAdapter;
import com.example.cookingapp.Adapter.SearchFoodAdapter;
import com.example.cookingapp.FoodDetail.FoodDetailActivity;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchActivity extends AppCompatActivity {
    private SearchFoodAdapter filterFoodAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("foodmenu");
    private RecyclerView recyclerView;
    ArrayList<Foodmenu> foodmenuSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerView = findViewById(R.id.recyclerView_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        ArrayList<String> key_filter = getIntent().getStringArrayListExtra("ingredient");

        ArrayList<String> f2 = getIntent().getStringArrayListExtra("id");
        Toast.makeText(getApplicationContext(), "พบ: "+f2.size()+" รายการ", Toast.LENGTH_SHORT).show();

        setTitle("พบ: "+f2.size()+" รายการ");

        for (int i=0;i<f2.size();i++){
            notebookRef.document(f2.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {
                        final Foodmenu foodmenu = documentSnapshot.toObject(Foodmenu.class);
                        String id = documentSnapshot.getId();
                        foodmenu.setDocumentId(id);


                        foodmenuSearch.add(foodmenu);

                        Collections.sort(foodmenuSearch, Comparator.comparingDouble(Foodmenu::getRating).reversed());

                        filterFoodAdapter = new SearchFoodAdapter(SearchActivity.this,foodmenuSearch,key_filter);
                        recyclerView.setAdapter(filterFoodAdapter);

                        filterFoodAdapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {

                                Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                                intent.putExtra("id", foodmenuSearch.get(position).getDocumentId());
                                startActivity(intent);

                            }
                        });



                    } else {
                        Toast.makeText(getApplicationContext(), "ไม่พบเรายการ", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
}
