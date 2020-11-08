package com.example.cookingapp.Post;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Adapter.PostRecyclerAdapter;
import com.example.cookingapp.CreateRecipe.AddMapActivity;
import com.example.cookingapp.CreateRecipe.CreateRecipeActivity;
import com.example.cookingapp.Model.BlogPost;
import com.example.cookingapp.Model.FirebaseMethods;

import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.example.cookingapp.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostRecyclerAdapter usersAdapter;
    private List<BlogPost> userAccountSettings;
    private FirebaseMethods firebaseMethods ;
    private FirebaseDatabase database;

    private DatabaseReference myRef;
    private String vip ;
    private int point ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    List<String> uidList = new ArrayList<>();
    LoadingDialog loadingDialog ;
    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        loadingDialog = new LoadingDialog(getActivity());
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        userAccountSettings = new ArrayList<>();
        firebaseMethods = new FirebaseMethods(mUser.getUid().toString());
        recyclerView = view.findViewById(R.id.post_list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //addContent();

        checkVip();

        view.findViewById(R.id.fab1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),NewPostActivity.class);
                intent.putExtra("status","newpost");
                startActivity(intent);
            }
        });

        return view;
    }

    private void checkVip() {

        myRef.child("vip_point").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
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

    private void addContent() {
        displayProgressDialog();
   myRef.child("following").child(mAuth.getCurrentUser().getUid())
           .addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists()){
                   uidList.clear();
                   for (DataSnapshot ds : dataSnapshot.getChildren()) {
                       String idkey = ds.getKey();
                       uidList.add(idkey);
                       //getPhoto(ds);
                   }

                       Query query = myRef.child("post");
                       query.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                              if (dataSnapshot.exists()){
                                  userAccountSettings.clear();
                                  for (DataSnapshot ds : dataSnapshot.getChildren())
                                  {
                                      BlogPost blogPost = ds.getValue(BlogPost.class).withId(ds.getKey());
                                      for (int i=0;i<uidList.size();i++)
                                      {
                                          if (uidList.get(i).equals(blogPost.getUser_id()))
                                          {
                                              userAccountSettings.add(blogPost);
                                          }
                                      }

                                      usersAdapter = new PostRecyclerAdapter(getContext(), userAccountSettings);
                                      recyclerView.setAdapter(usersAdapter);
                                      usersAdapter.notifyDataSetChanged();
                                  }
                              }
                              loadingDialog.dissLoadingDialog();
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });

                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });

    }

    private void getPhoto(DataSnapshot ds) {

        Query query = myRef.child("post").orderByChild("user_id").equalTo(ds.getKey());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userAccountSettings.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String blogPostId = ds.getKey();

                        BlogPost photo = ds.getValue(BlogPost.class).withId(blogPostId);
                        userAccountSettings.add(photo);

                        usersAdapter = new PostRecyclerAdapter(getContext(), userAccountSettings);
                        recyclerView.setAdapter(usersAdapter);
                        usersAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        addContent();
    }
    private void displayProgressDialog() {
        loadingDialog.startLoadindDialog();

    }
}
