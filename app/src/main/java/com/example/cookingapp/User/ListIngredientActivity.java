package com.example.cookingapp.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.cookingapp.CreateRecipe.AddMapActivity;
import com.example.cookingapp.Model.VipPointModel;
import com.example.cookingapp.Post.NewPostActivity;
import com.example.cookingapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListIngredientActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    private DatabaseReference myRef;
    private String vip ="";
    private int point =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_ingredient);
        setTitle("รายการวัตถุดิบ");
        ActionBar actionBar = getActionBar();
        viewPager = (ViewPager) findViewById(R.id.viewpager2);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabb);
        tabLayout.setupWithViewPager(viewPager);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        checkVip();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {

            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // Code goes here

            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(
                getSupportFragmentManager());
        adapter.addFrag(new IngredientListFragment(), "วัตถุดิบ");
        adapter.addFrag(new UserLatLngFragment(), "วัตถุดิบของฉัน");
        viewPager.setAdapter(adapter);
    }

    public void onClick(View view) {
        if (vip.equals("vip1") || vip.equals("vip2")) {

                Intent intent = new Intent(ListIngredientActivity.this, AddMapActivity.class);
                startActivity(intent);

        }else {
            Toast.makeText(ListIngredientActivity.this,"สำหรับสมาชิก VIP",Toast.LENGTH_LONG).show();
        }
    }

    private void checkVip() {

        myRef.child("vip_point").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
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
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            notifyDataSetChanged();
            return POSITION_NONE;
        }
    }

}
