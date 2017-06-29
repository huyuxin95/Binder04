// IBookManager.aidl
package com.jju.yuxin.binder04;

// Declare any non-default types here with import statements
import com.jju.yuxin.binder04.Book;
import com.jju.yuxin.binder04.IOnBookArrivedListener;

interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void addBook(in Book book);
    List<Book> getBookList();

    void registerListener(IOnBookArrivedListener listener);

    void unregisterListener(IOnBookArrivedListener listener);
}
