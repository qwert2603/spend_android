package com.qwert2603.spenddemo.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class QuestionDialog extends DialogFragment {

    private static final String QUESTION_KEY = "com.qwert2603.spenddemo.QUESTION_KEY";
    public static final String EXTRA_OBJECT = "com.qwert2603.spenddemo.EXTRA_OBJECT";

    public static QuestionDialog create(@NonNull String question, @Nullable Parcelable object) {
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.mQuestion = question;
        questionDialog.mObject = object;
        return questionDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUESTION_KEY, mQuestion);
        if (mObject != null) {
            outState.putParcelable(EXTRA_OBJECT, mObject);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mQuestion = savedInstanceState.getString(QUESTION_KEY);
            mObject = savedInstanceState.getParcelable(EXTRA_OBJECT);
        }
    }

    private String mQuestion;
    private Parcelable mObject;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(mQuestion)
                .setPositiveButton("ok", (dialog, which) -> setResult(true))
                .setNegativeButton("cancel", (dialog, which) -> setResult(false))
                .create();
    }

    private void setResult(boolean ok) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OBJECT, mObject);
        getTargetFragment().onActivityResult(getTargetRequestCode(), ok ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
    }
}
