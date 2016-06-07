package com.example.matusrubicky.detuzo;

import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class ScrollingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    FloatingActionsMenu fab;
    ListView listView;
    Toolbar toolbar;
    FloatingActionButton record;
    FloatingActionButton gpx;
    List<String> listPaths;
    DETUZODatabaseOpenHelper DBHelper = new DETUZODatabaseOpenHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionsMenu) findViewById(R.id.fab);
        record = (FloatingActionButton) findViewById(R.id.record);
        gpx = (FloatingActionButton) findViewById(R.id.addGpx);

        String[] from = {DETUZO.route.NAME, DETUZO.route.TIME, DETUZO.route.SPEED, DETUZO.route.ELEVATION};
        int[] to = {R.id.firstLine, R.id.thirdLine, R.id.secondLine, R.id.fourthLine};

        this.adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                null,
                from,
                to,
                0
        );
        listView.setAdapter(this.adapter);

        listPaths = DBHelper.getAllPaths();

        getLoaderManager().initLoader(0, Bundle.EMPTY, this);

        if (gpx != null) {
            gpx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialFilePicker()
                            .withActivity(ScrollingActivity.this)
                            .withRequestCode(1)
                            .withFilter(Pattern.compile(".*\\.gpx$"))
                            //.withFilter(Pattern.compile(".*\\.xml$"))// Filtering files and directories by file name using regexp
                            .withFilterDirectories(false) // Set directories filterable (false by default)
                            .withHiddenFiles(false) // Show hidden files and folders
                            .start();

                }
            });
        }

        if (record != null) {
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ScrollingActivity.this, GPSLogger.class);
                    startActivity(intent);
                }
            });
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ScrollingActivity.this, MapsActivity.class);
                intent.putExtra("route", getPath(position));

                startActivity(intent);
            }
        });

        if (getIntent().hasExtra("cestaKSuboru")) {
            String value = getIntent().getStringExtra("cestaKSuboru");
            String name = getIntent().getStringExtra("name");
            String cas = getIntent().getExtras().getString("cas");
            String type = getIntent().getExtras().getString("type");

            try {
                ulozDoContentProvidera(name, cas,
                        Logic.calculateDistance(value),
                        Logic.calculateElevation(value),
                        value);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String subor = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            fab.collapseImmediately();

            File from = new File(subor);
            String nazov = subor.substring(subor.lastIndexOf("/") + 1);

            File to = new File(Logic.path);
            try {
                Logic.moveFile(from, to);
            } catch (IOException e) {
                e.printStackTrace();
            }
            subor = to.getAbsolutePath();
            Log.d("SUBORIK", subor);

            subor += "/"+nazov;
            nazov = nazov.substring(0, nazov.length()-4);
            try {
                ulozDoContentProvidera(Logic.parseNameFromGPX(subor),
                        Logic.calculateTime(subor),
                        Logic.calculateDistance(subor),
                        Logic.calculateElevation(subor),
                        subor);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ulozDoContentProvidera(String name, String time, String speed, String elevation, String subor) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DETUZO.route.NAME, name);
        contentValues.put(DETUZO.route.TIME, time);
        contentValues.put(DETUZO.route.SPEED, speed);
        contentValues.put(DETUZO.route.ELEVATION, elevation);
        contentValues.put(DETUZO.route.PATH, subor);

        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                Toast.makeText(ScrollingActivity.this, "Záznam vložený", Toast.LENGTH_LONG).show();
            }
        };

        handler.startInsert(0, null, DETUZO.route.CONTENT_URI, contentValues);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(DETUZO.route.CONTENT_URI);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.adapter.swapCursor(null);
    }

    public String getPath(int index) {
        listPaths = DBHelper.getAllPaths();
        return listPaths.get(index);
    }
}
