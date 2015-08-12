package com.mycompany.mytestapp;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.Time;
import java.util.ArrayList;

public class PostListFragment extends ListFragment implements AbsListView.OnScrollListener{

    PostListFragmentInterface callback;
    private ArrayList ids = new ArrayList<Integer>();
    private ArrayList titles = new ArrayList<String>();
    boolean isLoading = false;
    int lastItem = 0;
    SharedPreferences sharedPref;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItem = firstVisibleItem + visibleItemCount;
        if(lastItem == totalItemCount && !isLoading && lastItem != 0){
            if(((MainActivity)getActivity()).isOnline()) {
                isLoading = true;
                loadMoreItems();
            }
        }
    }

    private void loadMoreItems() {
        ((MainActivity)getActivity()).downloadMorePosts(sharedPref.getInt(getString(R.string.shared_pref_post_counter), 0));
        getListView().smoothScrollToPosition(sharedPref.getInt(getString(R.string.shared_pref_post_counter),0));
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public interface PostListFragmentInterface {
        void onPostSelected(int position);
    }

    public PostListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getActivity().getActionBar().show();
        DatabaseClass mydb = new DatabaseClass(getActivity());
        SQLiteDatabase db = mydb.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT _id, title FROM Posts", null);
        c.moveToFirst();

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            return;
        }

        int layout = android.R.layout.simple_list_item_activated_1;
        String filter = "";
        Bundle bundle = getArguments();
        if (bundle != null) {
            ids.clear();
            titles.clear();
            filter = bundle.getString("filter");
            if (!filter.equals("")) {
                for (int i = 0; i < c.getCount(); i++) {
                    if (c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_TITLE)).contains(filter)) {
                        ids.add(c.getInt(c.getColumnIndexOrThrow(DatabaseClass.PostsTable._ID)));
                        titles.add(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_TITLE)));
                    }
                    c.moveToNext();
                }
            } else {
                for (int i = 0; i < c.getCount(); i++) {
                    ids.add(c.getInt(c.getColumnIndexOrThrow(DatabaseClass.PostsTable._ID)));
                    titles.add(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_TITLE)));
                    c.moveToNext();
                }
            }
            setListAdapter(new ArrayAdapter<String>(getActivity(), layout, titles));
            db.close();
            mydb.close();
            c.close();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPref = getActivity().getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnScrollListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            callback = (PostListFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        callback.onPostSelected((Integer) ids.get(position));
        getListView().setItemChecked(position, true);
    }
}
