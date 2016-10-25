package com.qwert2603.spenddemo.records_list;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.view.RxView;
import com.qwert2603.spenddemo.R;
import com.qwert2603.spenddemo.base.RxFragment;
import com.qwert2603.spenddemo.base.ViewTypeDelegateAdapter;
import com.qwert2603.spenddemo.data_manager.DataManager;
import com.qwert2603.spenddemo.dialogs.EditRecordDialog;
import com.qwert2603.spenddemo.dialogs.QuestionDialog;
import com.qwert2603.spenddemo.model.Change;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.records_list.adapter.RecordsAdapter;
import com.qwert2603.spenddemo.utils.LogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
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

    private RecordsAdapter mRecordsAdapter;

    private DataManager mDataManager = new DataManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecordsAdapter = new RecordsAdapter();
        mRecyclerView.setAdapter(mRecordsAdapter);

        showRefresh();
        Subscription subscription5 = mDataManager.getRecordsAndDates()
                .subscribe(
                        items -> {
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
        mCompositeSubscription.add(subscription5);

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

        // TODO: 24.10.16 make activity for changes list
        mFloatingActionButton.setOnLongClickListener(v -> {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Record> records = realm.where(Record.class).findAll();
            for (Record record : records) {
                LogUtils.d(record.toString());
            }
            RealmResults<Change> changes = realm.where(Change.class).findAll();
            for (Change change : changes) {
                LogUtils.d(change.toString());
            }
            realm.close();
            return true;
        });

        Subscription subscription4 = mRecordsAdapter.getClickObservable()
                .map(click -> mRecordsAdapter.getItems().get(click.mPosition))
                .filter(viewType -> viewType instanceof Record)
                .map(viewType -> ((Record) viewType))
                .subscribe(dataBaseRecord -> {
                    EditRecordDialog editRecordDialog = EditRecordDialog.createForEdit(dataBaseRecord.getId());
                    editRecordDialog.setTargetFragment(RecordsListFragment.this, REQUEST_EDIT_RECORD);
                    editRecordDialog.show(getFragmentManager(), editRecordDialog.getClass().getName() + REQUEST_EDIT_RECORD);
                });
        mCompositeSubscription.add(subscription4);

        Subscription subscription1 = mRecordsAdapter.getLongClickObservable()
                .filter(longClick -> mRecordsAdapter.getItems().get(longClick.mPosition) instanceof Record)
                .subscribe(longClick -> {
                    Record record = (Record) mRecordsAdapter.getItems().get(longClick.mPosition);
                    String question = "Delete?\n" + record.toStringLines();
                    QuestionDialog questionDialog = QuestionDialog.create(question, longClick);
                    questionDialog.setTargetFragment(RecordsListFragment.this, REQUEST_DELETE_RECORD);
                    questionDialog.show(getFragmentManager(), questionDialog.getClass().getName());
                });
        mCompositeSubscription.add(subscription1);

        Subscription subscription2 = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_INSERT_RECORD)
                .flatMap(o -> mDataManager.getRecordsAndDates())
                .subscribe(mRecordsAdapter::setItems,
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeSubscription.add(subscription2);

        Subscription subscription6 = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_EDIT_RECORD)
                .flatMap(o -> mDataManager.getRecordsAndDates())
                .subscribe(mRecordsAdapter::setItems,
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeSubscription.add(subscription6);

        Subscription subscription7 = getOnActivityResultObservable()
                .filter(args -> args.resultCode == Activity.RESULT_OK)
                .filter(args -> args.requestCode == REQUEST_DELETE_RECORD)
                .map(args -> ((ViewTypeDelegateAdapter.LongClick) args.data.getParcelableExtra(QuestionDialog.EXTRA_OBJECT)))
                .flatMap(longClick -> mDataManager.removeRecord(longClick.mId), (longClick, o) -> longClick)
                .flatMap(o -> mDataManager.getRecordsAndDates(), Pair::new)
                .subscribe(pair -> mRecordsAdapter.setItemsAndNotifyRemoved(pair.second, pair.first.mPosition),
                        throwable -> Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show());
        mCompositeSubscription.add(subscription7);

        Subscription subscription3 = RxSwipeRefreshLayout.refreshes(mRefreshLayout)
                .filter(aVoid -> {
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
                    boolean isInternetConnected = connectivityManager.getActiveNetworkInfo() != null
                            && connectivityManager.getActiveNetworkInfo().isConnected();
                    if (!isInternetConnected) {
                        hideRefresh();
                    }
                    return isInternetConnected;
                })
                .flatMap(o -> mDataManager.getRecordsAndDates())
                .subscribe(
                        items -> {
                            hideRefresh();
                            mRecordsAdapter.setItems(items);
                        },
                        throwable -> {
                            hideRefresh();
                            Snackbar.make(view, throwable.toString(), Snackbar.LENGTH_LONG).show();
                        });
        mCompositeSubscription.add(subscription3);

        return view;
    }

    @Override
    public void onDestroyView() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = new CompositeSubscription();
        super.onDestroyView();
    }

    private void showRefresh() {
        RxSwipeRefreshLayout.refreshing(mRefreshLayout).call(true);
    }

    private void hideRefresh() {
        RxSwipeRefreshLayout.refreshing(mRefreshLayout).call(false);
    }

}
