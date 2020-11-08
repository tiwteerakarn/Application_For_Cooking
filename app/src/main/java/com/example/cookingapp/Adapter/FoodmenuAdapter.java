package com.example.cookingapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodmenuAdapter extends FirestoreRecyclerAdapter<Foodmenu, FoodmenuAdapter.NoteHolder> {
    private OnItemClickListener mListener;
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FoodmenuAdapter(@NonNull FirestoreRecyclerOptions<Foodmenu> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int i, @NonNull Foodmenu model) {

        holder.textViewTitle.setText(model.getFoodname());
        holder.ratingBar.setRating(model.getRating().floatValue());
        df.child(model.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    holder.textViewDescription.setText(users.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //holder.textViewPriority.setText("ถูกใจ "+ String.valueOf(model.getLike()));
        try {
            Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_home).into(holder.image);
        }catch (Exception e){

        }


    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item,
                parent, false);
        return new NoteHolder(v,mListener);
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        RatingBar ratingBar;
        ImageView image;

        public NoteHolder(View itemView,final OnItemClickListener listener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.titlenamefood);
            textViewDescription = itemView.findViewById(R.id.titleuser);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            image = itemView.findViewById(R.id.food_photo);

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
