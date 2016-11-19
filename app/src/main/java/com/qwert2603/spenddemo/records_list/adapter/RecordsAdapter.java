package com.qwert2603.spenddemo.records_list.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.qwert2603.spenddemo.base.ViewType;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter.Click;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter.LongClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ViewType> mItems = new ArrayList<>();

    @SuppressLint("UseSparseArrays")
    private Map<Integer, ViewTypeDelegateAdapter> mAdapters = new HashMap<>();

    private Subject<Click> mClickSubject = PublishSubject.<Click>create().toSerialized();
    private Subject<LongClick> mLongClickSubject = PublishSubject.<LongClick>create().toSerialized();

    public RecordsAdapter() {
        RecordDelegateAdapter recordDelegateAdapter = new RecordDelegateAdapter();
        mAdapters.put(ViewType.RECORD, recordDelegateAdapter);

        for (Map.Entry<Integer, ViewTypeDelegateAdapter> adapterEntry : mAdapters.entrySet()) {
            adapterEntry.getValue().getClickObservable().subscribe(mClickSubject);
            adapterEntry.getValue().getLongClickObservable().subscribe(mLongClickSubject);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapters.get(viewType).onCreateViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mAdapters.get(getItemViewType(position)).onBindViewHolder(holder, mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    public void setItems(List<ViewType> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public Observable<Click> getClickObservable() {
        return mClickSubject;
    }

    public Observable<LongClick> getLongClickObservable() {
        return mLongClickSubject;
    }

    public List<ViewType> getItems() {
        return mItems;
    }
}
