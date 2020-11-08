package com.example.cookingapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.Users;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdminVipAdapter extends RecyclerView.Adapter<AdminVipAdapter.ViewHolder> implements Filterable {
    private List<Users> userAccount ,usersList ;
    private Context context;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference Usersfav = mRootRef.child("foodmenu_user");
    private DatabaseReference mVip = mRootRef.child("vip_point");

    public AdminVipAdapter(List<Users> userAccount, Context context) {
        this.userAccount = userAccount;
        this.usersList = new ArrayList<>(userAccount);
        this.context = context;
    }

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
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

        Query query = Usersfav.child(account.getUser_id());
        query.addValueEventListener(new ValueEventListener() {
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("ตั้งสถานะ VIP");
                dialog.setIcon(R.drawable.ic_user);
                dialog.setCancelable(true);
                dialog.setMessage("คุณต้องการเปลี่ยนระดับผู้ใช้นี้ ?");
                dialog.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       mVip.child(account.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if (dataSnapshot.exists()){
                                   VipPointModel vipPointModel1 = new VipPointModel();
                                   vipPointModel1.setPoint(5);
                                   vipPointModel1.setVip("vip1");
                                   mVip.child(account.getUser_id()).setValue(vipPointModel1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           Toast.makeText(context,"สำเร็จ",Toast.LENGTH_SHORT).show();
                                           dialog.dismiss();
                                           notifyDataSetChanged();
                                       }
                                   });
                               }
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });
                    }
                });

                dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
        TextView display_name,follow,loginlast,vip;
        ImageView image;
        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            image = itemView.findViewById(R.id.food_photo);
            display_name = itemView.findViewById(R.id.titlename_user);
            follow = itemView.findViewById(R.id.title1);
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
