package com.qwert2603.spenddemo.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import rx.Observable;

public interface ViewTypeDelegateAdapter {

    class Click {
        public int mId;
        public int mPosition;

        public Click(int id, int position) {
            mId = id;
            mPosition = position;
        }
    }

    class LongClick implements Parcelable {
        public int mId;
        public int mPosition;

        public LongClick(int id, int position) {
            mId = id;
            mPosition = position;
        }

        protected LongClick(Parcel in) {
            mId = in.readInt();
            mPosition = in.readInt();
        }

        public static final Creator<LongClick> CREATOR = new Creator<LongClick>() {
            @Override
            public LongClick createFromParcel(Parcel in) {
                return new LongClick(in);
            }

            @Override
            public LongClick[] newArray(int size) {
                return new LongClick[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mId);
            dest.writeInt(mPosition);
        }
    }

    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);
    void onBindViewHolder(RecyclerView.ViewHolder viewHolder, ViewType viewType);
    Observable<Click> getClickObservable();
    Observable<LongClick> getLongClickObservable();
}
