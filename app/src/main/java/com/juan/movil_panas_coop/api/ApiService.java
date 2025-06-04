package com.juan.movil_panas_coop.api;

import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.model.User;
import com.juan.movil_panas_coop.models.Actividad;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {

    @Multipart
    @POST("api/tasks")
    Call<Actividad> crearActividad(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("date") RequestBody date,
            @Part("place") RequestBody place,
            @Part("responsible") RequestBody responsible,
            @Part("estado") RequestBody estado,
            @Part("promocionada") RequestBody promocionada,
            @Part MultipartBody.Part imagen
    );


    @POST("api/auth/register")
    Call<LoginResponse> register(@Body User user);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body User user);

    @PUT("users/{id}")
    Call<User> actualizarUsuario(
            @Path("id") int id,
            @Body User user,
            @Header("Authorization") String token
    );

    @POST("api/auth/logout")
    Call<Void> logout();
}
