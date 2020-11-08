package com.example.cookingapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
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

public class AdminUsersAdapter  extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> implements Filterable {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mfollowing = mRootRef.child("following");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference Usersfav = mRootRef.child("foodmenu_user");
    private DatabaseReference mฺBanned = mRootRef.child("banned");

    private List<Users> userAccount ,usersList ;
    private Context context;

    public AdminUsersAdapter(List<Users> userAccount, Context context) {
        this.userAccount = userAccount;
        this.usersList = new ArrayList<>(userAccount);
        this.context = context;
    }

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
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

            userAccount.clear();
            userAccount.addAll((ArrayList<Users>) results.values) ;
            notifyDataSetChanged();
        }
    };


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_user_row ,parent, false);

        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Users account = userAccount.get(position);

        holder.display_name.setText(account.getUsername());

        if (account.getLogintime() != null) {
            holder.loginlast.setText("เข้าสู่ระบบล่าสุด: " + account.getLogintime());
        }else {
            holder.loginlast.setText("เข้าสู่ระบบล่าสุด: -");
        }

        try {
            Picasso.get().load(account.getProfile_photo()).placeholder(R.drawable.ic_user).into(holder.image);
        }catch (Exception e){

        }

        Usersfav.child(account.getUser_id()+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                        long count= dataSnapshot.getChildrenCount();
                    if (count != 0 && account.getFollower() != 0){
                        holder.follow.setText("ผู้ติดตาม: "+account.getFollower() +" สูตรอาหาร: "+count);
                    }else {
                        holder.follow.setText("ผู้ติดตาม: "+0 +" สูตรอาหาร: "+0);
                    }

                }else {
                    holder.follow.setText("ผู้ติดตาม: "+0 +" สูตรอาหาร: "+0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mVip.child(account.getUser_id()+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    VipPointModel vipPointModel1 = dataSnapshot.getValue(VipPointModel.class);
                    holder.point.setText("point: "+vipPointModel1.getPoint());
                    if (vipPointModel1.getVip().equals("vip1")){
                        holder.vip.setVisibility(View.VISIBLE);
                    }else {
                        holder.vip.setVisibility(View.GONE);;
                    }

                }else {
                    holder.point.setText("point: "+2);
                    VipPointModel vipPointModel= new VipPointModel("vip0",2);
                    mVip.child(account.getUser_id()+"").setValue(vipPointModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_admin_user);
                dialog.setCancelable(true);

                SwitchMaterial swvip = (SwitchMaterial) dialog.findViewById(R.id.txt_switchvip);
                SwitchMaterial swbanned = (SwitchMaterial) dialog.findViewById(R.id.txt_switchbanned);
                EditText etpoint = (EditText) dialog.findViewById(R.id.setpoint);
                TextView name = (TextView) dialog.findViewById(R.id.textnameuser);

                name.setText(account.getUsername().toString());


                mVip.child(account.getUser_id()+"").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            VipPointModel vipPointModel1 = dataSnapshot.getValue(VipPointModel.class);
                            etpoint.setText(""+vipPointModel1.getPoint());

                           if (vipPointModel1.getVip().equals("vip1") || vipPointModel1.getVip().equals("vip2") ){
                               swvip.setChecked(true);
                           }else {
                               swvip.setChecked(false);
                           }
                        }else {
                            etpoint.setText(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mฺBanned.child(account.getUser_id()+"").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            swbanned.setChecked(true);
                        }else {
                            swbanned.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                Button button1 = (Button)dialog.findViewById(R.id.button1);
                button1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        mVip.child(account.getUser_id()+"").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    VipPointModel vipPointModel1 = new VipPointModel();
                                    if (swvip.isChecked()){
                                        vipPointModel1.setVip("vip1");
                                        vipPointModel1.setPoint(Integer.parseInt(etpoint.getText().toString()));
                                        mVip.child(account.getUser_id()).setValue(vipPointModel1);
                                    }else {
                                        vipPointModel1.setVip("vip0");
                                        vipPointModel1.setPoint(Integer.parseInt(etpoint.getText().toString()));
                                        mVip.child(account.getUser_id()).setValue(vipPointModel1);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        if (swbanned.isChecked()) {
                            mฺBanned.child(account.getUser_id()).child("id").setValue(account.getUser_id());
                        }else {
                            mฺBanned.child(account.getUser_id()).removeValue();
                        }

                        Toast.makeText(context
                                , "บันทึกสำเร็จ" , Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                Button button2 = (Button)dialog.findViewById(R.id.button2);
                button2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });


                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userAccount.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView display_name,follow,point,loginlast,vip;
        ImageView image;

        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.food_photo);
            display_name = itemView.findViewById(R.id.titlename_user);
            follow = itemView.findViewById(R.id.title1);
            point = itemView.findViewById(R.id.title2);
            loginlast = itemView.findViewById(R.id.title3);
            vip = itemView.findViewById(R.id.titlevip);

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
