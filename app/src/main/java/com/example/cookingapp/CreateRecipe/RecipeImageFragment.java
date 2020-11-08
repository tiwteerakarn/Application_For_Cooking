package com.example.cookingapp.CreateRecipe;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.example.cookingapp.Model.Foodmenu;
import com.example.cookingapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeImageFragment extends NavigableFragment {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mTags = mRootRef.child("tags");
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private String currentRecipeImage;
    private ImageListener mListener;
    private TextInputLayout textInputLayout;
    private ImageView recipeImage;
    private Button selectImageBtn;
    private TextInputEditText recipeName;
    private TextInputEditText recipeDescription;
    private ChipGroup chipGroup;
    private AutoCompleteTextView autoCompleteTextView;
    private List<String> tag = new ArrayList<>();

    public static RecipeImageFragment newInstance(Foodmenu recipe) {
        RecipeImageFragment fragment = new RecipeImageFragment();

        Bundle args = new Bundle();
        if (recipe.getDirections() != null) {
            args.putString("imagePath", recipe.getImage());
            args.putString("description", recipe.getDescription());
            args.putString("name", recipe.getFoodname());
            args.putStringArrayList("tags",(ArrayList<String>) recipe.getTags());
            fragment.setArguments(args);
        }



        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_image, container, false);
        recipeImage = view.findViewById(R.id.recipe_image);
        selectImageBtn = view.findViewById(R.id.choose_image);
        recipeDescription = view.findViewById(R.id.recipe_description);
        recipeName = view.findViewById(R.id.recipe_name);
        //textInputLayout = view.findViewById(R.id.txt_input_layout);
        //autoCompleteTextView = view.findViewById(R.id.txt_category);
        chipGroup = (ChipGroup)view.findViewById(R.id.chipGroup);


        Bundle args = getArguments();
        if (args != null) {
            String imagePath = args.getString("imagePath");
            String description = args.getString("description");
            String name = args.getString("name");
            List<String> tags = args.getStringArrayList("tags");
            onImageSelected(imagePath);
            recipeDescription.setText(description);
            recipeName.setText(name);

        }



        selectImageBtn.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onSelectImage();
        });




        mTags.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<String>list = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String s = ds.getValue(String.class);
                        if (!s.equals("ทั้งหมด") && !s.equals("ค้นหาจากวัตถุดิบ"))
                        list.add(s);
                    }

                    for (String s : list){
                        Chip chip = (Chip)inflater.inflate(R.layout.chip_item,null,false);
                        chip.setText(s);
                        chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                if (!tag.contains(String.valueOf(chip.getText()))){
                                    tag.add(String.valueOf(chip.getText()));
                                    Toast.makeText(getContext(),tag.size()+ " รายการ",Toast.LENGTH_SHORT).show();
                                }else {
                                    int s = tag.indexOf(String.valueOf(chip.getText()));
                                    tag.remove(s);
                                    Toast.makeText(getContext(),"ลบแล้ว",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        chipGroup.addView(chip);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ImageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ImageListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onNext() {
        if (currentRecipeImage == null) {
            Toast.makeText(getActivity(), "Please choose an image for this recipe.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = recipeName.getText().toString();
        String description = recipeDescription.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please specify a name for this recipe.", Toast.LENGTH_LONG).show();
            return;
        }
        if (tag.size() == 0) {
            Toast.makeText(getActivity(), "Please specify tags for this recipe.", Toast.LENGTH_LONG).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(getActivity(), "Please type in a description for this recipe.", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (description.length() > 200) {
                Toast.makeText(getActivity(), "Your description shouldn't exceed " + MAX_DESCRIPTION_LENGTH + " characters.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (mListener != null)
            mListener.navigateToIngredientsFragment(name, description,tag);
    }

    public void onImageSelected(String imagePath) {
        currentRecipeImage = imagePath;
        if (!currentRecipeImage.isEmpty()) {
            recipeImage.setImageURI(Uri.fromFile(new File(currentRecipeImage)));
            selectImageBtn.setText("Update recipe image");
        }
    }

    public interface ImageListener {
        void onSelectImage();

        void navigateToIngredientsFragment(String name, String description,List<String> tag);
    }
}
