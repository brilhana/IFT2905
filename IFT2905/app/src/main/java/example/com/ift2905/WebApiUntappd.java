package example.com.ift2905;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import example.com.ift2905.UntappdLookup.Item;
import example.com.ift2905.UntappdLookup.Root;
import example.com.ift2905.UntappdVenue.ResponseGroup;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebApiUntappd {
    public String venueSearch, foursquareLookup, brewerySearch;

    public WebApiUntappd(String id) {
        brewerySearch = "https://api.untappd.com/v4/brewery/info/"+id+"/?client_id=389E7F9629000A072574E229232EA75612DE984C&client_secret=E5811BE03B49FDEEB20FE155F185E8413A67DA5D";
        venueSearch = "https://api.untappd.com/v4/venue/info/"+id+"/?client_id=389E7F9629000A072574E229232EA75612DE984C&client_secret=E5811BE03B49FDEEB20FE155F185E8413A67DA5D";
        foursquareLookup = "https://api.untappd.com/v4/venue/foursquare_lookup/"+id+"/?client_id=389E7F9629000A072574E229232EA75612DE984C&client_secret=E5811BE03B49FDEEB20FE155F185E8413A67DA5D";
    }

    // Retourne une venue d'Untappd.
    public ResponseGroup runUntappdVenueSearch() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(venueSearch).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<example.com.ift2905.UntappdVenue.Root> jsonAdapter = moshi.adapter(example.com.ift2905.UntappdVenue.Root.class);
        example.com.ift2905.UntappdVenue.Root root = jsonAdapter.fromJson(json);
        return root.response;
    }

    // Convertit un id de Foursquare en un id de Untappd.
    public List<Item> convertFoursquareToUntappd() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(foursquareLookup).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Root> jsonAdapter = moshi.adapter(Root.class);
        Root root = jsonAdapter.fromJson(json);
        return root.response.venue.items;
    }
}