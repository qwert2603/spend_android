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
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

class RecordDelegateAdapter implements ViewTypeDelegateAdapter {

    private Subject<Click> mClickSubject = PublishSubject.<Click>create().toSerialized();
    private Subject<LongClick> mLongClickSubject = PublishSubject.<LongClick>create().toSerialized();

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

            rx.Observable<Click> clickObservable = RxView.clicks(itemView)
                    .map(aVoid -> new Click(mModelId, getAdapterPosition()));

            RxJavaInterop.toV2Observable(clickObservable)
                    .subscribe(mClickSubject);

            rx.Observable<LongClick> longClickObservable = RxView.longClicks(itemView)
                    .map(aVoid -> new LongClick(mModelId, getAdapterPosition()));

            RxJavaInterop.toV2Observable(longClickObservable)
                    .subscribe(mLongClickSubject);
        }
    }
}
