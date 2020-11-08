package com.example.cookingapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Admin.Report_model;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.Users;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.HolderFilterFood>{
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    DatabaseReference df = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference mVip = mRootRef.child("vip_point");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference foodRef = db.collection("foodmenu");
    private DatabaseReference Usersfood = FirebaseDatabase.getInstance().getReference("foodmenu");
    private DatabaseReference Usersfav = FirebaseDatabase.getInstance().getReference("favorite_user");
    private DatabaseReference mfav = FirebaseDatabase.getInstance().getReference("favorite");
    private DatabaseReference favorite_list = FirebaseDatabase.getInstance().getReference("favorite_list");
    private DatabaseReference foodmenu_user = FirebaseDatabase.getInstance().getReference("foodmenu_user");
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("report");
    private FoodListener foodListener;
    private Context context;
    public Foodmenu foodmenu5  ;
    private List<String> key;
    private List<Report_model> reportModels;

    private FoodmenuAdapter.OnItemClickListener mListener;
    String s ="";
    private String stat;

    public AdminReportAdapter(Context context, ArrayList<Report_model> report_models, String on) {
        this.context = context;
        this.reportModels = report_models;
        this.stat = on;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(FoodmenuAdapter.OnItemClickListener listener) {
        mListener = listener;
    }



    @NonNull
    @Override
    public HolderFilterFood onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report,
                parent, false);

        return new HolderFilterFood(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFilterFood holder, int position) {


        foodRef.document(reportModels.get(position).getId_food()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    Foodmenu foodmenu = documentSnapshot.toObject(Foodmenu.class);
                    foodmenu5 = foodmenu;
                    holder.textViewTitle.setText(foodmenu.getFoodname());

                    if (foodmenu.getRating() != null) {
                        holder.ratingBar.setRating(foodmenu.getRating().floatValue());
                    }

                    df.child(foodmenu.getUsername()).addValueEventListener(new ValueEventListener() {
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
                        Picasso.get().load(foodmenu.getImage()).placeholder(R.drawable.ic_home).into(holder.image);
                    }catch (Exception e){

                    }






                }
            }
        });

        if (stat.equals("on")){
            holder.report.setVisibility(View.VISIBLE);
        }else {
            holder.report.setVisibility(View.INVISIBLE);
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

                        foodRef.document(reportModels.get(position).getId_food()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Usersfood.child(reportModels.get(position).getId_food()).removeValue();
                                //Usersfav.child(mUser.getUid()).child(foodmenu5.get(position).getDocumentId()).removeValue();
                                //StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(foodmenu5.get(position).getImage());
                                //storageReference.delete();
                                mfav.child(reportModels.get(position).getId_food()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                                String key = ds.getKey();
                                                favorite_list.child(reportModels.get(position).getId_food()).removeValue();
                                            }
                                        }
                                        mfav.child(reportModels.get(position).getId_food()).removeValue();
                                        foodmenu_user.child(foodmenu5.getUsername()).child(foodmenu5.getDocumentId()).removeValue();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            for (DataSnapshot ds:dataSnapshot.getChildren()) {
                                                Report_model report_model = ds.getValue(Report_model.class);
                                                if (report_model.getId_food().equals(reportModels.get(position).getId_food())) {
                                                    reportRef.child(ds.getKey()).removeValue();
                                                }
                                            }
                                        }
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

        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                df.child(reportModels.get(position).getId_user()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Users users = dataSnapshot.getValue(Users.class);
                            if (users.getUsername() != null){
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                builder.setMessage("ข้อความ: "+ reportModels.get(position).getDetail());

                                builder.setTitle("รายงานจาก: "+ users.getUsername());

                                builder.setPositiveButton("ปิด", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });



    }

    @Override
    public int getItemCount() {
        return reportModels.size();
    }

    class HolderFilterFood extends RecyclerView.ViewHolder
    {
        TextView textViewTitle;
        TextView textViewDescription;
        ImageView image,over,report;
        RatingBar ratingBar;
        public HolderFilterFood(@NonNull View itemView, FoodmenuAdapter.OnItemClickListener mListener) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.titlenamefood);
            textViewDescription = itemView.findViewById(R.id.titleuser);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            image = itemView.findViewById(R.id.food_photo);
            over = itemView.findViewById(R.id.overflow);
            report = itemView.findViewById(R.id.report);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);

                        }
                    }

                }
            });

        }
    }



    public void setDeleteListener(int i) {
        if (foodmenu5 != null)
        reportModels.remove(i);
        notifyDataSetChanged();
    }



    public interface FoodListener {
        void onDeleteDirection(int position);
    }
}
