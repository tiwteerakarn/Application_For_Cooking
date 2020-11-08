package com.example.cookingapp.CreateRecipe;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.R;
import com.example.cookingapp.SearchMenu.Ingredientv2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeIngredientsFragment extends NavigableFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText amountlist;
    private IngredientListener mListener;
    private List<String> ingredientList;
    private List<String> amount;
    private List<String> ingredientAmount;
    private IngredientAdapter ingredientAdapter;
    //private DatabaseAdapter databaseAdapter;

    private RecyclerView ingredientRecyclerView;
    private TextView emptyView;
    private Button addButton;
    private AutoCompleteTextView ingredientField;

    public RecipeIngredientsFragment() {
        // Required empty public constructor
    }

    public static RecipeIngredientsFragment newInstance(Foodmenu recipe) {
        RecipeIngredientsFragment fragment = new RecipeIngredientsFragment();

        if (recipe.getIngredients() != null) {
            Bundle args = new Bundle();
            args.putStringArrayList("ingredients", (ArrayList<String>) recipe.getIngredients());
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_ingredients, container, false);
        //databaseAdapter = DatabaseAdapter.getInstance(getActivity());
        List<String> v2 = new ArrayList<>();
        amountlist = view.findViewById(R.id.amount);

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
        Bundle args = getArguments();
        if (args != null)
            ingredientList = args.getStringArrayList("ingredients");
        if (ingredientList == null) {
            ingredientList = new ArrayList<>();
            amount = new ArrayList<>();
        }
        ingredientAmount = new ArrayList<>();



        ingredientRecyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.empty_view);
        addButton = view.findViewById(R.id.add_button);
        ingredientField = view.findViewById(R.id.ingredientField);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.ingredient_list_item, R.id.text_view_list_item,v2);
        ingredientField.setAdapter(adapter);
        ingredientField.setHint("ตัวอย่าง: ไข่ไก่");

        ingredientAdapter = new IngredientAdapter(getActivity(), ingredientAmount);
        ingredientAdapter.setIngredientListener(position -> {
            ingredientAmount.remove(position);
            ingredientList.remove(position);
            amount.remove(position);
            toggleEmptyView();
            ingredientAdapter.notifyDataSetChanged();
        });

        toggleEmptyView();

        ingredientRecyclerView.setHasFixedSize(true);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ingredientRecyclerView.setAdapter(ingredientAdapter);

        addButton.setOnClickListener(v -> {
            try {
                String newIngredient = ingredientField.getText().toString();
                String newAmount = amountlist.getText().toString().trim();

                String[] arr = newIngredient.split(",");
                if (!newIngredient.isEmpty() && !newAmount.isEmpty()) {
                    ingredientField.setText("");
                    amountlist.setText("");
                    ingredientAmount.add(newIngredient+"      "+newAmount);
                    ingredientList.add(newIngredient);
                    amount.add(newAmount);
                    toggleEmptyView();
                    ingredientAdapter.notifyDataSetChanged();
                }
            }catch (ArrayIndexOutOfBoundsException e){
                Toast.makeText(getActivity(),"กรุณาเพิ่มข้อมูลให้ครบ",Toast.LENGTH_SHORT).show();

            }

        });

        return view;
    }

    private void toggleEmptyView() {
        if (ingredientList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            ingredientRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            ingredientRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IngredientListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IngredientListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onNext() {
        if (mListener != null) {
            if (ingredientList.isEmpty()) Toast.makeText(getActivity(),"กรุณาเพิ่ม",Toast.LENGTH_SHORT).show();
            else mListener.navigateToDirectionsFragment(ingredientList,amount);
        }

        }


    public interface IngredientListener {
        void navigateToDirectionsFragment(List<String> ingredients, List<String> amount);
    }

    @Override
    public void onStart() {
        super.onStart();
        
    }
}
