package com.walmart.gridimagesearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.walmart.gridimagesearch.activities.SearchActivity;

public class EditSettingsFragment extends DialogFragment {

    private Button btnSave;
    private Context context;

    // TODO: Rename and change types and number of parameters
    public static EditSettingsFragment newInstance(String title) {
        EditSettingsFragment dialogFrag = new EditSettingsFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialogFrag.setArguments(args);
        return dialogFrag;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        final View view = inflater.inflate(R.layout.fragment_edit_settings, container);
        Button btnSave = (Button)view.findViewById(R.id.btnSave);
        Button btnCancel = (Button)view.findViewById(R.id.btnCancel);
        final Spinner spSize = (Spinner) view.findViewById(R.id.spImageSize);
        final Spinner spColor = (Spinner) view.findViewById(R.id.spColorFilter);
        final Spinner spType = (Spinner) view.findViewById(R.id.spImageType);
        final EditText etSite = (EditText) view.findViewById(R.id.etSite);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SearchActivity.class);
                // Pass relevant data back as a result
                intent.putExtra("size", spSize.getSelectedItem().toString());
                intent.putExtra("color", spColor.getSelectedItem().toString());
                intent.putExtra("type", spType.getSelectedItem().toString());
                // Activity finished ok, return the data
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
    }

}
