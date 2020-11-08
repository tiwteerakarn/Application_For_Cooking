package com.example.cookingapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Users on 2/12/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Users> users = new ArrayList<>();

    private Context mContext;

    public RecyclerViewAdapter(ArrayList<Users> mNames, Context mContext) {
        this.users = mNames;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_popular, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        mVip.child(users.get(position).getUser_id()+"").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String vip = dataSnapshot.child("vip").getValue(String.class);
                    if (vip.equals("vip1")){
                        holder.mVip.setVisibility(View.VISIBLE);
                    }else {
                        holder.mVip.setVisibility(View.GONE);
                    }

                }else {
                    holder.mVip.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        try {
            Picasso.get().load(users.get(position).getProfile_photo()).placeholder(R.drawable.ic_user).into(holder.image);
        }catch (Exception e){

        }


        holder.name.setText(users.get(position).getUsername());
        holder.follower.setText("ผู้ติดตาม "+String.valueOf(users.get(position).getFollower()));

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on an image: " + users.get(position).getUsername());
                Toast.makeText(mContext, users.get(position).getUsername(), Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(mContext, FoodMenuActivity.class);
                //intent.putExtra("menu",mNames.get(position));
               // mContext.startActivity(intent);



            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name,follower,mVip;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
            follower = itemView.findViewById(R.id.txt_follower);
            mVip = itemView.findViewById(R.id.vip);
        }
    }
}
