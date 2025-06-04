package com.juan.movil_panas_coop.api;

import android.content.Context;

import com.juan.movil_panas_coop.utils.AuthInterceptor;
import com.juan.movil_panas_coop.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://backend-nrpu.onrender.com/";
    private static Retrofit retrofit = null;

    private static Context appContext;

    /**
     * Inicializa el contexto de la aplicación.
     * Debe llamarse antes de usar RetrofitClient, idealmente en Application o MainActivity.
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * Retorna la instancia de ApiService configurada.
     * @return ApiService para hacer llamadas al backend.
     */
    public static ApiService getApiService() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("RetrofitClient no ha sido inicializado. Llama a RetrofitClient.init(context) antes de usarlo.");
            }

            SessionManager sessionManager = new SessionManager(appContext);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(sessionManager)) // Añade token en Authorization header
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}
