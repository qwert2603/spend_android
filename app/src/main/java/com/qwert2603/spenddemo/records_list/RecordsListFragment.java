package com.qwert2603.spenddemo.records_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.view.RxView;
import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.base.RxFragment;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter;
import com.qwert2603.spenddemo.changes_list.ChangesListActivity;
import com.qwert2603.spenddemo.data_manager.DataManager;
import com.qwert2603.spenddemo.dialogs.EditRecordDialog;
import com.qwert2603.spenddemo.dialogs.QuestionDialog;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.records_list.adapter.RecordsAdapter;
import com.qwert2603.spenddemo.utils.LogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RecordsListFragment extends RxFragment {

    private static final int REQUEST_INSERT_RECORD = 1;
    private static final int REQUEST_EDIT_RECORD = 2;
    private static final int REQUEST_DELETE_RECORD = 3;

    public RecordsListFragment() {
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private RecordsAdapter mRecordsAdapter;

    private DataManager mDataManager = new DataManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecordsAdapter = new RecordsAdapter();
        mRecyclerView.setAdapter(mRecordsAdapter);

        showRefresh();
        Disposable disposable5 = mDataManager.getAllRecords()
                .subscribe(
                        items -> {
                            LogUtils.d("mDataManager.getAllRecords()_mRecordsAdapter.setItems(items);");
                            mRecordsAdapter.setItems(items);
                            hideRefresh();
                        },
                        throwable -> {
                            View view1 = getView();
                            if (view1 != null) {
                                Snackbar.make(view1, throwable.toString(), Snackbar.LENGTH_LONG).show();
                            }
                            hideRefresh();
                        }
                );
        mCompositeDisposable.add(disposable5);

        Subscription subscription = RxView.clicks(mFloatingActionButton)
                .subscribe(
                        aVoid -> {
                            EditRecordDialog editRecordDialog = EditRecordDialog.createForInserting();
                            editRecordDialog.setTargetFragment(RecordsListFragment.this, REQUEST_INSERT_RECORD);
                            editRecordDialog.show(getFragmentManager(), editRecordDialog.getClass().getName() + REQUEST_INSERT_RECORD);
                        },
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show()
                );
        mCompositeSubscription.add(subscription);

        Disposable disposable4 = mRecordsAdapter.getClickObservable()
                .map(click -> mRecordsAdapter.getItems().get(click.mPosition))
                .filter(viewType -> viewType instanceof Record)
                .map(viewType -> ((Record) viewType))
                .subscribe(dataBaseRecord -> {
                    EditRecordDialog editRecordDialog = EditRecordDialog.createForEdit(dataBaseRecord.getId());
                    editRecordDialog.setTargetFragment(RecordsListFragment.this, REQUEST_EDIT_RECORD);
                    editRecordDialog.show(getFragmentManager(), editRecordDialog.getClass().getName() + REQUEST_EDIT_RECORD);
                });
        mCompositeDisposable.add(disposable4);

        Disposable disposable3 = mRecordsAdapter.getLongClickObservable()
                .filter(longClick -> mRecordsAdapter.getItems().get(longClick.mPosition) instanceof Record)
                .subscribe(longClick -> {
                    Record record = (Record) mRecordsAdapter.getItems().get(longClick.mPosition);
                    String question = "Delete?\n" + record.toStringLines();
                    QuestionDialog questionDialog = QuestionDialog.create(question, longClick);
                    questionDialog.setTargetFragment(RecordsListFragment.this, REQUEST_DELETE_RECORD);
                    questionDialog.show(getFragmentManager(), questionDialog.getClass().getName());
                });
        mCompositeDisposable.add(disposable3);

        Disposable disposable = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_INSERT_RECORD)
                .flatMap(o -> mDataManager.getAllRecords().toObservable())
                .subscribe((items) -> {
                            LogUtils.d("REQUEST_INSERT_RECORD_mRecordsAdapter.setItems(items);");
                            mRecordsAdapter.setItems(items);
                        },
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeDisposable.add(disposable);


        Disposable disposable1 = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_EDIT_RECORD)
                .flatMap(o -> mDataManager.getAllRecords().toObservable())
                .subscribe((items) -> {
                            LogUtils.d("REQUEST_EDIT_RECORD_mRecordsAdapter.setItems(items);");
                            mRecordsAdapter.setItems(items);
                        },
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeDisposable.add(disposable1);


        Disposable disposable2 = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_DELETE_RECORD)
                .map(args -> ((ViewTypeDelegateAdapter.LongClick) args.data.getParcelableExtra(QuestionDialog.EXTRA_OBJECT)))
                .flatMap(longClick -> mDataManager.removeRecord(longClick.mId).toObservable(), (longClick, o) -> longClick)
                .flatMap(o -> mDataManager.getAllRecords().toObservable())
                .subscribe(records -> {
                            LogUtils.d("REQUEST_DELETE_RECORD_mRecordsAdapter.setItems(items);");
                            mRecordsAdapter.setItems(records);
                        },
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeDisposable.add(disposable2);


        Disposable disposable6 = RxJavaInterop.toV2Observable(RxSwipeRefreshLayout.refreshes(mRefreshLayout))
//                .filter(aVoid -> {
//                    ConnectivityManager connectivityManager =
//                            (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
//                    boolean isInternetConnected = connectivityManager.getActiveNetworkInfo() != null
//                            && connectivityManager.getActiveNetworkInfo().isConnected();
//                    if (!isInternetConnected) {
//                        hideRefresh();
//                    }
//                    return isInternetConnected;
//                })
                .flatMap(o -> mDataManager.getAllRecords().toObservable())
                .subscribe(
                        items -> {
                            hideRefresh();
                            LogUtils.d("RxSwipeRefreshLayout_mRecordsAdapter.setItems(items);");
                            mRecordsAdapter.setItems(items);
                        },
                        throwable -> {
                            hideRefresh();
                            Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show();
                        });
        mCompositeDisposable.add(disposable6);

        return view;
    }

    @Override
    public void onDestroyView() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = new CompositeSubscription();
        mCompositeDisposable.dispose();
        mCompositeDisposable = new CompositeDisposable();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.records_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_local_changes) {
            startActivity(new Intent(getActivity(), ChangesListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRefresh() {
        RxSwipeRefreshLayout.refreshing(mRefreshLayout).call(true);
    }

    private void hideRefresh() {
        RxSwipeRefreshLayout.refreshing(mRefreshLayout).call(false);
    }

}
