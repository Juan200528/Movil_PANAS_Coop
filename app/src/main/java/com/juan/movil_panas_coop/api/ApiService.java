package com.juan.movil_panas_coop.api;

import com.juan.movil_panas_coop.model.Actividad;
import com.juan.movil_panas_coop.model.LoginResponse;
import com.juan.movil_panas_coop.model.User;

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
    Call<Actividad> crearActividadJson(
            @Part("actividad") RequestBody actividadJson,
            @Part MultipartBody.Part imagen
    );

    /**
     * Registra un nuevo usuario.
     */
    @POST("api/auth/register")
    Call<LoginResponse> register(@Body User user);

    /**
     * Inicia sesión de usuario y devuelve el token o cookie de sesión.
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body User user);

    /**
     * Actualiza los datos de un usuario identificado por su ID.
     * Requiere token en el header Authorization.
     */
    @PUT("users/{id}")
    Call<User> actualizarUsuario(
            @Path("id") int id,
            @Body User user,
            @Header("Authorization") String token
    );

    /**
     * Cierra la sesión del usuario.
     */
    @POST("api/auth/logout")
    Call<Void> logout();
}
