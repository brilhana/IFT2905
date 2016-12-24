package example.com.ift2905;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import example.com.ift2905.FoursquareSearch.Item;
import example.com.ift2905.FoursquareVenue.Venue;
import example.com.ift2905.UntappdVenue.ItemTopBeer;
import example.com.ift2905.UntappdVenue.ResponseGroup;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMyLocationButtonClickListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private NavigationView navigationView;
    private SupportMapFragment fragment;
    private Bundle extras;
    private GoogleMap map;
    private List<Item> venues;
    private List<Item> sevensAndUp = new ArrayList<>();
    private List<Item> eightsAndUp = new ArrayList<>();
    private List<Item> ninesAndUp = new ArrayList<>();
    private Marker clickedMarker;
    private DBHelper dbh;
    private Integer venueId;
    private String foursquareId = null;
    private double rating;
    private boolean seven = false;
    private boolean eight = false;
    private boolean nine = false;
    private ItemTopBeer topBeer;
    private example.com.ift2905.UntappdVenue.ResponseGroup untappdVenueResponse;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private double currLat = 0;
    private double currLong = 0;
    private Boolean CoordAssign = false;
    private String searchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // On la recherche des microbrasseries.
        FoursquareInit foursquare = new FoursquareInit();
        foursquare.execute();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        extras = getIntent().getExtras();

        // Ajoute notre toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ajoute notre fragment carte.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Ajoute notre tiroir.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        menu.setGroupCheckable(R.id.drawer, true, true);

        fragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        map = fragment.getMap();

        // Pour notre barre de recherche.
        final EditText editText = (EditText) findViewById(R.id.search);

        if (editText != null) {

            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override

                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    boolean handled = false;

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchString = editText.getText().toString();
                        getSearch();
                        handled = true;

                    }

                    return handled;

                }

            });

        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Listener sur le info window: un clique dessus l'ajouter ou le supprime des favoris.
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Item i = findItem(marker);
                if (!i.favorite) {
                    dbh.addFavorite(i);
                    marker.setIcon((BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on)));
                    i.setFavorite(true);
                    Toast.makeText(MainActivity.this, "Added to your Favorites!", Toast.LENGTH_LONG).show();
                }
                else {
                	dbh.removeFavorite(i);
                	i.setFavorite(false);
                    if (i.rating < 7) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    else if (i.rating >= 8) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    }
                    else if (i.rating >= 9) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    Toast.makeText(MainActivity.this, "Removed from your Favorites!", Toast.LENGTH_LONG).show();
                }
                marker.hideInfoWindow();
            }
        });

        dbh = new DBHelper(this);

        map.setOnMarkerClickListener(this);

        map.setOnMyLocationButtonClickListener(this);

        map.setInfoWindowAdapter(new MyInfoWindowAdapter());

        enableMyLocation();

        seven = true;
        eight = true;
        nine = true;

        LatLng latLng = new LatLng(45.5, -73.566667);

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        map.animateCamera(center);

        // Si on clique sur un élément du curseur dans FavoritesActivity, on zoom sur le favori.
        if(extras != null && extras.getStringArray("extras") != null) {
            String[] coords = extras.getStringArray("extras");
            LatLng latLngFavorite = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngFavorite, 18);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.favorites:
                Intent i = new Intent(this, FavoritesActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);


    }

    // On retourne la microbrasserie la plus proche.
    public void onClickActionNearest(View view){
        Toast.makeText(MainActivity.this, "Taking you to the nearest microbrewery!", Toast.LENGTH_LONG).show();
        nearestBrewery();
    }

    // On retourne la meilleure microbrasserie.
    public void onClickActionTop(View view){
        Toast.makeText(MainActivity.this, "Taking you to the best microbrewery", Toast.LENGTH_LONG).show();
        topBrewery();
    }

    // // On retourne la microbrasserie au hasard.
    public void onClickActionRandom(View view){
        Toast.makeText(MainActivity.this, "Taking you to a random microbrewery!", Toast.LENGTH_LONG).show();
        randomBrewery();
    }

    // Retourne une microbrasserie au hasard selon les filtres activés.
    public void randomBrewery() {
        Random rand = new Random();
        Item random;
        if(!seven) {
            random = sevensAndUp.get(rand.nextInt(sevensAndUp.size()));
        }

        if(!eight) {
            random = eightsAndUp.get(rand.nextInt(eightsAndUp.size()));
        }

        if(!nine) {
            random = ninesAndUp.get(rand.nextInt(ninesAndUp.size()));
        }

        else {
            random = venues.get(rand.nextInt(venues.size()));
        }

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(random.location.lat, random.location.lng), 18);
        map.animateCamera(center);
    }

    // Retourne la microbrasserie la plus proche selon les filtres activés.
    public void nearestBrewery() {
        Item min;

        // Parmis celle qui ont un score au dessus de 7.
        if(!seven) {
            min = sevensAndUp.get(0);
            for(int i=1; i< sevensAndUp.size(); i++) {
                if (sevensAndUp.get(i).location.distance < min.location.distance) {
                    min = sevensAndUp.get(i);
                }
            }
        }

        // Parmis celle qui ont un score au dessus de 8.
        if(!eight) {
            min = eightsAndUp.get(0);
            for(int i=1; i< eightsAndUp.size(); i++) {
                if (eightsAndUp.get(i).location.distance < min.location.distance) {
                    min = eightsAndUp.get(i);
                }
            }
        }

        // Parmis celle qui ont un score au dessus de 9.
        if(!nine) {
            min = ninesAndUp.get(0);
            for(int i=1; i< ninesAndUp.size(); i++) {
                if (ninesAndUp.get(i).location.distance < min.location.distance) {
                    min = ninesAndUp.get(i);
                }
            }
        }

        else {
            min = venues.get(0);
            for(int i=1; i<venues.size(); i++) {
            	if (venues.get(i).location.distance < min.location.distance) {
                	min = venues.get(i);
                }
            }
        }
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(min.location.lat, min.location.lng), 18);
        map.animateCamera(center);
    }

    public void topBrewery() {
        Item top;

        // Parmis celle qui ont un score au dessus de 7.
        if(!seven) {
            top = sevensAndUp.get(0);
            for (int i = 1; i < sevensAndUp.size(); i++) {
                if (sevensAndUp.get(i).rating > top.rating) {
                    top = sevensAndUp.get(i);
                }
            }
        }

        // Parmis celle qui ont un score au dessus de 8.
        if(!eight) {
            top = eightsAndUp.get(0);
            for (int i = 1; i < eightsAndUp.size(); i++) {
                if (eightsAndUp.get(i).rating > top.rating) {
                    top = eightsAndUp.get(i);
                }
            }
        }

        // Parmis celle qui ont un score au dessus de 9.
        if(!nine) {
            top = ninesAndUp.get(0);
            for (int i = 1; i < ninesAndUp.size(); i++) {
                if (ninesAndUp.get(i).rating > top.rating) {
                    top = ninesAndUp.get(i);
                }
            }
        }
        else {
            top = venues.get(0);
            for(int i=1; i<venues.size(); i++) {
                if (venues.get(i).rating > top.rating) {
                    top = venues.get(i);
                }
            }
        }
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(top.location.lat, top.location.lng), 18);
        map.animateCamera(center);
    }

    public void getSearch(){
        boolean v = true;

        for (int i=0; i<venues.size(); i++) {
            if (searchString.toLowerCase().equals(venues.get(i).name.toLowerCase())){
                Toast.makeText(MainActivity.this, venues.get(i).name, Toast.LENGTH_SHORT).show();
                LatLng found = new LatLng(venues.get(i).location.lat, venues.get(i).location.lng);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(found, 15);
                map.animateCamera(cameraUpdate);
                v=false;

            }
        }
        if (v){
            Toast.makeText(MainActivity.this, "Venue not found", Toast.LENGTH_LONG).show();
        }



    }

    // Les méthodes rattachées aux boutons du tiroir.
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        // On accède aux favoris.
        if (id == R.id.nav_favorites) {
            Intent i = new Intent(this, FavoritesActivity.class);
            startActivity(i);
        }

        // On filtre uniquement les microbrasseries de score 7 et plus.
        else if (id == R.id.nav_7) {
            if(seven) {
                map.clear();
                for(int i=0; i<venues.size(); i++) {
                    if (venues.get(i).rating < 8) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    if (venues.get(i).rating >= 8) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    if (venues.get(i).rating >= 9) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }

                    if (venues.get(i).favorite) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    
                }
            }
            else {
                resetMarkers();
            }
            seven = !seven;

        }

        // On filtre uniquement les microbrasseries de score 8 et plus.
        else if (id == R.id.nav_8) {
            if(eight) {
                map.clear();
                for(int i=0; i<venues.size(); i++) {
                    if (venues.get(i).rating >= 8) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    if (venues.get(i).rating >= 9) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    if (venues.get(i).favorite) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                }
            }
            else {
                resetMarkers();
            }
            eight = !eight;

        }

        // On filtre uniquement les microbrasseries de score 9 et plus.
        else if (id == R.id.nav_9) {
            if(nine) {
                map.clear();
                for(int i=0; i<venues.size(); i++) {
                    if (venues.get(i).rating >= 9) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                    if (venues.get(i).favorite) {
                        map.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on))
                                .title(venues.get(i).name)
                                .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                    }
                }
            }
            else {
                resetMarkers();
            }
            nine = !nine;

        }

        // La microbrasserie la plus proche.
        else if (id == R.id.nav_nearest) {
            Toast.makeText(MainActivity.this, "Taking you to the nearest brewery", Toast.LENGTH_LONG).show();
            nearestBrewery();

        }

        // La meilleure microbrasserie.
        else if (id == R.id.nav_top) {
            Toast.makeText(MainActivity.this, "Taking you to the best brewery!", Toast.LENGTH_LONG).show();
            topBrewery();

        }

        // Une microbrasserie au hasard.
        else if (id == R.id.nav_random) {
            Toast.makeText(MainActivity.this, "Taking you to a random brewery", Toast.LENGTH_LONG).show();
            randomBrewery();
        }

        // On accède aux settings.
        else if (id == R.id.nav_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Si on clique sur un marker, son icon devient mi-transparent, on lance l'API d'Untappd pour obtenir ses infos et zoom sur la localisation.
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(map != null) {
            marker.setAlpha(0.5f);
            clickedMarker = marker;
            String foursquareId = findItem(marker).id;
            FoursquareLookup untappd = new FoursquareLookup();
            untappd.execute(foursquareId);
            LatLng latLng = marker.getPosition();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            map.animateCamera(cameraUpdate);
        }
        return true;
    }

    // Retourne l'item de Foursquare correspondant au marqueur.
    public Item findItem(Marker marker) {
        for(int i=0; i<venues.size(); i++) {
            if(marker.getPosition().latitude == venues.get(i).location.lat && marker.getPosition().longitude == venues.get(i).location.lng) {
                return venues.get(i);
            }
        }
        return null;
    }

    // Supprime tous les marqueurs sur la carte.
    public void deleteMarkers() {
        map.clear();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        currLat = location.getLatitude();
        currLong = location.getLongitude();
        LatLng latLong = new LatLng(currLat, currLong);
        CameraUpdate cameraUpdate1 = CameraUpdateFactory.newLatLngZoom(latLong, 15);
        map.animateCamera(cameraUpdate1);
        CoordAssign = true;
        if (location == null) {
            // error
        }
        else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    // Info window sur mesure.
    public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.info_contents, null);

        }

        // Mise en place des infos de la microbrasserie.
        @Override
        public View getInfoContents(Marker marker) {

            ImageView badge = (ImageView) myContentsView.findViewById(R.id.badge);

            TextView title = (TextView) myContentsView.findViewById(R.id.title);
            title.setText(marker.getTitle());

            TextView snippet = (TextView) myContentsView.findViewById(R.id.snippet);
            Item i = findItem(marker);

            // Si c'est un favori, on met son badge de favori.
            if(i.favorite) {
                badge.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
            }
            else {
                badge.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
            }

            String str;
            if (i.location.distance < 1000) {
                str = i.location.address + ", " + i.location.city + "\nDistance: " + Math.round((i.location.distance)) + " m\nRating: " + rating + " / 10\n" + "Top Beer: " + topBeer.beer.beer_name + "\n" + (i.url).split("http://")[1];
            }
            else {
                str = i.location.address + ", " + i.location.city + "\nDistance: " + Math.round((i.location.distance) / 1000) + " km\nRating: " + rating + " / 10\n" + "Top Beer: " + topBeer.beer.beer_name + "\n" + (i.url).split("http://")[1];
            }
            snippet.setText(str);
            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    // On initialise notre liste de venues de Foursquare.
    public class FoursquareInit extends AsyncTask<String, Object, List<Item>> {
        @Override
        public List<Item> doInBackground(String... params) {
            while (!CoordAssign){
                //wait until coordinates havebeen assigned by onConnected
            }
            WebApiFoursquare web = new WebApiFoursquare(currLat, currLong);
            try {
                venues = web.runFoursquare();
            } catch(IOException e) {}
            return venues;
        }

        // Pour tous les éléments de notre liste, on va chercher son score.
        @Override
        protected void onPostExecute(List<Item> venues) {
            super.onPostExecute(venues);
            for (int i = 0; i < venues.size(); i++) {
                FoursquareRatings foursquareRatings = new FoursquareRatings();
                foursquareRatings.execute(venues.get(i).id, String.valueOf(i));
            }
        }
    }

    public class FoursquareRatings extends AsyncTask<String, Object, Double> {

        Double rating = null;

        // Pour toutes nos venues, on leur associe une propriété score (double).
        @Override
        public Double doInBackground(String... params) {
            WebApiFoursquare web = new WebApiFoursquare(params[0]);
            String i = params[1];
            try {
                rating = web.runVenueRatingsSearch();
            }
            catch(IOException e) {}
            venues.get(Integer.parseInt(i)).setRating(rating);
            return rating;
        }

        // À cette étape, on est prêt à rajouter les marqueurs.
        @Override
        protected void onPostExecute(Double venue) {
            super.onPostExecute(venue);
            resetMarkers();
        }
    }

    public class FoursquareLookup extends AsyncTask<String, Object, Integer> {

        public example.com.ift2905.UntappdLookup.Item i = null;

        // On convertit une venue de Foursquare en venue de Untappd.
        @Override
        public Integer doInBackground(String... params) {
            foursquareId = params[0];
            WebApiUntappd web = new WebApiUntappd(params[0]);
            try {
                i = web.convertFoursquareToUntappd().get(0);
                venueId = i.venue_id;
            } catch(IOException e) {}
            return venueId;
        }

        // On lance la recherche pour obtenir le json de la venue Untappd.
        @Override
        protected void onPostExecute(Integer venueId) {
            super.onPostExecute(venueId);
            FoursquareVenueSearch foursquareVenueSearch = new FoursquareVenueSearch();
            foursquareVenueSearch.execute(foursquareId);
        }
    }

    // On cherche les infos de la venue de Foursquare.
    public class FoursquareVenueSearch extends AsyncTask<String, Object, Venue> {

        Venue venue = null;

        @Override
        public Venue doInBackground(String... params) {
            WebApiFoursquare web = new WebApiFoursquare(params[0]);
            try {
                venue = web.runVenueSearch();
            }
            catch(IOException e) {}
            return venue;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            super.onPostExecute(venue);
            rating = venue.rating;
            UntappdVenueSearch run = new UntappdVenueSearch();
            run.execute(String.valueOf(venueId));
        }
    }

    // On va chercher les infos / json propre à la venue Untappd.
    public class UntappdVenueSearch extends AsyncTask<String, Object, ResponseGroup> {
        public ResponseGroup doInBackground(String... params) {
            WebApiUntappd web = new WebApiUntappd(params[0]);
            try {
                untappdVenueResponse = web.runUntappdVenueSearch();
                List<ItemTopBeer> topBeers = untappdVenueResponse.venue.top_beers.items;
                topBeer = topBeers.get(0);
                for(int i=0; i<topBeers.size(); i++) {
                    if(topBeers.get(i).beer.rating_score > topBeer.beer.rating_score) {
                        topBeer = topBeers.get(i);
                    }
                }
            } catch (IOException e) {}
            return untappdVenueResponse;
        }
        @Override
        protected void onPostExecute(ResponseGroup brewery) {
            super.onPostExecute(brewery);
            clickedMarker.showInfoWindow();
        }
    }

    // Set / reset les marqueurs sur la carte.
    public void resetMarkers() {
        for(int i=0; i<venues.size(); i++) {
            if(venues.get(i).rating >= 9) {
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(venues.get(i).name)
                        .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                ninesAndUp.add(venues.get(i));
            }
            else if (venues.get(i).rating >= 8 && venues.get(i).rating < 9) {
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .title(venues.get(i).name)
                        .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                eightsAndUp.add(venues.get(i));
            }
            else if (venues.get(i).rating < 8) {
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(venues.get(i).name)
                        .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
                sevensAndUp.add(venues.get(i));
            }
            else if (venues.get(i).favorite) {
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on))
                        .title(venues.get(i).name)
                        .position(new LatLng(venues.get(i).location.lat, venues.get(i).location.lng)));
            }
        }
    }
}