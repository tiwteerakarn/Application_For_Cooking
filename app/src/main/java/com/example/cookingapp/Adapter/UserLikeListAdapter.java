package com.example.cookingapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserLikeListAdapter extends RecyclerView.Adapter<UserLikeListAdapter.HolderFilterFood> implements Filterable {
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference foodRef = db.collection("foodmenu");
    private DatabaseReference Usersfood = FirebaseDatabase.getInstance().getReference("foodmenu");
    private DatabaseReference Usersfav = FirebaseDatabase.getInstance().getReference("favorite_user");
    private DatabaseReference mfav = FirebaseDatabase.getInstance().getReference("favorite");
    private DatabaseReference favorite_list = FirebaseDatabase.getInstance().getReference("favorite_list");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference foodmenu_user = FirebaseDatabase.getInstance().getReference("foodmenu_user");
    private Context context;
    public ArrayList<Foodmenu> foodmenu5 , filterList ;
    private String stat;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public UserLikeListAdapter(Context context, ArrayList<Foodmenu> foodmenu5, String on) {
        this.context = context;
        this.foodmenu5 = foodmenu5;
        this.filterList = new ArrayList<>(foodmenu5);
        this.stat = on;
    }

    @NonNull
    @Override
    public HolderFilterFood onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_like_list_item,
                parent, false);

        return new HolderFilterFood(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFilterFood holder, int position) {
        holder.textViewTitle.setText(foodmenu5.get(position).getFoodname());

        if (stat.equals("on")){
            holder.over.setVisibility(View.VISIBLE);
        }else {
            holder.over.setVisibility(View.INVISIBLE);
        }

        holder.ratingBar.setRating(foodmenu5.get(position).getRating().floatValue());
        df.child(foodmenu5.get(position).getUsername()).addValueEventListener(new ValueEventListener() {
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
        try {
            Picasso.get().load(foodmenu5.get(position).getImage()).placeholder(R.drawable.ic_home).into(holder.image);
        }catch (Exception e){

        }

        holder.over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(context);
                builder.setMessage("ยืนยันการลบ");

                builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        foodRef.document(foodmenu5.get(position).getDocumentId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Usersfood.child(foodmenu5.get(position).getDocumentId()).removeValue();
                                Usersfav.child(mUser.getUid()).child(foodmenu5.get(position).getDocumentId()).removeValue();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(foodmenu5.get(position).getImage());
                                storageReference.delete();
                                mfav.child(foodmenu5.get(position).getDocumentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                                String key = ds.getKey();
                                                favorite_list.child(foodmenu5.get(position).getDocumentId()).removeValue();
                                            }
                                        }
                                        mfav.child(foodmenu5.get(position).getDocumentId()).removeValue();
                                        foodmenu_user.child(mUser.getUid()).child(foodmenu5.get(position).getDocumentId()).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Toast.makeText(context,"ลบแล้ว",Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                });
                builder.setPositiveButton("ยกเเลิก", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodmenu5.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    class HolderFilterFood extends RecyclerView.ViewHolder
    {
        TextView textViewTitle;
        TextView textViewDescription;
        RatingBar ratingBar;
        ImageView image,over;

        public HolderFilterFood(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.titlenamefood);
            textViewDescription = itemView.findViewById(R.id.titleuser);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            image = itemView.findViewById(R.id.food_photo);
            over = itemView.findViewById(R.id.overflow);

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
    private Filter exampleFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Foodmenu> filterModel = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filterModel.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Foodmenu item : filterList) {
                    if (item.getFoodname().toLowerCase().contains(filterPattern)) {
                        filterModel.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterModel;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            foodmenu5.clear();
            foodmenu5.addAll((ArrayList<Foodmenu>) results.values) ;
            notifyDataSetChanged();
        }
    };
}
