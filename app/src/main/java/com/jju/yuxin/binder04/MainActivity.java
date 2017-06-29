package com.jju.yuxin.binder04;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0){
                Book book = (Book) msg.obj;
                Log.d(TAG,"I head get new book:" +book.toString());
            }

        }
    };

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button bt_connect;
    private Button bt_disconnect;
    private Button bt_addbook;
    private Button bt_getbooklist;
    private int book_flag=0;
    private IBookManager iBookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_connect = (Button) this.findViewById(R.id.bt_connect);
        bt_disconnect = (Button) this.findViewById(R.id.bt_disconnect);
        bt_addbook = (Button) this.findViewById(R.id.bt_addbook);
        bt_getbooklist = (Button) this.findViewById(R.id.bt_getbooklist);
        ButtonClickListener listener=new ButtonClickListener();
         bt_connect .setOnClickListener(listener);
         bt_disconnect.setOnClickListener(listener);
         bt_addbook .setOnClickListener(listener);
         bt_getbooklist.setOnClickListener(listener);
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_connect:
                    //连接
                    Intent intent=new Intent(MainActivity.this,BookManagerService.class);
                    bindService(intent,serviceConnection,BIND_AUTO_CREATE);
                    break;
                case R.id.bt_disconnect:
                    //断开连接
                    try{
                        iBookManager.asBinder().unlinkToDeath(deathRecipient,0);
                        unbindService(serviceConnection);
                    }catch (Exception e){
                        Log.e(TAG,"请先开启服务！");
                    }
                    break;
                case R.id.bt_addbook:
                    //添加书本
                    Book book=new Book(book_flag++,"Click Add Book:"+book_flag);
                    try {
                        iBookManager.addBook(book);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.bt_getbooklist:
                    //获取书本
                    try {
                        List<Book> bookList = iBookManager.getBookList();
                        for (Book book_item: bookList) {
                            Log.d(TAG,book_item.toString());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private ServiceConnection serviceConnection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBookManager = IBookManager.Stub.asInterface(service);
            try {
                iBookManager.asBinder().linkToDeath(deathRecipient,0);
                iBookManager.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"onServiceDisconnected,ComponentName:"+name.getClassName());
        }
    };
    private IBinder.DeathRecipient deathRecipient=new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            try {
                iBookManager.unregisterListener(listener);
                iBookManager.asBinder().unlinkToDeath(deathRecipient,0);
                //连接
                Intent intent=new Intent(MainActivity.this,BookManagerService.class);
                bindService(intent,serviceConnection,BIND_AUTO_CREATE);

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }
    };
    private IOnBookArrivedListener listener=new IOnBookArrivedListener.Stub(){

        @Override
        public void newBookArrived(Book book) throws RemoteException {
            Message msg=new Message();
            msg.what=0;
            msg.obj=book;
            mhandler.sendMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            iBookManager.unregisterListener(listener);
            unbindService(serviceConnection);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}


