// IOnBookArrivedListener.aidl
package com.jju.yuxin.binder04;

import com.jju.yuxin.binder04.Book;
// Declare any non-default types here with import statements

interface IOnBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void newBookArrived(in Book book);
}
