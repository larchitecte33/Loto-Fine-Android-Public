package com.projects.loto_fine.classes_utilitaires;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.projects.loto_fine.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

// Classe implémentant une structure de donnée de type File.
// Elle va ajouter les requêtes à traiter les unes à la suite des autres.
// De plus, elle implémente le design pattern Singleton afin d'être certain qu'il y aura au plus une instance de la classe créée au moment t.
public class RequestQueueSingleton {
    private static RequestQueueSingleton instance; // Instance de la classe RequestQueueSingleton (utilisée pour implémenter le design patter Singleton).
    private RequestQueue requestQueue; // File des requêtes.
    private static Context ctx; // Contexte

    // Constructeur
    private RequestQueueSingleton(Context ctx) {
        this.ctx = ctx;
        requestQueue = getRequestQueue();
    }

    // Fonction utilisée pour HTTPS.
    private static SSLSocketFactory getSocketFactory() {
        // On définit une certifiacte factory utilisée pour générer un certificat.
        CertificateFactory cf = null;

        try {
            cf = CertificateFactory.getInstance("X.509");
            // On charge les données du certificat sous forme d'InputStream.
            InputStream caInput = ctx.getResources().openRawResource(R.raw.springboot);
            Certificate ca;

            try {
                // On génère un certificat à partir des données du certificat.
                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // On crée un KeyStor et on y ajoute le certificat.
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // On approuve le certificat.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // On crée un HostnameVerifier qui va vérifier l'adresse du serveur.
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    Log.e("CipherUsed", session.getCipherSuite());
                    return hostname.compareTo("192.168.1.17") == 0; //The Hostname of your server.

                }
            };


            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            SSLContext context = null;
            context = SSLContext.getInstance("TLS");

            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            SSLSocketFactory sf = context.getSocketFactory();


            return sf;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return  null;
    }

    // Implémentation du design pattern Singleton.
    public static synchronized RequestQueueSingleton getInstance(Context context) {
        // Si aucune instance de la classe RequestQueueSingleton existe, on en crée une.
        if(instance == null) {
            instance = new RequestQueueSingleton(context);
        }

        // On retourne l'instance, soit qui vient d'être créée, soit qui existait déjà.
        return instance;
    }

    // Implémentation du design pattern Singleton.
    public RequestQueue getRequestQueue() {
        // Si la RequestQueue n'a pas déjà été instancée, on l'instancie.
        if(requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext(), new HurlStack(null, getSocketFactory()));
        }

        // On retourne l'instance, soit qui vient d'être créée, soit qui existait déjà.
        return requestQueue;
    }

    // Fonction utilisée pour ajouter une requête à la file.
    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
