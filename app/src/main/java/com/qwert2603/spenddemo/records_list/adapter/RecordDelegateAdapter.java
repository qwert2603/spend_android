package com.qwert2603.spenddemo.records_list.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.base.ViewType;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.utils.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RecordDelegateAdapter implements ViewTypeDelegateAdapter {

    private Subject<Click, Click> mClickSubject = new SerializedSubject<>(PublishSubject.create());
    private Subject<LongClick, LongClick> mLongClickSubject = new SerializedSubject<>(PublishSubject.create());

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new RecordViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, ViewType viewType) {
        RecordViewHolder recordViewHolder = (RecordViewHolder) viewHolder;
        Record record = (Record) viewType;
        recordViewHolder.mModelId = record.getId();
        recordViewHolder.mIdTextView.setText(String.valueOf(record.getId()));
        recordViewHolder.mDateTextView.setText(DateUtils.formatDate(record.getDate()));
        recordViewHolder.mKindTextView.setText(record.getKind());
        recordViewHolder.mValueTextView.setText(String.valueOf(record.getValue()));
    }

    @Override
    public Observable<Click> getClickObservable() {
        return mClickSubject;
    }

    @Override
    public Observable<LongClick> getLongClickObservable() {
        return mLongClickSubject;
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {

        int mModelId;

        @BindView(R.id.id_text_view)
        TextView mIdTextView;

        @BindView(R.id.date_text_view)
        TextView mDateTextView;

        @BindView(R.id.kind)
        TextView mKindTextView;

        @BindView(R.id.value)
        TextView mValueTextView;

        RecordViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            RxView.clicks(itemView)
                    .map(aVoid -> new Click(mModelId, getAdapterPosition()))
                    .subscribe(mClickSubject);

            RxView.longClicks(itemView)
                    .map(aVoid -> new LongClick(mModelId, getAdapterPosition()))
                    .subscribe(mLongClickSubject);
        }
    }
}
