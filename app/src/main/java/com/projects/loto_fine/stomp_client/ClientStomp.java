package com.projects.loto_fine.stomp_client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.projects.loto_fine.activites.MainActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ClientStomp {
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private int idPartie;

    public ClientStomp(Context context, int idPartie, MainActivity mainActivity) {
        this.idPartie = idPartie;
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.1.17:8084/lotofine-websocket");

        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);

        resetSubscriptions();

        Disposable dispLifecycle = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Toast.makeText(context, "Stomp connection opened", 5000);
                            break;
                        case ERROR:
                            Log.e("AAA", "Stomp connection error", lifecycleEvent.getException());
                            Toast.makeText(context, "Stomp connection error", 5000);
                            break;
                        case CLOSED:
                            Toast.makeText(context, "Stomp connection closed", 5000);
                            resetSubscriptions();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Toast.makeText(context, "Stomp failed server heartbeat", 5000);
                            break;
                    }
                });

        compositeDisposable.add(dispLifecycle);

        Disposable dispTopic = stompClient.topic("/topic/getInfosPartie/" + this.idPartie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d("AAA", "Received " + topicMessage.getPayload());

                    if(!topicMessage.getPayload().equals("")) {
                        JsonObject jsonObject = new JsonParser().parse(topicMessage.getPayload()).getAsJsonObject();
                        mainActivity.setNumeroTire(jsonObject.get("numeroEnCours").getAsInt());
                        mainActivity.setLotEnCours(jsonObject.get("lotEnCours").getAsString());
                        mainActivity.setIsLotCartonPlein(jsonObject.get("lotCartonPlein").getAsBoolean());
                        mainActivity.rafraichirInfosNumeroLot();
                    }
                }, throwable -> {
                    Log.e("AAA", "Error on subscribe topic", throwable);
                });

        compositeDisposable.add(dispTopic);

        stompClient.connect();
    }

    private void resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }

        compositeDisposable = new CompositeDisposable();
    }

    public void deconnecterClientStomp() {
        if(stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    public boolean isConnected() {
        return stompClient.isConnected();
    }
}
