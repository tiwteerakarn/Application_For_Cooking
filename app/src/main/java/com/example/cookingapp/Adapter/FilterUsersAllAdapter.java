package com.example.cookingapp.Adapter;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Chats.ChatsActivity;
import com.example.cookingapp.Model.FirebaseMethods;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.example.cookingapp.User.Food_UserActivity;
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


public class FilterUsersAllAdapter extends RecyclerView.Adapter<FilterUsersAllAdapter.ViewHolder> implements Filterable {

    private List<Users> userAccountSettings ,usersList ;
    private FilterUsersAllAdapter.OnItemClickListener onItemClickListener;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mฺBlock = mRootRef.child("block");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    String mID = mUser.getUid();
    private FirebaseMethods firebaseMethods ;
    Dialog dialog;
    private int pos=0;
    private Context context;

    public FilterUsersAllAdapter(Context context, List<Users> userAccountSettings) {
        this.context=context;
        this.userAccountSettings = userAccountSettings;
        this.usersList = new ArrayList<>(userAccountSettings);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private Filter exampleFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Users> filterModel = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filterModel.addAll(usersList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Users item : usersList) {
                    if (item.getUsername().toLowerCase().contains(filterPattern)) {
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

            userAccountSettings.clear();
            userAccountSettings.addAll((ArrayList<Users>) results.values) ;
            notifyDataSetChanged();
        }
    };

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(FilterUsersAllAdapter.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item ,parent, false);
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_contact);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        firebaseMethods = new FirebaseMethods(mUser.getUid().toString());

        return new ViewHolder(view,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Users accountSettings = userAccountSettings.get(position);
        pos = position;

        DatabaseReference df = mฺBlock.child(mUser.getUid());
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    holder.img_block.setVisibility(View.GONE);

                } else {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.child("user_id").getValue().equals(accountSettings.getUser_id())) {

                            try {
                                holder.img_block.setVisibility(View.VISIBLE);
                            }catch (Exception e){

                            }

                            break;
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        DatabaseReference query = mfollowing.child(mUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    holder.img_check.setVisibility(View.GONE);

                } else {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.child("user_id").getValue().equals(accountSettings.getUser_id())) {

                            try {
                                holder.img_check.setVisibility(View.VISIBLE);
                            }catch (Exception e){

                            }

                            break;
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.display_name.setText(accountSettings.getUsername());
        try {
            Picasso.get().load(accountSettings.getProfile_photo()).placeholder(R.drawable.ic_user).into(holder.image);
        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return userAccountSettings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView display_name,img_check,img_block;
        ImageView image;
        boolean follow = false;
        boolean block =false;
        public ViewHolder(@NonNull View itemView,final FilterUsersAllAdapter.OnItemClickListener listener) {
            super(itemView);

            display_name = itemView.findViewById(R.id.titlename_users);
            image = itemView.findViewById(R.id.users_photo);
            img_check = itemView.findViewById(R.id.image_check);
            img_block = itemView.findViewById(R.id.image_block);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView name = (TextView)dialog.findViewById(R.id.name);
                    ImageView photo = (ImageView) dialog.findViewById(R.id.user_photo);
                    Button bt1 = (Button)dialog.findViewById(R.id.bt_fl);
                    Button bt2 = (Button)dialog.findViewById(R.id.bt_msg);
                    Button bt3 = (Button)dialog.findViewById(R.id.bt_block);
                    Button bt4 = (Button)dialog.findViewById(R.id.bt_foodlist);

                    bt4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, Food_UserActivity.class);
                            intent.putExtra("id",userAccountSettings.get(getAdapterPosition()).getUser_id());
                            context.startActivity(intent);
                            dialog.dismiss();
                        }
                    });

                    if (!follow){
                        bt1.setText("ติดตาม");
                    }

                    mfollowing.child(mID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    if (ds.child("user_id").getValue().equals(userAccountSettings.get(getAdapterPosition()).getUser_id())) {

                                        follow = true;
                                        bt1.setText("เลิกติดตาม");


                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    if (!block){
                        bt3.setText("บล็อค");
                    }

                    mฺBlock.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    if (ds.child("user_id").getValue().equals(userAccountSettings.get(getAdapterPosition()).getUser_id())) {

                                        block = true;
                                        bt3.setText("ปลดบล็อค");

                                        break;
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {


                        }
                    });

                    bt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context,bt1.getText().toString(),Toast.LENGTH_SHORT).show();
                            if (!follow){
                                follow = true;
                                setFollow("ติดตามแล้ว");
                                firebaseMethods.addFollowingAndFollowers(userAccountSettings.get(getAdapterPosition()).getUser_id());

                            }else {
                                follow = false;
                                setFollow("ติดตาม");
                                firebaseMethods.removeFollowingAndFollowers(userAccountSettings.get(getAdapterPosition()).getUser_id());
                            }

                            dialog.dismiss();
                            notifyDataSetChanged();

                        }
                    });
                    bt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (follow) {
                                Intent intent = new Intent(context, ChatsActivity.class);
                                intent.putExtra("hisid", userAccountSettings.get(getAdapterPosition()).getUser_id());
                                intent.putExtra("name", userAccountSettings.get(getAdapterPosition()).getUsername());
                                intent.putExtra("img", userAccountSettings.get(getAdapterPosition()).getProfile_photo());
                                context.startActivity(intent);

                            }else {
                                Toast.makeText(context,"คุณยังไม่ได้ติดตามผู้ใช้งาน",Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });

                    bt3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!block){
                                block = true;
                                setBlock("ปลดบล็อค");
                                firebaseMethods.AddblockUser(userAccountSettings.get(getAdapterPosition()).getUser_id());

                            }else {
                                block = false;
                                setBlock("บล็อค");
                                firebaseMethods.removeblockUser(userAccountSettings.get(getAdapterPosition()).getUser_id());
                            }

                            dialog.dismiss();
                            notifyDataSetChanged();

                        }

                    });

                    name.setText(userAccountSettings.get(getAdapterPosition()).getUsername());

                    try {
                        Picasso.get().load(userAccountSettings.get(getAdapterPosition()).getProfile_photo()).placeholder(R.drawable.ic_user).into(photo);
                    } catch (Exception e) {

                    }
                    dialog.show();
                }

            });

        }

        private void setFollow(String s) {
            try {
                if (s.equals("ติดตามแล้ว")) img_check.setVisibility(View.VISIBLE);
                else img_check.setVisibility(View.GONE);
            }catch (Exception e){

            }
        }

        private void setBlock(String s) {
            try {
                if (s.equals("ปลดบล็อค")) img_block.setVisibility(View.VISIBLE);
                else img_block.setVisibility(View.GONE);
            }catch (Exception e){

            }

        }
        }
    }

