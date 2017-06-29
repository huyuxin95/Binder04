package com.jju.yuxin.binder04;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BookManagerService extends Service {
    private static final String TAG = BookManagerService.class.getSimpleName();
    private CopyOnWriteArrayList<Book> mbooklist=new CopyOnWriteArrayList<>();
    private AtomicBoolean isstop=new AtomicBoolean(false);
    private AtomicInteger book_flag=new AtomicInteger(0);
    private RemoteCallbackList<IOnBookArrivedListener> callbackList=new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mbooklist.add(new Book(1,"MAN BOOK"));
        mbooklist.add(new Book(2,"WOMEN BOOK"));
        mbooklist.add(new Book(3,"Baby BOOK"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isstop.get()){
                    try {
                        Thread.sleep(2000);
                        newBookArried(new Book(book_flag.incrementAndGet(),"Book Name is AutoAdd"+book_flag));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void newBookArried(Book book) {
        mbooklist.add(book);
        int size = callbackList.beginBroadcast();
        for(int i=0;i<size;i++){
            try {
                callbackList.getBroadcastItem(i).newBookArrived(book);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        callbackList.finishBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  binder;
    }

    private IBookManager.Stub binder=new IBookManager.Stub(){
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //做权限校验
            String[] packagesForUid = getPackageManager().getPackagesForUid(getCallingUid());
            if(packagesForUid!=null&&packagesForUid.length>0){
                if(!packagesForUid[0].startsWith("com.jju")){
                    return false;
                }
            }else{
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public void addBook(Book book) throws RemoteException {

            newBookArried(book);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {

            return mbooklist;
        }

        @Override
        public void registerListener(IOnBookArrivedListener listener) throws RemoteException {
            boolean register = callbackList.register(listener);
            if (register){
                Log.d(TAG,"add register success");
            }else{
                throw new IllegalStateException("监听添加失败！");
            }
        }

        @Override
        public void unregisterListener(IOnBookArrivedListener listener) throws RemoteException {
            boolean unregister = callbackList.unregister(listener);
            if (unregister){
                Log.d(TAG,"unregister listener success");
            }else{
                throw new IllegalStateException("取消监听失败！");
            }

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        isstop.set(true);
        callbackList.kill();
    }
}
