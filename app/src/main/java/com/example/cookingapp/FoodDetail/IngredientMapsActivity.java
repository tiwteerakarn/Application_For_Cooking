package com.example.cookingapp.FoodDetail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.Model.AddLatLngModel;
import com.example.cookingapp.R;
import com.example.cookingapp.utils.GpsTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class IngredientMapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private GpsTracker gpsTracker;
    CustomSuggestionsAdapter customSuggestionsAdapter;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final int RE_CODE = 10;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Maps");
    DatabaseReference mUser = database.getReference("users");
    String textid;
    private ArrayList<AddLatLngModel> latlngs = new ArrayList<>();
    public static MaterialSearchBar materialSearchBar;
    private List<Marker> markers = new ArrayList<>();
    private Button btFind;
    int radius = 500;
    double latitude ;
    double longitude ;
    String selected  = "ทั้งหมด";
    private Circle circle ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_maps);
        materialSearchBar = findViewById(R.id.searchBar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
        btFind = findViewById(R.id.btn_find);

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });


        materialSearchBar.inflateMenu(R.menu.maps_menu);
        materialSearchBar.getMenu().setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                if (id == R.id.map1) {
                    selected = "ทั้งหมด";
                    getLocation();
                    return true;
                }else if (id == R.id.map2){
                    selected = "ระยะทาง";
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View promptsView = inflater.inflate(R.layout.distance_map, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            IngredientMapsActivity.this);
                    alertDialogBuilder.setView(promptsView);
                    EditText userInput = (EditText) promptsView.findViewById(R.id.etInput);

                    userInput.setText(""+radius);

                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    radius = Integer.parseInt(userInput.getText().toString().trim());

                                    getLocation();

                                }
                            })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                    return true;
                }

                return false;
            }
        });


    }

    private void openGoogleMap(LatLng src, LatLng dest) {

        String url = "http://maps.google.com/maps?saddr="+src.latitude+","+src.longitude+"&daddr="+dest.latitude+","+dest.longitude+"&mode=driving";
        Uri gmmIntentUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        finish();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String text = getIntent().getExtras().getString("id");
        textid=text;
        mMap.clear();
        if (circle != null){
            circle.remove();
        }
        if (selected.equals("ระยะทาง")){
            LatLng coordinate = new LatLng(latitude, longitude);

            circle = mMap.addCircle(new CircleOptions()
                    .center(coordinate)
                    .radius(radius)
                    .strokeColor(Color.rgb(0, 136, 255))
                    .fillColor(Color.argb(20, 0, 136, 255)));
        }


        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

            LatLng coordinate = new LatLng(latitude, longitude);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 14));


           MarkerOptions marker = new MarkerOptions()
                   .position(new LatLng(coordinate.latitude, coordinate.longitude)).title("คุณ").icon(BitmapDescriptorFactory.fromResource(R.drawable.chef_mark));


           mMap.addMarker(marker);



        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        List<Float> distance = new ArrayList<>();

        myRef.child(text).child("LatLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                latlngs.clear();
                distance.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        AddLatLngModel lngModel = ds.getValue(AddLatLngModel.class);

                        float result[] = new float[10];
                        Location.distanceBetween(Double.parseDouble(lngModel.getLat()),Double.parseDouble(lngModel.getLng()),coordinate.latitude,coordinate.longitude,result);

                        if (result[0] < radius && selected.equals("ระยะทาง")) {
                            String dis= "";
                            if (result[0] <= 5000) {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            }else if (result[0] > 5000 && result[0] <= 10000){
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_BLUE))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);

                            }else if (result[0] > 10000 && result[0] <= 20000){
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_ORANGE))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            }else {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            }
                            setTitle(lngModel.getName()+" ระยะ: "+ radius+" เมตร");

                        }else if (selected.equals("ทั้งหมด")) {
                            setTitle(lngModel.getName()+"");
                            if (result[0] <= 5000) {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            } else if (result[0] > 5000 && result[0] <= 10000) {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_BLUE))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);

                            } else if (result[0] > 10000 && result[0] <= 20000) {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_ORANGE))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            } else {
                                googleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(Double.parseDouble(lngModel.getLat()), Double.parseDouble(lngModel.getLng())))
                                        .title(lngModel.getTitle()).snippet(Math.round(result[0]) + " เมตร").icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED))).showInfoWindow();
                                latlngs.add(lngModel);
                                distance.add(result[0]);
                            }

                        }
                    }


                    customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater, latlngs, IngredientMapsActivity.this, googleMap,distance);
                    customSuggestionsAdapter.setSuggestions(latlngs);
                    materialSearchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);
                    Toast.makeText(IngredientMapsActivity.this,"พบ: "+latlngs.size()+" รายการ",Toast.LENGTH_SHORT).show();

                    materialSearchBar.addTextChangeListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (latlngs.size() > 0) {
                                customSuggestionsAdapter.getFilter().filter(materialSearchBar.getText());
                            }else {
                                Toast.makeText(IngredientMapsActivity.this,"ไม่พบรายการ",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                }else {
                    Toast.makeText(IngredientMapsActivity.this,"พบ 0 รายการ",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //*** Marker

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final LatLng sydney = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                openGoogleMap(coordinate, sydney);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RE_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }
                break;
        }
    }


    public static class CustomSuggestionsAdapter extends SuggestionsAdapter<AddLatLngModel, CustomSuggestionsAdapter.SuggestionHolder> {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mUser = database.getReference("users");
        List<AddLatLngModel> latLngModels;
        List<Float> distance;
        GoogleMap map;

        Context context;

        public CustomSuggestionsAdapter(LayoutInflater inflater, List<AddLatLngModel> latLngModels, Context context, GoogleMap map, List<Float> distance) {
            super(inflater);
            this.latLngModels = latLngModels;
            this.context = context;
            this.map = map;
            this.distance = distance;
        }

        @Override
        public int getSingleViewHeight() {
            return 80;
        }

        @Override
        public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_custom_suggestion, parent, false);
            return new SuggestionHolder(view);
        }

        @Override
        public void onBindSuggestionHolder(final AddLatLngModel suggestion, SuggestionHolder holder, final int position) {
            holder.title.setText(latLngModels.get(position).getTitle());
            //holder.subtitle.setText("The price is " + suggestion.getLat()+ "$");

                    holder.subtitle.setText("ระยะทาง: "+Math.round(distance.get(position))+ " เมตร");


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,""+latLngModels.get(position).getTitle(),Toast.LENGTH_LONG).show();
                    LatLng coordinate = new LatLng(Double.parseDouble(latLngModels.get(position).getLat()), Double.parseDouble(latLngModels.get(position).getLng()));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 13));
                    materialSearchBar.closeSearch();

                }
            });
        }

        /**
         * <b>Override to customize functionality</b>
         * <p>Returns a filter that can be used to constrain data with a filtering
         * pattern.</p>
         * <p>
         * <p>This method is usually implemented by {@link RecyclerView.Adapter}
         * classes.</p>
         *
         * @return a filter used to constrain data
         */
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();
                    String term = constraint.toString();
                    if(term.isEmpty())
                        suggestions = suggestions_clone;
                    else {
                        suggestions = new ArrayList<>();
                        for (AddLatLngModel item: suggestions_clone)
                            if(item.getTitle().toLowerCase().contains(term.toLowerCase()))
                                suggestions.add(item);
                    }
                    results.values = suggestions;
                    return results;

                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    suggestions = (List<AddLatLngModel>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        class SuggestionHolder extends RecyclerView.ViewHolder{
            protected TextView title;
            protected TextView subtitle;

            public SuggestionHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public void getLocation(){
        gpsTracker = new GpsTracker(IngredientMapsActivity.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();


            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(IngredientMapsActivity.this::onMapReady);

        }else{
            gpsTracker.showSettingsAlert();
        }
    }
}
