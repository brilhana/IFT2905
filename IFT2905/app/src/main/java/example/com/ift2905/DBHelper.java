package example.com.ift2905;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import example.com.ift2905.FoursquareSearch.Item;

public class DBHelper extends SQLiteOpenHelper {

    static final String DB_NAME = "favorites.db";
    static final int DB_VERSION = 1;

    static final String TABLE_FAVORITES = "favorites";
    static final String F_ID = "_id";
    static final String F_VENUEID = "venueId";
    static final String F_VENUENAME = "venueName";
    static final String F_VENUEADDRESS = "venueAddress";
    static final String F_VENUECITY = "venueCity";
    static final String F_VENUEDISTANCE = "venueDistance";
    static final String F_VENUERATING = "venueRating";
    static final String F_VENUELAT = "venueLat";
    static final String F_VENUELNG = "venueLng";

    private int j = 0;

    private static SQLiteDatabase db = null;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if(db == null) {
            db = getWritableDatabase();
        }
    }

    // Créé notre notre base de données.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "
                + TABLE_FAVORITES + " ( "
                + F_ID + " integer primary key, "
                + F_VENUEID + " text, "
                + F_VENUENAME + " text, "
                + F_VENUEADDRESS + " text, "
                + F_VENUECITY + " text, "
                + F_VENUEDISTANCE + " text, "
                + F_VENUELAT + " text, "
                + F_VENUELNG + " text, "
                + F_VENUERATING + " text ) ";
        Log.d("SQL", sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_FAVORITES);
        onCreate(db);
    }

    // Ajouter un favori dans la base de données.
    public void addFavorite(Item i) {
        String strDist;
        double dist;
        String strAdd;

        // Si la distance est plus grande que 1 000 mètres, on l'affiche en kilomètres.
        if (i.location.distance < 1000) {
            dist = Math.round((i.location.distance));
            strDist = "" + dist + " m";
        } else {
            dist = Math.round((i.location.distance / 1000));
            strDist = "" + dist + " km";
        }

        strAdd = i.location.address + ", " + i.location.city;

        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(F_ID, j);
        cv.put(F_VENUEID, i.id);
        cv.put(F_VENUENAME, i.name);
        cv.put(F_VENUEADDRESS, strAdd);
        cv.put(F_VENUECITY, i.location.city);
        cv.put(F_VENUEDISTANCE, strDist);
        cv.put(F_VENUELAT, i.location.lat);
        cv.put(F_VENUELNG, i.location.lng);
        cv.put(String.valueOf(F_VENUERATING), i.rating);
        try {
            db.insertOrThrow(TABLE_FAVORITES, null, cv);
        } catch(SQLException e) {}
        j++;
    }

    // Supprime un favori en utilisant son F_VENUEID.
    public void removeFavorite(Item i) {
        j--;
        try {
            db.delete(TABLE_FAVORITES, F_VENUEID + " =?", new String[]{i.id});
        }
        catch(SQLException e) {}
    }

    // Supprime un favori en utilisant son F_ID.
    public void remove(int i) {
        j--;
        try {
            db.delete(TABLE_FAVORITES, F_ID + " =?", new String[]{String.valueOf(i)});
        }
        catch(SQLException e) {}
    }

    public static void clearDatabase() {
        db.delete(TABLE_FAVORITES, null, null);
    }

    // Trie les microbrasseries selon le nom.
    public Cursor favoritesName() {
        Cursor c;
        c = db.rawQuery("select * from " + TABLE_FAVORITES + " order by " + F_VENUENAME+ " asc", null);
        return c;
    }

    // Trie les microbrasseries selon la la ville.
    public Cursor favoritesCity() {
        Cursor c;
        c = db.rawQuery("select * from " + TABLE_FAVORITES + " order by " + F_VENUECITY + " asc", null);
        return c;
    }

    // Trie les microbrasseries selon la distance.
    public Cursor favoritesDistance() {
        Cursor c;
        c = db.rawQuery("select * from " + TABLE_FAVORITES + " order by " + F_VENUEDISTANCE + " asc", null);
        return c;
    }

    // Trie les microbrasseries selon le rating.
    public Cursor favoritesRating() {
        Cursor c;
        c = db.rawQuery("select * from " + TABLE_FAVORITES + " order by " + F_VENUERATING + " desc", null);
        return c;
    }
}