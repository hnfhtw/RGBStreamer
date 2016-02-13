package com.hn.rgbstreamer;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

// RGBStreamer - TextinputDialogFragment
// Version: V01
// Last Mofidied: 04.02.2016
// Author: HN (TextInputDialog: http://www.androidinterview.com/android-custom-dialog-box-example-android-dialog/)


public class TextinputDialogFragment extends DialogFragment {
    EditText inputText;
    Button doneButton;
    Button cancelButton;
    static String DialogboxTitle;
    Boolean enableFilter = false;

    public interface TextinputDialogListener {
        void onFinishInputDialog(String inputText);
    }

    // Empty constructor
    public TextinputDialogFragment() {

    }
    // Set title of dialog fragment
    public void setDialogTitle(String title) {
        DialogboxTitle = title;
    }

    // Activate InputFilter for reserved characters (if dialog is used for entering a filename)
    public void inputFilterEnabled(Boolean enFilter) {
        enableFilter = enFilter;
    }

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){

        View view = inflater.inflate(
                R.layout.fragment_textinputdialog, container);

        inputText = (EditText) view.findViewById(R.id.inputText);
        doneButton = (Button) view.findViewById(R.id.doneButton);
        cancelButton = (Button) view.findViewById(R.id.cancelButton);

        // Avoid reserved characters if the input filter is activated
        if(enableFilter == true)
        {
            InputFilter filter = new InputFilter()
            {
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
                {
                    if (source.length() < 1) return null;
                    char last = source.charAt(source.length() - 1);
                    String reservedChars = "?:\"*|/\\<>";
                    if(reservedChars.indexOf(last) > -1) return source.subSequence(0, source.length() - 1);
                    return null;
                }
            };
            inputText.setFilters(new InputFilter[] { filter });
        }

        // Event handler for the positive button
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {

                //---gets the calling activity
                TextinputDialogListener activity = (TextinputDialogListener) getActivity();
                activity.onFinishInputDialog(inputText.getText().toString());

                //---dismiss the alert
                dismiss();
            }
        });

        // Event handler for the negative button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //---gets the calling activity
                TextinputDialogListener activity = (TextinputDialogListener) getActivity();
                activity.onFinishInputDialog(null);

                //---dismiss the alert
                dismiss();
            }
        });

        // Automatically show the keyboard
        inputText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Set the title of the dialog fragment
        getDialog().setTitle(DialogboxTitle);

        return view;
    }
}