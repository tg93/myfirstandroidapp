package com.mycompany.mytestapp;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends FragmentActivity implements PostListFragment.PostListFragmentInterface {

    private FragmentManager fragmentManager = getFragmentManager();
    private String filterString = "";
    private Thread refreshThread, sendThread;
    private Context activityContext = this;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            return;
        }
        if(!isOnline()){

            Bundle bundle = new Bundle();
            bundle.putString("filter", filterString);
            PostListFragment newPostListFragment = new PostListFragment();
            newPostListFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.main_fragment_container, newPostListFragment).commit();
            return;
        }
        PostListFragment postListFragment = new PostListFragment();
        postListFragment.setArguments(getIntent().getExtras());
        fragmentManager.beginTransaction().add(R.id.main_fragment_container, postListFragment).commit();
    }

    @Override
    protected void onStart() {
        sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        super.onStart();
        refreshThread = new Thread(){
            @Override
            public void run() {
                if(isOnline()) {
                    new PostDownloader().execute(0);
                }
                try {
                    sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        refreshThread.start();

        sendThread = new Thread(){
            @Override
            public void run() {
                DatabaseClass mydb = new DatabaseClass(activityContext);
                SQLiteDatabase db = mydb.getReadableDatabase();
                new DatabaseClass(activityContext).onCreate(db);
                Cursor c = db.rawQuery("SELECT * FROM Tosend", null);
                c.moveToFirst();
                int messages = c.getCount();
                for(int i = 0; i < messages;) {
                    if (isOnline()) {
                        int uid = c.getInt(c.getColumnIndexOrThrow(DatabaseClass.ToSendTable.COLUMN_NAME_USER_ID));
                        String title = c.getString(c.getColumnIndexOrThrow(DatabaseClass.ToSendTable.COLUMN_NAME_TITLE));
                        String body = c.getString(c.getColumnIndexOrThrow(DatabaseClass.ToSendTable.COLUMN_NAME_BODY));
                        RestTemplate restTemplate = new RestTemplate();
                        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                        DownloadedPost postToSend = new DownloadedPost(null, Integer.toString(uid), title, body);
                        new PostDownloader().execute(0);
                        db.execSQL("DELETE FROM "+DatabaseClass.ToSendTable.TABLE_NAME+" WHERE "+DatabaseClass.ToSendTable._ID+"="+Integer.toString(c.getInt(c.getColumnIndexOrThrow(DatabaseClass.ToSendTable.COLUMN_NAME_USER_ID))));
                        i++;
                        c.moveToNext();
                    }
                    else{
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mydb.close();
                db.close();
                c.close();
                if(messages > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                    builder.setTitle("Old messages send (" + messages + ")");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                this.interrupt();
            }
        };
        sendThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        refreshThread.interrupt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //todo dokonczyc switch
        switch(item.getItemId()){
            case R.id.action_filter:
                actionFilterFunction();
            case R.id.action_refresh:
                new PostDownloader().execute(0);
                break;
            case R.id.action_add:
                NewPostFragment newPostFragment = new NewPostFragment();
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, newPostFragment).addToBackStack(null).commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void actionFilterFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set filter");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filterString = input.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("filter", filterString);
                PostListFragment newPostListFragment = new PostListFragment();
                newPostListFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, newPostListFragment).commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if( this.getFragmentManager().getBackStackEntryCount() != 0 ){
                this.getFragmentManager().popBackStack();
                getActionBar().show();
                return true;
            }
            // If there are no fragments on stack perform the original back button event
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPostSelected(int position) {
        Bundle bundle = new Bundle();

        bundle.putInt("id", position);
        TextFragment textFragment = new TextFragment();
        textFragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, textFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private class PostDownloader extends AsyncTask<Integer, Void, DownloadedPost[]> {

        @Override
        protected DownloadedPost[] doInBackground(Integer... params) {
            try {
                if(isOnline()) {
                    final String url;
                    if (params[0] != null) {
                        System.out.println("PARAMETR: "+params[0]);
                        url = "http://jsonplaceholder.typicode.com/posts?_start=" + params[0] + "&_end=" + (params[0] + 15) + "&_sort=views&_order=DESC";
                    } else {
                        url = "http://jsonplaceholder.typicode.com/posts/";
                    }
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    DownloadedPost[] downloadedPost = restTemplate.getForObject(url, DownloadedPost[].class);

                    sharedPrefEditor.putInt(getString(R.string.shared_pref_post_counter), params[0]+15);
                    sharedPrefEditor.commit();

                    return downloadedPost;
                }
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(DownloadedPost[] downloadedPost) {
            Bundle bundle = new Bundle();
            DatabaseClass mydb = new DatabaseClass(activityContext);
            SQLiteDatabase db = mydb.getWritableDatabase();
            if(sharedPref.getInt(getString(R.string.shared_pref_post_counter), 15) <= 15){
                db.delete("Posts", null, null);
            }
            ContentValues values = new ContentValues();

            if(downloadedPost != null) {
                for (int i = 0; i < downloadedPost.length; i++) {

                    values.put(DatabaseClass.PostsTable.COLUMN_NAME_ID, downloadedPost[i].getId());
                    values.put(DatabaseClass.PostsTable.COLUMN_NAME_USER_ID, downloadedPost[i].getUserId());
                    values.put(DatabaseClass.PostsTable.COLUMN_NAME_TITLE, downloadedPost[i].getTitle());
                    values.put(DatabaseClass.PostsTable.COLUMN_NAME_BODY, downloadedPost[i].getBody());

                    db.insert(DatabaseClass.PostsTable.TABLE_NAME, null, values);
                }
            }
            bundle.putString("filter", filterString);

            PostListFragment newPostListFragment = new PostListFragment();
            newPostListFragment.setArguments(bundle);
            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); i++){
                fragmentManager.popBackStack();
            }
            fragmentManager.beginTransaction().replace(R.id.main_fragment_container, newPostListFragment).commit();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()){
            return true;
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Connection error");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        return false;
    }

    public void downloadMorePosts(int startPosition){
        new PostDownloader().execute(startPosition);
    }

}