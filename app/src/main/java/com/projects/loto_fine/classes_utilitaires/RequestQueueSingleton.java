package com.projects.loto_fine.classes_utilitaires;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSingleton {
    private static RequestQueueSingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private RequestQueueSingleton(Context ctx) {
        this.ctx = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if(instance == null) {
            instance = new RequestQueueSingleton(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        // Si la RequestQueue n'a pas déjà été instancée, on l'instancie.
        if(requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
