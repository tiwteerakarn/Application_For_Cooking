package com.example.cookingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.example.cookingapp.FoodDetail.FoodDetailActivity;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;


public class BestMenuAdapter extends LoopingPagerAdapter<Foodmenu> {
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private List<String> ingredients, directions;

    public BestMenuAdapter(Context context, List<Foodmenu> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.layout_bestdeal_item,container,false);
    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {

        ImageView imageView = (ImageView)convertView.findViewById(R.id.image_bestdeal);
        TextView textViewv = (TextView)convertView.findViewById(R.id.txt_bestdeal);
        TextView textViewName=(TextView)convertView.findViewById(R.id.txt_bestdeal_name);


        Glide.with(convertView).load(itemList.get(listPosition).getImage()).into(imageView);
        textViewv.setText(itemList.get(listPosition).getFoodname());


        df.child(itemList.get(listPosition).getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    textViewName.setText(users.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,""+itemList.get(listPosition).getFoodname(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, FoodDetailActivity.class);
                intent.putExtra("id", itemList.get(listPosition).getDocumentId());
                context.startActivity(intent);
            }
        });
    }
}
