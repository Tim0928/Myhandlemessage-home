package com.avc.app.myhandlemessage;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.room.Room;
import androidx.room.RoomDatabase;

public class MessengerService extends Service {
    private Messenger mActivityMessenger;

    private Handler handler;

    private Messenger mServiceMessenger;
    private static final Random mGenerator = new Random();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TAG", "onCreate()");
       //myAppDatabase=Room.databaseBuilder(getApplicationContext(),(, "bookdb").allowMainThreadQueries().build();

        /**
         * HandlerThread是Android系统专门为Handler封装的一个线程类，
         通过HandlerThread创建的Hanlder便可以进行耗时操作了
         * HandlerThread是一个子线程,在调用handlerThread.getLooper()之前必须先执行
         * HandlerThread的start方法。
         */
        HandlerThread handlerThread = new HandlerThread("serviceCalculate");
        handlerThread.start();


        handler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(mActivityMessenger == null) {
                    mActivityMessenger = msg.replyTo;//訊息 reply
                }
                MyDao bookDao = MainActivity.getmyAppDatabase().myDao();
                Book bookinfo=new Book();
                if(msg.what == 0x11){
                    Log.i("MessengerService","handleMessage");

                    bookinfo.setId(mGenerator.nextInt(100));
                    bookinfo.setBook_name("123"+mGenerator.nextLong());
                    bookinfo.setBook_conext("123");
                    bookDao.addbook(bookinfo);
                    List<Book> books=bookDao.getbook();
                    String booklistview="\n";
                    for(Book book:books){
                        int id=book.getId();
                        String  bookname=book.getBook_name();
                        String  bookcontext=book.getBook_conext();
                        booklistview =booklistview+"\n\n"+"Id :"+id+"\n Book_Name :"+bookname+"\n context :"+bookcontext;
                    }
                    Log.i("MessengerService","succeed ReaduserTolog msg.what:"+msg.what);
                    Log.i("MessengerService",booklistview);

                    try {//模拟耗时任务
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //发送结果回Activity
                    Message message = this.obtainMessage();
                    message.what = 0x12;
                    message.arg1 = msg.arg1 + msg.arg2;//小資料的相加 可以用obj傳大資料
                    try {
                        mActivityMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else if(msg.what==0x01){

                    Bundle bundle0x01 =msg.getData();
                    String Name = bundle0x01.getString("bookman");
                    Log.i("MessengerService",Name);
                    bookDao.deleteallbook();
                    Name="delete all books";
                    Bundle newbundle = new Bundle();
                    newbundle.putString("bookman", Name);
                    //bookman.tob
                    Message newmessage = this.obtainMessage();
                    newmessage.setData(newbundle);
                    newmessage.what = 0x01;
                   // newmessage.replyTo=mActivityMessenger;
                    try {
                        mActivityMessenger.send(newmessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else if(msg.what == 0x20){
                    List<Book> books=bookDao.getbook();
                    String booklistview="";
                    for(Book book:books){
                        int id=book.getId();
                        String  bookname=book.getBook_name();
                        String  bookcontext=book.getBook_conext();
                        booklistview =booklistview+"\n\n"+"Id :"+id+"\n Book_Name :"+bookname+"\n context :"+bookcontext;
                    }
                    Log.i("MessengerService","succeed ReaduserTolog msg.what:"+msg.what);
                    Log.i("MessengerService",booklistview);

                }
            }
        };

        mServiceMessenger = new Messenger(handler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("TAG","onBind()");
        return mServiceMessenger.getBinder();
    }
}
