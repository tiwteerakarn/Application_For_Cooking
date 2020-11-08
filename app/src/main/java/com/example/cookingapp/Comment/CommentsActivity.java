package com.example.cookingapp.Comment;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Adapter.CommentsRecyclerAdapter;
import com.example.cookingapp.Model.Comments;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity implements UtilityInterface{

    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn;
    private boolean isCommentAdded = false;
    private long startLimit =-1;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    private FirebaseDatabase database;

    private DatabaseReference myRef;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    private String blog_post_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();

        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setAdapter(commentsRecyclerAdapter);

        myRef.child("post");
        myRef.child(blog_post_id);
        myRef.child("Comments");

        ShowComments();
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String comment_message = comment_field.getText().toString();
                String dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS", Locale.ENGLISH).format(Calendar.getInstance().getTime());

                if (comment_message.length() > 0) {
                    isCommentAdded = true;
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_message);
                    commentsMap.put("user_id", mUser.getUid());
                    commentsMap.put("timestamp", dateAdded);

                    myRef.child("post").child(blog_post_id).child("comments").push().setValue(commentsMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {

                                        Toast.makeText(CommentsActivity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    } else {

                                        comment_field.setText("");

                                    }

                                }
                            });


                }else {
                    Toast.makeText(CommentsActivity.this,"กรุณาใส่ข้อความ",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void ShowComments() {

        Query query = myRef.child("post").child(blog_post_id).child("comments");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long  commentsLength = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (isCommentAdded && commentsLength == dataSnapshot.getChildrenCount() - 1) {
                        Comments photo = ds.getValue(Comments.class);
                        commentsList.add(0,photo);
                        comment_list.smoothScrollToPosition(0);
                        isCommentAdded = false;

                    }
                    else if (!isCommentAdded&&commentsLength <= 20 && commentsLength > startLimit) {
                        Comments photo = ds.getValue(Comments.class);
                        commentsList.add(photo);
                    }
                    commentsLength++;
                }
                startLimit = 20;
                commentsRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void loadMore() {
        ShowComments();
    }
}
