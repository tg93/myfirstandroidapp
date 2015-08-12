package com.mycompany.mytestapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewPostFragment extends Fragment implements View.OnClickListener{

    View view;


    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_post, container, false);

        Button b1 = (Button)view.findViewById(R.id.button_add_post);
        b1.setOnClickListener(this);
        Button b2 = (Button)view.findViewById(R.id.button_clear);
        b2.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_add_post:
                addPost();
                break;
            case R.id.button_clear:
                clearFields();
                break;
        }
    }

    private void clearFields() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText tab[] = new EditText[3];
                tab[0] = (EditText) getView().findViewById(R.id.text_user_id_value_newPost);
                tab[1] = (EditText) getView().findViewById(R.id.text_title_value_newPost);
                tab[2] = (EditText) getView().findViewById(R.id.text_post_newPost);
                for (int i = 0; i < tab.length; i++) {
                    tab[i].setText("");
                }
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

    private void addPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Sender().execute();
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

    private class Sender extends AsyncTask<Void, Void, DownloadedPost> {

        @Override
        protected DownloadedPost doInBackground(Void... params) {
            try {
                final String url = "http://jsonplaceholder.typicode.com/posts";
                EditText tab[] = new EditText[3];
                tab[0] = (EditText) getView().findViewById(R.id.text_user_id_value_newPost);
                tab[1] = (EditText) getView().findViewById(R.id.text_title_value_newPost);
                tab[2] = (EditText) getView().findViewById(R.id.text_post_newPost);
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                String uid = tab[0].getText().toString();
                String title = tab[1].getText().toString();
                String body = tab[2].getText().toString();
                if(uid == "") uid = "0";
                if(title == "") title = "-Example title-";
                if(body == "") body = "-Example\nmultiline body-";
                if (((MainActivity) getActivity()).isOnline()) {
                    DownloadedPost postToSend = new DownloadedPost(null, uid, title, body);
                    System.out.println(restTemplate.postForObject(url, postToSend, DownloadedPost.class));
                    ((MainActivity) getActivity()).downloadMorePosts(0); // parametr 0 == refresh
//                --nie wywalalo bledu--
//                MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//                map.add("user_id", tab[0].getText().toString());
//                map.add("title", tab[1].getText().toString());
//                map.add("body", tab[2].getText().toString());
//                restTemplate.postForObject(url, map, String.class);
                }
                else{
                    DatabaseClass mydb = new DatabaseClass(getActivity());
                    SQLiteDatabase db = mydb.getWritableDatabase();
                    ContentValues values = new ContentValues();

                    values.put(DatabaseClass.ToSendTable.COLUMN_NAME_USER_ID, tab[0].getText().toString());
                    values.put(DatabaseClass.ToSendTable.COLUMN_NAME_TITLE, tab[1].getText().toString());
                    values.put(DatabaseClass.ToSendTable.COLUMN_NAME_BODY, tab[2].getText().toString());

                    db.insert(DatabaseClass.ToSendTable.TABLE_NAME, null, values);
                    mydb.close();
                    db.close();
                }

            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }
    }
}
