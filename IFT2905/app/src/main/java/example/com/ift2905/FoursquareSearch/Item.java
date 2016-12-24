package example.com.ift2905.FoursquareSearch;

public class Item {
	public String id;
    public String name;
	public Location location;
    public String url;
	public double rating;
	public boolean favorite;

	public void setRating(double rating) {
		this.rating = rating;
	}

	public void setFavorite(boolean b) {
		this.favorite = b;
	}
}