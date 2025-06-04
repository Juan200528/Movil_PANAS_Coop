package com.juan.movil_panas_coop.api;

import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Endpoints corregidos
    @POST("/api/auth/register")
    Call<LoginResponse> register(@Body User user);

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body User user);
    // Interfaz ApiService (dentro de tu ApiService.java o donde tengas)

    @PUT("users/{id}")
    Call<User> actualizarUsuario(
            @Path("id") int id,
            @Body User user,
            @Header("Authorization") String token
    );

    @POST("/api/auth/logout")
    Call<Void> logout();
}