package com.w3engineers.mesh.application.ui.meshlog.ui.base;

import android.view.View;


public interface ItemClickListener<T> {
    /**
     * Called when a item has been clicked.
     *
     * @param view The view that was clicked.
     * @param item The T type object that was clicked.
     */
    void onItemClick(View view, T item);
}
