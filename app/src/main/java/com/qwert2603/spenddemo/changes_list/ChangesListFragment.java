package com.qwert2603.spenddemo.changes_list;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.changes_list.adapter.ChangesAdapter;
import com.qwert2603.spenddemo.data_manager.DataManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ChangesListFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private DataManager mDataManager = new DataManager();

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_changes_list, container, false);
        ButterKnife.bind(ChangesListFragment.this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Disposable disposable = mDataManager.getAllChanges()
                .subscribe(changes -> {
                            if (changes.isEmpty()) {
                                View view1 = getView();
                                if (view1 != null) {
                                    Snackbar.make(view1, R.string.no_local_changes, Snackbar.LENGTH_LONG).show();
                                }
                            } else {
                                mRecyclerView.setAdapter(new ChangesAdapter(getActivity(), changes));
                            }
                        },
                        throwable -> {
                            View view1 = getView();
                            if (view1 != null) {
                                Snackbar.make(view1, throwable.toString(), Snackbar.LENGTH_LONG).show();
                            }
                        });
        mCompositeDisposable.add(disposable);

        return view;
    }

    @Override
    public void onDestroyView() {
        mCompositeDisposable.dispose();
        mCompositeDisposable = new CompositeDisposable();
        super.onDestroyView();
    }
}
