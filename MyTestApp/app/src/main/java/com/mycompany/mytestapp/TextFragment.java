package com.mycompany.mytestapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TextFragment extends Fragment {

    public TextFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        Bundle bundle = getArguments();

        DatabaseClass mydb = new DatabaseClass(getActivity());
        SQLiteDatabase db = mydb.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Posts WHERE _id = ?", new String[]{new Integer(bundle.getInt("id")).toString()});
        c.moveToFirst();

        TextView temp = (TextView)view.findViewById(R.id.label_id_value);
        temp.setText(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_ID)));
        temp = (TextView)view.findViewById(R.id.label_user_id_value);
        temp.setText(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_USER_ID)));
        temp = (TextView)view.findViewById(R.id.label_title_value);
        temp.setText(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_TITLE)));
        temp = (TextView)view.findViewById(R.id.label_post);
        temp.setText(c.getString(c.getColumnIndexOrThrow(DatabaseClass.PostsTable.COLUMN_NAME_BODY)));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
