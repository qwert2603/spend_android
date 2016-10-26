package com.qwert2603.spenddemo.model;

import android.support.annotation.IntDef;

@IntDef(value = {ChangeKind.INSERT, ChangeKind.UPDATE, ChangeKind.DELETE})
public @interface ChangeKind {
    int INSERT = 0;
    int UPDATE = 1;
    int DELETE = 2;
}
