package com.qwert2603.spenddemo.changes_list.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.model.Change;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangesAdapter extends RecyclerView.Adapter<ChangesAdapter.ChangeViewHolder> {

    private String[] mChangeKinds;

    private List<Change> mChangeList;

    public ChangesAdapter(Context context, List<Change> changeList) {
        mChangeKinds = context.getResources().getStringArray(R.array.change_kinds);
        mChangeList = changeList;
    }

    @SuppressLint("InflateParams")
    @Override
    public ChangeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ChangeViewHolder(layoutInflater.inflate(R.layout.item_change, null));
    }

    @Override
    public void onBindViewHolder(ChangeViewHolder holder, int position) {
        holder.bind(mChangeList.get(position));
    }

    @Override
    public int getItemCount() {
        return mChangeList.size();
    }

    class ChangeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.kind_text_view)
        TextView mKindTextView;

        @BindView(R.id.record_id_text_view)
        TextView mRecordIdTextView;

        public ChangeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(ChangeViewHolder.this, itemView);
        }

        public void bind(Change change) {
            mKindTextView.setText(mChangeKinds[change.getKind()]);
            mRecordIdTextView.setText(String.valueOf(change.getRecordId()));
        }
    }
}
