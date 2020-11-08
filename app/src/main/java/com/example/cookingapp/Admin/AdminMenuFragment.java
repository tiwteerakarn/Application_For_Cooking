package com.example.cookingapp.Admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Adapter.FilterFoodAdapter;
import com.example.cookingapp.Adapter.FoodmenuAdapter;
import com.example.cookingapp.CreateRecipe.CreateRecipeActivity;
import com.example.cookingapp.FoodDetail.FoodDetailActivity;
import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.Ingredientv2;
import com.example.cookingapp.SearchMenu.SearchActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminMenuFragment extends Fragment {
    int s = 0;
    private FilterFoodAdapter filterFoodAdapter;
    private ImageButton bt_fillter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mTags = mRootRef.child("tags");
    TextView txt_title;
    EditText et_searchfood;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("foodmenu");
    private RecyclerView recyclerView;
    private List<Foodmenu> foodmenus = new ArrayList<>();
    List<String>  f2;
    ArrayList<List<String>> f4 = new ArrayList<List<String>>();
    List<String> f5 = new ArrayList<String>();
    ArrayList<Foodmenu> foodmenuSearch = new ArrayList<>();
    private ProgressDialog pDialog;
    private String vip ;
    private int point ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    boolean check_value=false;

    public AdminMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_admin_menu, container, false);
        bt_fillter = view.findViewById(R.id.filter_food);
        et_searchfood =view.findViewById(R.id.search_food);
        pDialog = new ProgressDialog(getActivity());
        txt_title = view.findViewById(R.id.txt_title);
        checkVip();

        recyclerView = view.findViewById(R.id.recyclerView_fillter_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setHasOptionsMenu(true);
        f2 = new ArrayList<>();
        filterFood("ทั้งหมด");


        view.findViewById(R.id.fab1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (point > 0) {
                    Intent intent = new Intent(getActivity(), CreateRecipeActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getActivity(),"แต้มของคุณหมด",Toast.LENGTH_LONG).show();
                }
            }
        });

        et_searchfood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    filterFoodAdapter.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTags.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    long count= dataSnapshot.getChildrenCount();
                    List<String>list = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String s = ds.getValue(String.class);
                        list.add(s);
                    }

                    String aa[] = list.toArray(new String[list.size()]);


                    bt_fillter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("เลือกรายการ")
                                    .setItems(aa, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            String selected = aa[which];

                                            if (selected.equals("ค้นหาจากวัตถุดิบ")){
                                                ShowFilterDialog();
                                            }else {
                                                txt_title.setText(selected);

                                                if (selected.equals("ทั้งหมด")){
                                                    filterFood("ทั้งหมด");
                                                }
                                                else
                                                {
                                                    filterFood(txt_title.getText().toString());
                                                }
                                            }


                                        }
                                    }).show();
                        }

                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
    private void checkVip() {

        mRootRef.child("vip_point").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    VipPointModel vipPointModel = dataSnapshot.getValue(VipPointModel.class);
                    vip = vipPointModel.getVip();
                    point = vipPointModel.getPoint();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void displayProgressDialog() {
        pDialog.setMessage("กำลังโหลดข้อมูล...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void ShowFilterDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("เลือกวัตถุดิบ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> v2 = new ArrayList<>();

        Query query =  db.collection("ingredient").orderBy("name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()){
                    v2.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        Ingredientv2 ingredientv2 = queryDocumentSnapshot.toObject(Ingredientv2.class);
                        v2.add(ingredientv2.getName());
                    }

                }
            }
        });


        final LayoutInflater inflater = this.getLayoutInflater();
        View filter_layout = inflater.inflate(R.layout.dialog_options,null);

        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)filter_layout.findViewById(R.id.txt_category);
        final ChipGroup chipGroup = (ChipGroup)filter_layout.findViewById(R.id.chipGroup);

        CheckBox ch1 = (CheckBox)filter_layout.findViewById(R.id.checkBox1) ;
        CheckBox ch2 = (CheckBox)filter_layout.findViewById(R.id.checkBox2) ;

        ch2.setChecked(true);

        ch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ch2.setChecked(false);
                    check_value=true;
                }
                else
                    ch2.setChecked(true);
            }
        });
        ch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ch1.setChecked(false);
                    check_value=false;
                }
                else
                    ch1.setChecked(true);
            }
        });



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.select_dialog_item, v2);

        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                autoCompleteTextView.setText("");

                Chip chip = (Chip)inflater.inflate(R.layout.chip_item2,null,false);
                chip.setText(((TextView)view).getText());
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "ลบ", Toast.LENGTH_SHORT).show();
                        chipGroup.removeView(view);
                    }

                });

                //Toast.makeText(getContext(), String.valueOf(chip.getText()), Toast.LENGTH_SHORT).show();

                chipGroup.addView(chip);



            }
        });

        alertDialog.setView(filter_layout);
        alertDialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setPositiveButton("ค้นหา", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                displayProgressDialog();
                final List<String> filter_key = new ArrayList<>();
                StringBuilder filter_query = new StringBuilder("");

                for (int j=0 ; j<chipGroup.getChildCount();j++){
                    Chip chip = (Chip)chipGroup.getChildAt(j);
                    filter_key.add(chip.getText().toString());
                }

                //Toast.makeText(getContext(), filter_key.get(1), Toast.LENGTH_SHORT).show();
                // fetchFilterCategory(filter_query.toString());

                notebookRef.orderBy("like").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        foodmenus.clear();
                        f2.clear();
                        f4.clear();
                        f5.clear();


                        for (QueryDocumentSnapshot q: task.getResult()){
                            Foodmenu foodmenu = q.toObject(Foodmenu.class);
                            f5.add(q.getId());
                            f4.add((List<String>) q.get("ingredients"));
                            foodmenus.add(foodmenu);


                        }


                        for (int i = 0; i < f4.size(); i++) {

                            for (int j = 0; j < f4.get(i).size(); j++) {

                                for (int k=0 ; k<filter_key.size();k++) {

                                    if (f4.get(i).get(j).equals(filter_key.get(k))) {
                                        s += 1;

                                        //btn1.setText(String.valueOf(f4.get(i).get(j)));
                                        //btn2.setText(String.valueOf(foodmenus.get(i).getFoodname()));
                                    }
                                }

                                if (ch2.isChecked()) s+=2;

                                if (s >= f4.get(i).size()) {
                                    if (!f2.contains(f5.get(i))){
                                        f2.add(f5.get(i));
                                    }

                                }


                            }
                            s=0;

                        }
                        foodmenuSearch.clear();
                        //foodAdd(f2);

                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putStringArrayListExtra("id", new ArrayList<String>(f2));
                        intent.putStringArrayListExtra("ingredient",new ArrayList<String>(filter_key));

                        startActivity(intent);
                        //filter(f2.get(0));
                        //Toast.makeText(getContext(), "พบ: "+f2.size()+" รายการ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void foodAdd(List<String> f2) {
        Toast.makeText(getContext(), "พบ: "+f2.size()+" รายการ", Toast.LENGTH_SHORT).show();

        for (int i=0;i<f2.size();i++){
            notebookRef.document(f2.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {
                        final Foodmenu foodmenu = documentSnapshot.toObject(Foodmenu.class);
                        String id = documentSnapshot.getId();
                        foodmenu.setDocumentId(id);


                        foodmenuSearch.add(foodmenu);

                        Collections.sort(foodmenuSearch, Comparator.comparingDouble(Foodmenu::getRating).reversed());

                        filterFoodAdapter = new FilterFoodAdapter(getContext(),foodmenuSearch);
                        recyclerView.setAdapter(filterFoodAdapter);

                        filterFoodAdapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {

                                Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                                intent.putExtra("id", foodmenuSearch.get(position).getDocumentId());
                                startActivity(intent);

                            }
                        });



                    } else {
                        Toast.makeText(getContext(), "ไม่พบเรายการ", Toast.LENGTH_SHORT).show();
                    }
                    pDialog.dismiss();
                }
            });
        }
        pDialog.dismiss();
    }


    private void filterFood(String tag) {
        ArrayList<Foodmenu> ff = new ArrayList<>();
        displayProgressDialog();
        notebookRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ff.clear();
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        Foodmenu foodmenu = document.toObject(Foodmenu.class);
                        if (tag.equals("ทั้งหมด"))
                        {
                            ff.add(foodmenu);

                        }else {
                            for (String s : foodmenu.getTags()) {
                                if (s.equals(tag)) {
                                    ff.add(foodmenu);
                                }
                            }
                        }

                        Collections.sort(ff,Comparator.comparingDouble(Foodmenu::getRating).reversed());

                        filterFoodAdapter = new FilterFoodAdapter(getContext(),ff);
                        recyclerView.setAdapter(filterFoodAdapter);
                        filterFoodAdapter.notifyDataSetChanged();
                        //Toast.makeText(getContext(),""+ff.size(),Toast.LENGTH_LONG).show();

                        filterFoodAdapter.setOnItemClickListener(new FoodmenuAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                //Toast.makeText(getContext()," Id : "+ff.get(position).getFoodname(),Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                                intent.putExtra("id", ff.get(position).getDocumentId());
                                startActivity(intent);

                            }
                        });

                    }



                }
                pDialog.dismiss();
            }

        });



    }

    @Override
    public void onStart() {
        super.onStart();
        filterFood("ทั้งหมด");
    }

}
