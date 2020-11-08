package com.example.cookingapp.FoodDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cookingapp.Adapter.FoodmenuAdapter;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FoodMenuActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Foods");
    private RecyclerView recyclerView;
    private FoodmenuAdapter adapter;
    TextView txt_title_menu;
    private List<String> ingredients, directions;
    private Toolbar mToolbar;
    private String foodtitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_menu);

        //txt_title_menu = findViewById(R.id.txt_food_menu);

        Intent intent = getIntent();
        String key = intent.getStringExtra("menu");
        foodtitle = key;
        mToolbar = findViewById(R.id.toolbar5);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(foodtitle);

       // txt_title_menu.setText(key);

        recyclerView = findViewById(R.id.recyclerView5);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        Query query = notebookRef.whereEqualTo("menu",key);

        final FirestoreRecyclerOptions<Foodmenu> options5 = new FirestoreRecyclerOptions.Builder<Foodmenu>()
                .setQuery(query, Foodmenu.class)
                .build();

        adapter = new FoodmenuAdapter(options5);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Foodmenu foodmenu = adapter.getItem(position);
                String id = options5.getSnapshots().getSnapshot(position).getId();
                foodmenu.setDocumentId(id);

                ingredients = new ArrayList<>();
                directions = new ArrayList<>();
                for (String s : foodmenu.getIngredients()) {
                    ingredients.add(s);
                }
                for (String s : foodmenu.getDirections()) {
                    directions.add(s);
                }

                Intent intent = new Intent(FoodMenuActivity.this, FoodDetailActivity.class);
                intent.putStringArrayListExtra("ingredients", (ArrayList<String>) ingredients);
                intent.putStringArrayListExtra("directions", (ArrayList<String>) directions);
                intent.putExtra("foods", options5.getSnapshots().get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void filter(String newText) {

        Query query = notebookRef.whereEqualTo("menu",foodtitle).orderBy("foodname").startAt(newText).endAt(newText + "\uf8ff");

        final FirestoreRecyclerOptions<Foodmenu> options = new FirestoreRecyclerOptions.Builder<Foodmenu>()
                .setQuery(query, Foodmenu.class)
                .build();
        adapter = new FoodmenuAdapter(options);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
