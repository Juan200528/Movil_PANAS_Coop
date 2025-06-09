package com.juan.movil_panas_coop.api;

import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.model.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // ✅ CAMBIO PRINCIPAL: De multipart a JSON directo
    @POST("api/tasks")
    Call<ActividadModel> crearActividad(
            @Header("Authorization") String authToken,
            @Body ActividadModel actividad  // Cambio principal aquí
    );

    @GET("api/tasks")
    Call<List<ActividadModel>> obtenerActividades(@Header("Authorization") String token);


    @DELETE("api/tasks/{id}")
    Call<ResponseBody> deleteTask(@Path("id") String id, @Header("Authorization") String token);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body User user);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(
            @Path("id") int id,
            @Body User user,
            @Header("Authorization") String token
    );

    @POST("api/auth/logout")
    Call<Void> logout();

}