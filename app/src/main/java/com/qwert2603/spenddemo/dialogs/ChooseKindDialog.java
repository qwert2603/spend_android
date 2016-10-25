package com.qwert2603.spenddemo.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.data_manager.DataManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseKindDialog extends DialogFragment {

    public static final String EXTRA_KIND = "com.qwert2603.spenddemo.EXTRA_KIND";

    public static ChooseKindDialog newInstance() {
        return new ChooseKindDialog();
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_kind, null);
        ButterKnife.bind(ChooseKindDialog.this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        new DataManager().getDistinctKinds()
                .subscribe(kinds -> mRecyclerView.setAdapter(new KindsAdapter(kinds)));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setNegativeButton("cancel", null)
                .create();
    }

    class KindsAdapter extends RecyclerView.Adapter<KindViewHolder> {

        private List<String> mKinds;

        public KindsAdapter(List<String> kinds) {
            mKinds = kinds;
        }

        @SuppressLint("InflateParams")
        @Override
        public KindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kind, null);
            return new KindViewHolder(view);
        }

        @Override
        public void onBindViewHolder(KindViewHolder holder, int position) {
            holder.mTextView.setText(mKinds.get(position));
            holder.mKind = mKinds.get(position);
        }

        @Override
        public int getItemCount() {
            return mKinds.size();
        }
    }

    class KindViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_view)
        TextView mTextView;

        String mKind;

        KindViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(KindViewHolder.this, itemView);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_KIND, mKind);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismissAllowingStateLoss();
            });
        }
    }
}
