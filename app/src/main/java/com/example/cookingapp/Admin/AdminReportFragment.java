package com.example.cookingapp.Admin;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cookingapp.Adapter.AdminReportAdapter;
import com.example.cookingapp.Adapter.FoodmenuAdapter;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminReportFragment extends Fragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference foodRef = db.collection("foodmenu");
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("report");
    private RecyclerView recyclerView;
    private List<Foodmenu> foodmenus = new ArrayList<>();
    private AdminReportAdapter filterFoodAdapter;
    private ArrayList<Report_model> report_models = new ArrayList<>();


    public AdminReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_report, container, false);
        recyclerView = view.findViewById(R.id.recyclerView_report);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setHasOptionsMenu(true);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        filterFood();
    }

    private void filterFood() {

        reportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    report_models.clear();
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        Report_model reportModel = ds.getValue(Report_model.class);
                        reportModel.setKey(ds.getKey());
                        report_models.add(reportModel);
                    }
                    filterFoodAdapter = new AdminReportAdapter(getContext(),report_models,"on");
                    recyclerView.setAdapter(filterFoodAdapter);
                    filterFoodAdapter.notifyDataSetChanged();

                    filterFoodAdapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {

                            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                            intent.putExtra("id", report_models.get(position).id_food);
                            startActivity(intent);

                        }
                    });


                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            int pos = viewHolder.getAdapterPosition();
                            //ff.remove(pos);


                            reportRef.child(report_models.get(pos).getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        reportRef.child(report_models.get(pos).getKey()).removeValue();
                                        filterFoodAdapter.setDeleteListener(pos);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            filterFoodAdapter.notifyDataSetChanged();
                        }
                    });

                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addFood(ArrayList<Report_model> report_models) {
        List<Foodmenu> ff = new ArrayList<>();
        List<String> keyy = new ArrayList<>();

        foodRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ff.clear();
                    keyy.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Foodmenu foodmenu = document.toObject(Foodmenu.class);
                        for (int i=0 ;i<report_models.size();i++ ){
                            if (foodmenu.getDocumentId().equals(report_models.get(i).getId_food())){
                                ff.add(foodmenu);
                                keyy.add(report_models.get(i).getKey());
                            }
                        }
                    }



                    filterFoodAdapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {

                            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                            intent.putExtra("id", ff.get(position).getDocumentId());
                            startActivity(intent);

                        }
                    });

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            int pos = viewHolder.getAdapterPosition();
                            //ff.remove(pos);


                            reportRef.child(keyy.get(pos)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        reportRef.child(keyy.get(pos)).removeValue();
                                        filterFoodAdapter.setDeleteListener(pos);
                                        keyy.remove(pos);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            filterFoodAdapter.notifyDataSetChanged();
                        }
                    });

                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }
            }
        });
    }
}
