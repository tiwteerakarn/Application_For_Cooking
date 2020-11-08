package com.example.cookingapp.Navigation;



import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.example.cookingapp.Adapter.BestMenuAdapter;
import com.example.cookingapp.Adapter.HomeAdapter;
import com.example.cookingapp.Adapter.RecyclerViewAdapter;
import com.example.cookingapp.FoodDetail.FoodDetailActivity;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Users;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    ArrayList<String> userkey = new ArrayList<>();
    ArrayList<String> userkey2 = new ArrayList<>();
    private TextView textView;
    private String vip;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mList = new ArrayList<>();
    LayoutAnimationController animationController;
    private HomeAdapter homeAdapter ,homeAdapter2;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("foodmenu");
    private List<Foodmenu> foodmenus,foodmenu2,foodmenus3;
    private RecyclerView recyclerView,recyclerView2,recyclerView3;
    private BestMenuAdapter bestMenuAdapter;
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mfollowing = mRootRef.child("following");
    private ArrayList<Users> userAccount;
    private DatabaseReference Usersfav = mRootRef.child("foodmenu_user");
    private LoopingViewPager loopView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    DatabaseReference myVip = mRootRef.child("vip_point");
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        textView = view.findViewById(R.id.txt_bestdeal_item);

        loopView = view.findViewById(R.id.loopView);
        foodmenus = new ArrayList<>();

        animationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setHasFixedSize(true);

        //LinearLayoutManager layoutManager3 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        //recyclerView3 = view.findViewById(R.id.recyclerView3);
        //recyclerView3.setLayoutManager(layoutManager3);
        //recyclerView3.setHasFixedSize(true);


        userAccount = new ArrayList<>();

        addpoppular();
        addlastmenu();
        adduser();
        //addMenufollower();








        return view;
    }

    private void addMenufollower() {

        List<String> mlist2 = new ArrayList<>();
        foodmenus3 = new ArrayList<>();

        mfollowing.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mlist2.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        mlist2.add(ds.getKey());
                    }
                    Query query = notebookRef;
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isComplete()) {
                                foodmenus3.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Foodmenu foodmenu = document.toObject(Foodmenu.class);
                                    for (String s : mlist2) {
                                        if (foodmenu.getUsername().equals(s))
                                            foodmenus3.add(foodmenu);
                                    }
                                }
                                homeAdapter = new HomeAdapter(foodmenus3,getContext());
                                recyclerView3.setAdapter(homeAdapter);
                                recyclerView3.setLayoutAnimation(animationController);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }


    private void adduser() {


        df.orderByChild("follower").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userAccount.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Users users = ds.getValue(Users.class);
                        userAccount.add(users);
                    }
                    Collections.sort(userAccount, Comparator.comparing(Users::getFollower).reversed());

                    final RecyclerViewAdapter adapter = new RecyclerViewAdapter(userAccount, getContext());
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutAnimation(animationController);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addlastmenu() {

        db.collection("foodmenu").orderBy("timestamp", Query.Direction.DESCENDING).limit(10).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        foodmenus.clear();
                        if (task.isSuccessful()) {
                            //Toast.makeText(getContext(),"ok",Toast.LENGTH_SHORT).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Foodmenu foodmenu = document.toObject(Foodmenu.class);
                                foodmenus.add(foodmenu);
                            }
                            bestMenuAdapter = new BestMenuAdapter(getContext(),foodmenus,true);
                            loopView.setAdapter(bestMenuAdapter);

                        } else {
                            //Toast.makeText(getActivity(),"no",Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }




                    }
                });

    }

    private void addpoppular() {
        //displayProgressDialog();
        foodmenu2 = new ArrayList<>();

        db.collection("foodmenu").orderBy("rating", Query.Direction.DESCENDING).limit(10).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        foodmenu2.clear();
                        if (task.isSuccessful()) {
                            //Toast.makeText(getContext(),"ok",Toast.LENGTH_SHORT).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Foodmenu foodmenu = document.toObject(Foodmenu.class);
                                foodmenu2.add(foodmenu);
                            }
                            homeAdapter2 = new HomeAdapter(foodmenu2,getContext());
                            recyclerView2.setAdapter(homeAdapter2);
                            recyclerView2.setLayoutAnimation(animationController);

                            homeAdapter2.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                                    intent.putExtra("id", foodmenu2.get(position).getDocumentId());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            //Toast.makeText(getActivity(),"no",Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        //pDialog.dismiss();
                    }
                });

    }


    @Override
    public void onResume() {
        super.onResume();
        loopView.resumeAutoScroll();
        addpoppular();
        //addMenufollower();
        adduser();

    }

    @Override
    public void onPause() {
        super.onPause();
        loopView.pauseAutoScroll();
    }
}
