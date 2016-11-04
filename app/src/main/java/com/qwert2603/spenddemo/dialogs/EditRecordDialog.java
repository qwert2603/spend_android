package com.qwert2603.spenddemo.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.data_manager.DataManager;
import com.qwert2603.spenddemo.data_manager.RealmHelper;
import com.qwert2603.spenddemo.model.Id;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.utils.DateUtils;
import com.qwert2603.spenddemo.utils.LogUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.realm.Realm;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class EditRecordDialog extends DialogFragment {

    public static final String EXTRA_RECORD_ID = "com.qwert2603.spenddemo.dialogs.EditRecordDialog.EXTRA_RECORD_ID";

    public static EditRecordDialog createForEdit(int recordId) {
        EditRecordDialog editRecordDialog = new EditRecordDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_RECORD_ID, recordId);
        editRecordDialog.setArguments(args);
        return editRecordDialog;
    }

    public static EditRecordDialog createForInserting() {
        EditRecordDialog editRecordDialog = new EditRecordDialog();
        Bundle args = new Bundle();
        editRecordDialog.setArguments(args);
        return editRecordDialog;
    }

    @BindView(R.id.kind)
    EditText mKind;

    @BindView(R.id.value)
    EditText mValue;

    @BindView(R.id.date)
    EditText mDate;

    private Realm mRealm;

    private Record mRecord;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private boolean mIsInserting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        Bundle arguments = getArguments();
        mIsInserting = !arguments.containsKey(EXTRA_RECORD_ID);
        if (mIsInserting) {
            int recordId = RealmHelper.getNextRecordId(mRealm);
            LogUtils.d("EditRecordDialog#recordId == " + recordId);
            mRecord = new Record();
            mRecord.setId(recordId);
            mRecord.setDate(new Date());
        } else {
            int recordId = arguments.getInt(EXTRA_RECORD_ID);
            Record realmRecord = mRealm.where(Record.class).equalTo("mId", recordId).findFirst();
            mRecord = mRealm.copyFromRealm(realmRecord);
        }
    }

    @Override
    public void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_insert, null);
        ButterKnife.bind(this, view);

        RxTextView.text(mKind).call(mRecord.getKind());
        RxTextView.text(mValue).call(String.valueOf(mRecord.getValue()));
        RxTextView.text(mDate).call(DateUtils.formatDate(mRecord.getDate()));

        Subscription subscription = RxTextView.textChanges(mKind)
                .subscribe(charSequence -> mRecord.setKind(charSequence.toString()));
        mCompositeSubscription.add(subscription);

        Subscription subscription1 = RxView.longClicks(mKind)
                .subscribe(aVoid -> {
                    ChooseKindDialog chooseKindDialog = ChooseKindDialog.newInstance();
                    chooseKindDialog.setTargetFragment(EditRecordDialog.this, 1);
                    chooseKindDialog.show(getFragmentManager(), chooseKindDialog.getClass().getName());
                });
        mCompositeSubscription.add(subscription1);

        Subscription subscription2 = RxTextView.textChanges(mValue)
                .subscribe(charSequence -> mRecord
                        .setValue(charSequence.toString().equals("") ? 0 : Integer.parseInt(charSequence.toString())));
        mCompositeSubscription.add(subscription2);

        Subscription subscription3 = RxTextView.textChanges(mDate)
                .subscribe(charSequence -> {
                    try {
                        mRecord.setDate(DateUtils.createDate(charSequence.toString()));
                    } catch (Exception ignored) {
                    }
                });
        mCompositeSubscription.add(subscription3);

        view.requestFocus();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("ok", (dialog, which) -> {
                    Single<Id> single;
                    if (mIsInserting) {
                        single = new DataManager().insertRecord(mRecord);
                    } else {
                        single = new DataManager().updateRecord(mRecord);
                    }
                    single.subscribe(id -> {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_RECORD_ID, id.getId());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        dismissAllowingStateLoss();
                    }, LogUtils::e);
                })
                .setNegativeButton("cancel", null)
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || requestCode != 1) {
            return;
        }
        String kind = data.getStringExtra(ChooseKindDialog.EXTRA_KIND);
        mKind.setText(kind);
        mValue.requestFocus();
    }

    @Override
    public void onDestroyView() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = new CompositeSubscription();
        super.onDestroyView();
    }
}
