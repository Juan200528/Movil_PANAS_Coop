    package com.juan.movil_panas_coop.utils;

    import java.io.IOException;

    import okhttp3.Interceptor;
    import okhttp3.Request;
    import okhttp3.Response;

    public class AuthInterceptor implements Interceptor {

        private SessionManager sessionManager;

        public AuthInterceptor(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(newRequest);
            }

            return chain.proceed(originalRequest);
        }
    }
