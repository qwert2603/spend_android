package com.qwert2603.spenddemo.base;

import android.app.Fragment;
import android.content.Intent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxFragment extends Fragment {

    public static class Args {
        public int requestCode;
        public int resultCode;
        public Intent data;

        Args(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    private Subject<Args> mOnActivityResultSubject = PublishSubject.<Args>create().toSerialized();

    public Observable<Args> getOnActivityResultObservable() {
        return mOnActivityResultSubject;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mOnActivityResultSubject.onNext(new Args(requestCode, resultCode, data));
    }

    @Override
    public void onDestroy() {
        mOnActivityResultSubject.onComplete();
        super.onDestroy();
    }
}
