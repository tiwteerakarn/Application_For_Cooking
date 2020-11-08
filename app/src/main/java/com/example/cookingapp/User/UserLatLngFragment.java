package com.example.cookingapp.User;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Model.AddLatLngModel;
import com.example.cookingapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserLatLngFragment extends Fragment {
    private RecyclerView recyclerView;
    static DatabaseReference df = FirebaseDatabase.getInstance().getReference("user_LatLng");
    DatabaseReference df2 = FirebaseDatabase.getInstance().getReference("Maps");
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser mUser = mAuth.getCurrentUser();
    List<String> list = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    UserLattLngAdapter userLattLngAdapter;
    List<AddLatLngModel> ingredientv2s = new ArrayList<>();
    public UserLatLngFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_lat_lng, container, false);
        recyclerView = view.findViewById(R.id.recyclerView_userslatlng);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        addLatLng();

        return view;

    }

    private void addLatLng(){

        Query query = df.child(mUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    list.clear();
                    list2.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String id = ds.child("idname").getValue(String.class);
                        String id2 = ds.child("idlatlng").getValue(String.class);
                        list.add(id);
                        list2.add(id2);

                    }

                    userLattLngAdapter = new UserLattLngAdapter(list,list2,getContext());
                    recyclerView.setAdapter(userLattLngAdapter);
                    userLattLngAdapter.notifyDataSetChanged();

                    // Query query = df2.child(id);


                    userLattLngAdapter.setOnItemClickListener(new UserLattLngAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static class UserLattLngAdapter extends RecyclerView.Adapter<UserLattLngAdapter.MyViewHolder> {
        DatabaseReference df2 = FirebaseDatabase.getInstance().getReference("Maps");

        private List<String> menu;
        private List<String> menu2;
        static List<AddLatLngModel> addLatLngModels = new ArrayList<>();
        private Context context;
        private UserLattLngAdapter.OnItemClickListener mListener;
        public UserLattLngAdapter(List<String> menu,List<String> menu2, Context context) {
            this.menu = menu;
            this.menu2 = menu2;
            this.context = context;
        }
        public void setOnItemClickListener(UserLattLngAdapter.OnItemClickListener listener) {
            mListener = listener;
        }
        public interface OnItemClickListener {
            void onItemClick(int position);
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_latlng, parent, false);
            MyViewHolder holder = new MyViewHolder(view,mListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            df2.child(menu.get(position)).child("LatLng").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        addLatLngModels.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                           if (ds.getKey().equals(menu2.get(position))){
                               AddLatLngModel addLatLngModel = ds.getValue(AddLatLngModel.class);
                               addLatLngModels.add(addLatLngModel);
                               holder.tx1.setText(addLatLngModel.getTitle());
                               holder.tx2.setText(addLatLngModel.getName());
                               holder.tx3.setText("พิกัด :   "+addLatLngModel.getLat() + " : " +addLatLngModel.getLng());
                               break;
                           }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });





        }

        @Override
        public int getItemCount() {
            return menu.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tx1,tx2,tx3;
            private ImageView overflow;
            public MyViewHolder(@NonNull View itemView,final UserLattLngAdapter.OnItemClickListener listener) {
                super(itemView);
                tx1 = itemView.findViewById(R.id.hisname);
                tx2 = itemView.findViewById(R.id.histext);
                tx3 = itemView.findViewById(R.id.hislatlng);
                overflow = itemView.findViewById(R.id.overflow);

                overflow.setOnClickListener(this::showPopupMenu);
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

            private void showPopupMenu(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.inflate(R.menu.grid_popup_menu);
                popup.setOnMenuItemClickListener(
                        new MyMenuItemClickListener(menu.get(getAdapterPosition()),menu2.get(getAdapterPosition())));
                popup.show();
            }
            class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

                public String id_ingredient,id_latlng;

                public MyMenuItemClickListener( String id_ingredient, String id_latlng) {
                    this.id_ingredient = id_ingredient;
                    this.id_latlng = id_latlng;
                }

                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {

                        case R.id.action_delete_recipe:

                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(context);
                            builder.setMessage("ยืนยันการลบ");

                            builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    df2.child(id_ingredient).child("LatLng").child(id_latlng).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            df.child(mUser.getUid()).child(id_latlng).removeValue();
                                            Toast.makeText(context,"ลบ "+id_ingredient+" สำเร็จ",Toast.LENGTH_SHORT).show();
                                            notifyDataSetChanged();
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



                            return true;
                        default:
                    }
                    return false;
                }
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLatLng();
    }
}
