package com.example.cookingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingapp.Model.Foodmenu;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>{
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private List<Foodmenu> mfood ;
    private Context mContext;
    private HomeAdapter.OnItemClickListener mListener;
    public HomeAdapter(List<Foodmenu> users, Context mContext) {
        this.mfood = users;
        this.mContext = mContext;
    }
    public void setOnItemClickListener(HomeAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_hit, parent, false);
        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext)
                .asBitmap()
                .load(mfood.get(position).getImage())
                .into(holder.image);

        holder.name.setText(mfood.get(position).getFoodname());

        if (mfood.get(position).getRating() != null){
            holder.ratingBar.setRating(mfood.get(position).getRating().floatValue());
        }else {
            holder.ratingBar.setRating(0);
        }

        df.child(mfood.get(position).getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    holder.follower.setText(users.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mfood.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name,follower;
        RatingBar ratingBar;
        public ViewHolder(@NonNull View itemView,final HomeAdapter.OnItemClickListener listener) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
            follower = itemView.findViewById(R.id.txt_follower);
            ratingBar = itemView.findViewById(R.id.ratingbar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);

                        }
                    }

                }
            });
        }
    }
}
