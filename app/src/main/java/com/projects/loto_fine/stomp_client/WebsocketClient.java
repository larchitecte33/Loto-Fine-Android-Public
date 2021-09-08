package com.projects.loto_fine.stomp_client;

import android.app.Activity;
import android.util.Log;

import com.projects.loto_fine.activites.MainActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.*;

public class WebsocketClient {
    String WEBSOCKET_CONNECT_URL="ws://example.com/ws";
    String WEBSOCKET_TOPIC="/user/queue/";
    static StompClient mStompClient;
    static Activity activity;
    CompositeDisposable compositeDisposable;

    private void connectWebSocket(Activity activity) {
        this.activity = activity;

        compositeDisposable = new CompositeDisposable();
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_CONNECT_URL);
        Disposable lifecycle = mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.i("1", "Stomp Connection Opened");
                    break;
                case ERROR:
                    Log.d("2", "Error ", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.w("3", "Stomp Connection Closed");

                    break;
                case FAILED_SERVER_HEARTBEAT:
                    Log.d("4", "Failed Server Heartbeat ");
                    break;
            }
        });
        if (!mStompClient.isConnected()) {
            mStompClient.connect();
        }

        Disposable topic = mStompClient.topic(WEBSOCKET_TOPIC).subscribe(stompMessage -> {
            Log.d("5",stompMessage.getPayload());
            // Do your code here when ever you receive data from server.
        }, throwable -> Log.d("6",throwable.getMessage().toString()+""));
        compositeDisposable.add(lifecycle);
        compositeDisposable.add(topic);
    }

    public void connectSocket() {
        if (mStompClient != null) {
            if (!mStompClient.isConnected()) {
                connectWebSocket(activity);
            }
        }
    }

    public void disconnectSocket() {
        if (mStompClient != null) {
            if (mStompClient.isConnected()) {
                mStompClient.disconnect();
//                compositeDisposable.dispose();
            }
        }

    }
}
