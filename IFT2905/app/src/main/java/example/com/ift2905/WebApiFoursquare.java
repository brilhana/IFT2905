package example.com.ift2905;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import example.com.ift2905.FoursquareSearch.Item;
import example.com.ift2905.FoursquareSearch.Root;
import example.com.ift2905.FoursquareVenue.Venue;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebApiFoursquare {
	public String venueSearch;
    public String venueDetails;

    public WebApiFoursquare(double lati, double longi) {
        venueSearch = "https://api.foursquare.com/v2/venues/search?client_id=AIW3TTQYOESMBJPU0NUCBMJZYEGPJ31N2NFC1NXKF52UDNOJ&client_secret=J3AMVCROE1NSA4G2ZLKBHIULZJICSY43J13IQMATXH0I1QQ2&v=20130815&ll="+lati+","+longi+"&limit=50&categoryId=50327c8591d4c4b30a586d5d&radius=20000";
    }

    public WebApiFoursquare(String id) {
        venueDetails = "https://api.foursquare.com/v2/venues/"+id+"?client_id=AIW3TTQYOESMBJPU0NUCBMJZYEGPJ31N2NFC1NXKF52UDNOJ&client_secret=J3AMVCROE1NSA4G2ZLKBHIULZJICSY43J13IQMATXH0I1QQ2&v=20130815";
    }

    // On va chercher toutes les microbrasseries.
	public List<Item> runFoursquare() throws IOException {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(venueSearch).build();
		Response response = client.newCall(request).execute();
		String json = response.body().string();
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<Root> jsonAdapter = moshi.adapter(Root.class);
		Root root = jsonAdapter.fromJson(json);
        Log.d("liste", String.valueOf(root.response.venues));
        Log.d("liste", String.valueOf(root.response.venues.size()));
		return root.response.venues;
	}

    // On va chercher la fiche d'une microbrasserie en particulier.
    public Venue runVenueSearch() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(venueDetails).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<example.com.ift2905.FoursquareVenue.Root> jsonAdapter = moshi.adapter(example.com.ift2905.FoursquareVenue.Root.class);
        example.com.ift2905.FoursquareVenue.Root root = jsonAdapter.fromJson(json);
        return root.response.venue;
    }

    // On va chercher le score d'une microbrasserie en particulier.
    public double runVenueRatingsSearch() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(venueDetails).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<example.com.ift2905.FoursquareVenue.Root> jsonAdapter = moshi.adapter(example.com.ift2905.FoursquareVenue.Root.class);
        example.com.ift2905.FoursquareVenue.Root root = jsonAdapter.fromJson(json);
        return root.response.venue.rating;
    }
}