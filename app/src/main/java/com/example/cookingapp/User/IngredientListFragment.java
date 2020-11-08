package com.example.cookingapp.User;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.FoodDetail.IngredientMapsActivity;
import com.example.cookingapp.Model.AddLatLngModel;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.Ingredientv2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class IngredientListFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Ingredientv2> ingredient_list = new ArrayList<>();
    private RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Maps");
    public IngredientListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ingredient_list, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_ingredientlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        filter("All");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        filter("All");
    }

    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {


        private List<Ingredientv2> menu;
        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            onItemClickListener = listener;
        }

        public CustomAdapter(List<Ingredientv2> menu) {
            this.menu = menu;
        }


        @NonNull
        @Override
        public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_ingredientlist, parent, false);
            MyViewHolder holder = new MyViewHolder(view,onItemClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, final int position) {
            holder.food.setText((position+1)+". " + menu.get(position).getName());

        }

        @Override
        public int getItemCount() {
            return menu.size();
        }

        public interface OnItemClickListener {
            void onItemClick(int position);
        }
        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView food ;
            public MyViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
                super(itemView);
                food = itemView.findViewById(R.id.ingredient_name);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    filter(query.trim());
                }else {
                    filter("All");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    filter(query.trim());
                }else {
                    filter("All");
                }
                return false;
            }
        });
    }


    private void filter(String trim) {
        List<Ingredientv2> uu = new ArrayList<>();

        Query query =  db.collection("ingredient").orderBy("name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()){
                    uu.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        Ingredientv2 ingredientv2 = queryDocumentSnapshot.toObject(Ingredientv2.class);
                        if (trim.equals("All")){
                            uu.add(ingredientv2);
                        }else {
                            if (ingredientv2.getName().toUpperCase().contains(trim.toUpperCase())) {
                                uu.add(ingredientv2);
                            }
                        }

                    }

                    customAdapter = new CustomAdapter(uu);
                    customAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(customAdapter);

                    customAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {

                            myRef.child(uu.get(position).getId()).child("LatLng").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            AddLatLngModel add = ds.getValue(AddLatLngModel.class);
                                            if (add.getName().equals(uu.get(position).getName())) {
                                                Intent intent = new Intent(getContext(), IngredientMapsActivity.class);
                                                intent.putExtra("id", add.getId_ingredient());
                                                startActivity(intent);
                                                break;
                                            }
                                        }
                                    }else {
                                        Toast.makeText(getActivity(),"ไม่พบ",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            }
        });
    }

}
