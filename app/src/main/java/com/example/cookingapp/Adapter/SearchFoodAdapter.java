package com.example.cookingapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchFoodAdapter extends RecyclerView.Adapter<SearchFoodAdapter.HolderFilterFood>{
    Dialog custom_popup;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private Context context;
    public ArrayList<Foodmenu> foodmenu5  ;
    private FoodmenuAdapter.OnItemClickListener mListener;
    private ArrayList<String> key_filter;

    public SearchFoodAdapter(Context context, ArrayList<Foodmenu> foodmenu5, ArrayList<String> key_filter) {
        this.context = context;
        this.foodmenu5 = foodmenu5;
        this.key_filter = key_filter;
    }

    @NonNull
    @Override
    public HolderFilterFood onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search_menu,
                parent, false);
        custom_popup = new Dialog(context);
        return new HolderFilterFood(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFilterFood holder, int position) {
        holder.textViewTitle.setText(foodmenu5.get(position).getFoodname());

        holder.titletext.setText("วัตถุดิบ "+ foodmenu5.get(position).getIngredients().size()+" รายการ");
        if (foodmenu5.get(position).getRating() != null)
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
                Showpopup(foodmenu5.get(position).getIngredients());
            }
        });

    }

    public void Showpopup(List<String> ingredients) {

        List<String> list = new ArrayList<>(ingredients);

        custom_popup.setContentView(R.layout.custom_popup);
        //close_img = (ImageView) custom_popup.findViewById(R.id.btn_close);
        Button btn_yess = (Button) custom_popup.findViewById(R.id.btn_closeyes);
        TextView tx1 = (TextView) custom_popup.findViewById(R.id.txt1);
        TextView tx2 = (TextView) custom_popup.findViewById(R.id.txt2);

        StringBuilder yes = new StringBuilder("");
        StringBuilder no = new StringBuilder("");

        for (String s : key_filter){
            if (ingredients.contains(s)) {
                yes.append(s);
                yes.append("\n");
            }
        }

        for (String s : key_filter) {
            if (list.contains(s)) {
                list.remove(list.indexOf(s));
            }
        }

        for (String a : list){
            no.append(a);
            no.append("\n");
        }

        tx1.setText(yes);

        if (no.length() == 0){
            tx2.setText("-");
        }else {
            tx2.setText(no);
        }

        btn_yess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                custom_popup.dismiss();
            }
        });

        custom_popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_popup.show();
    }

    @Override
    public int getItemCount() {
        return foodmenu5.size();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(FoodmenuAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
    class HolderFilterFood extends RecyclerView.ViewHolder
    {

        TextView textViewTitle,titletext;
        TextView textViewDescription;
        ImageView image,over;
        RatingBar ratingBar;
        public HolderFilterFood(@NonNull View itemView,final FoodmenuAdapter.OnItemClickListener listener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.titlenamefood);
            textViewDescription = itemView.findViewById(R.id.titleuser);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            image = itemView.findViewById(R.id.food_photo);
            over = itemView.findViewById(R.id.overflow);
            titletext = itemView.findViewById(R.id.titletext);

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
