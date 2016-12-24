package example.com.ift2905;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FavoritesActivity extends AppCompatActivity {

    ListView lv;
    DBHelper dbh;
    Cursor c;
    String[] from;
    int[] to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        lv = (ListView) findViewById(R.id.listView);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // Si on clique sur un élément du curseur, on nous ramène sur la carte au dessus de l'endroit.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(FavoritesActivity.this, MainActivity.class);
                String[] extras = {c.getString(c.getColumnIndex(DBHelper.F_VENUELAT)), c.getString(c.getColumnIndex(DBHelper.F_VENUELNG))};
                i.putExtra("extras", extras);
                startActivity(i);
            }
        });

        dbh = new DBHelper(this);
        c = dbh.favoritesName();

        from = new String[] {DBHelper.F_ID, DBHelper.F_VENUENAME, DBHelper.F_VENUEADDRESS, DBHelper.F_VENUERATING, DBHelper.F_VENUEDISTANCE};
        to = new int[] {0, R.id.bar, R.id.address, R.id.rating, R.id.distance};

        MyCursorAdapter sca = new MyCursorAdapter(this, c, true);
        lv.setAdapter(sca);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    // Nos boutons dans le menu qui représentent les différentes façons de trier.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.sort_alpha) {
            lv.setAdapter(new MyCursorAdapter(this, dbh.favoritesName(), true));
        }
        else if(id == R.id.sort_city) {
            lv.setAdapter(new MyCursorAdapter(this, dbh.favoritesCity(), true));
        }
        else if(id == R.id.sort_distance) {
            lv.setAdapter(new MyCursorAdapter(this, dbh.favoritesDistance(), true));
        }
        else if(id == R.id.sort_rating) {
            lv.setAdapter(new MyCursorAdapter(this, dbh.favoritesRating(), true));
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyCursorAdapter extends CursorAdapter implements View.OnClickListener {

        LayoutInflater inflater;

        public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // Remplit notre curseur avec les informations pertinentes.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cursor c=this.getCursor();
            c.moveToPosition(position);

            View v=convertView;
            if( v==null ) {
                v=inflater.inflate(R.layout.favorites_layout,null);
            }

            TextView tvbar = (TextView) v.findViewById(R.id.bar);
            TextView tvaddress = (TextView) v.findViewById(R.id.address);
            TextView tvrating = (TextView) v.findViewById(R.id.rating);
            TextView tvdistance = (TextView) v.findViewById(R.id.distance);

            ImageView ib = (ImageView) v.findViewById(R.id.b_fav);

            tvbar.setText(c.getString(c.getColumnIndex(DBHelper.F_VENUENAME)));
            tvaddress.setText(c.getString(c.getColumnIndex(DBHelper.F_VENUEADDRESS)));
            tvrating.setText(c.getString(c.getColumnIndex(DBHelper.F_VENUERATING)));
            tvdistance.setText(c.getString(c.getColumnIndex(DBHelper.F_VENUEDISTANCE)));

            ib.setTag(new Integer(position));
            ib.setOnClickListener(this);

            return v;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

        @Override
        public void onClick(View v) {
            int pos = ((Integer)v.getTag()).intValue();
            dbh.remove(pos);
            Toast.makeText(FavoritesActivity.this, "Removed from your Favorites!", Toast.LENGTH_LONG).show();
        }
    }
}