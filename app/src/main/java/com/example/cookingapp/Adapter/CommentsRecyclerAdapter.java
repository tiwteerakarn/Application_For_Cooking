package com.example.cookingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.Comments;
import com.example.cookingapp.Model.Users;

import com.example.cookingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUser = mRootRef.child("users");
    private List<Users> userAccount;

    public CommentsRecyclerAdapter(List<Comments> commentsList){

        this.commentsList = commentsList;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, final int position) {
        String commentID = commentsList.get(position).getUser_id();
        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

        userAccount = new ArrayList<>();

       mUser.child(commentID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                  Users users = dataSnapshot.getValue(Users.class);
                   if (users.getUser_id().equals(commentID)) {
                       userAccount.add(0,users);
                   }

                   holder.setUser_name(userAccount.get(0).getUsername());
                   try {
                       Picasso.get().load(userAccount.get(0).getProfile_photo()).placeholder(R.drawable.ic_user).into(holder.blogImageuser);
                   }catch (Exception ee){}

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView comment_message,username;
        private ImageView blogImageuser;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogImageuser=itemView.findViewById(R.id.comment_image);
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);


        }

        public void setUser_name(String name){
            username=itemView.findViewById(R.id.comment_username);
            username.setText(name);
        }

    }

}
