package com.example.cookingapp.CreateRecipe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cookingapp.Model.LatLngModel;
import com.example.cookingapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsGoogleFragment extends Fragment implements OnMapReadyCallback {
    SupportMapFragment supportMapFragment;
    private String txttitle;
    private Double lat,lng;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Maps");

    private GoogleMap mMap;
    private ArrayList<LatLngModel> latlngs = new ArrayList<>();

    public static MapsGoogleFragment newInstance(String text, Double number1,Double number2) {
        MapsGoogleFragment fragment = new MapsGoogleFragment();
        Bundle args = new Bundle();
        args.putString("title", text);
        args.putDouble("txtlat",number1);
        args.putDouble("txtlng",number2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mapsgoogle, container, false);




        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        FragmentManager ft = getFragmentManager();
        FragmentTransaction fm = ft.beginTransaction();
        supportMapFragment = SupportMapFragment.newInstance();
        fm.replace(R.id.map,supportMapFragment).commit();

        supportMapFragment.getMapAsync(this);
        latlngs.add(new LatLngModel("13.76488", "100.538334","Victory"));
        latlngs.add(new LatLngModel("13.669605", "100.610077","BITEC"));
        latlngs.add(new LatLngModel("13.745653", "100.534402","BTS"));

        return  view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (getArguments() != null) {
            txttitle = getArguments().getString("title");
            lat = getArguments().getDouble("txtlat");
            lng = getArguments().getDouble("txtlng");

            Toast.makeText(getContext(),""+txttitle,Toast.LENGTH_LONG).show();

            LatLng coordinate = new LatLng(lat, lng);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13));

            //mMap.addMarker(new MarkerOptions().position(coordinate));
        }

        myRef.child("6dLxRx85LtLsyGP79Zbz").child("LatLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    LatLngModel lngModel = ds.getValue(LatLngModel.class);
                    if (dataSnapshot.exists()) {
                        googleMap.addMarker(new MarkerOptions().position(
                                new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                .title(lngModel.getTitle()));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getContext(),
                        "ยืนยัน "+latLng.latitude, Toast.LENGTH_SHORT).show();
                MarkerOptions marker1 = new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude));


                mMap.addMarker(marker1);

                LatLng coordinate = new LatLng(latLng.latitude, latLng.longitude);

                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));


                mMap.addMarker(marker1);

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getContext());
                builder.setMessage("ละติจูด : "+latLng.latitude +"\n"+ "ลองจิจูด : "+latLng.longitude);

                builder.setNegativeButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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
    }
}
