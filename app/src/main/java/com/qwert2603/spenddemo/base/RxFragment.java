package com.qwert2603.spenddemo.base;

import android.app.Fragment;
import android.content.Intent;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxFragment extends Fragment {

    public static class Args {
        public int requestCode;
        public int resultCode;
        public Intent data;

        public Args(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    private Subject<Args, Args> mOnActivityResultSubject  = new SerializedSubject<>(PublishSubject.create());

    public Observable<Args> getOnActivityResultObservable() {
        return mOnActivityResultSubject.asObservable();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mOnActivityResultSubject.onNext(new Args(requestCode, resultCode, data));
    }

    @Override
    public void onDestroy() {
        mOnActivityResultSubject.onCompleted();
        super.onDestroy();
    }
}
