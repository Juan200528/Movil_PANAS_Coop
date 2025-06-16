package com.juan.movil_panas_coop.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.juan.movil_panas_coop.model.ActividadModel;
import com.juan.movil_panas_coop.models.Actividad;
import com.juan.movil_panas_coop.models.Asistente;
import com.juan.movil_panas_coop.models.Notificacion;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerDb {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public ManagerDb(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
        Log.d("ManagerDb", "Base de datos abierta: " + (database != null && database.isOpen()));
        if (database != null && database.isOpen()) {
            Log.d("ManagerDb", "Ruta de la base de datos: " + database.getPath());
        }
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d("ManagerDb", "Base de datos cerrada");
        }
        dbHelper.close();
    }




    public String getUserNameById(int userId) {
        Cursor cursor = database.query(Constantes.TABLA_USUARIOS, new String[]{Constantes.COLUMNA_NOMBRE},
                Constantes.COLUMNA_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_NOMBRE));
            cursor.close();
            return nombre;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    // Método para obtener teléfono
    public String getUserPhoneById(int userId) {
        String telefono = null;
        Cursor cursor = database.query(
                "usuarios", new String[]{"telefono"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"));
            cursor.close();
        }
        return telefono;
    }

    // Método para obtener dirección
    public String getUserAddressById(int userId) {
        String direccion = null;
        Cursor cursor = database.query(
                "usuarios", new String[]{"direccion"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion"));
            cursor.close();
        }
        return direccion;
    }
    public boolean actualizarUsuario(int id, String nombre, String email, String telefono, String direccion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Constantes.COLUMNA_NOMBRE, nombre);
        valores.put(Constantes.COLUMNA_EMAIL, email);
        valores.put(Constantes.COLUMNA_TELEFONO, telefono);
        valores.put(Constantes.COLUMNA_DIRECCION, direccion);

        String whereClause = Constantes.COLUMNA_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };

        int filasActualizadas = db.update(Constantes.TABLA_USUARIOS, valores, whereClause, whereArgs);
        db.close();

        return filasActualizadas > 0;
    }


    public String[] obtenerDatosUsuarioPorId(int userId) {
        Cursor cursor = database.query(Constantes.TABLA_USUARIOS,
                new String[]{Constantes.COLUMNA_ID, Constantes.COLUMNA_NOMBRE, Constantes.COLUMNA_EMAIL},
                Constantes.COLUMNA_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String[] datos = new String[3];
            datos[0] = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID));
            datos[1] = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_NOMBRE));
            datos[2] = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_EMAIL));
            cursor.close();
            return datos;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public int validarUsuario(String email, String password) {
        Cursor cursor = database.query(Constantes.TABLA_USUARIOS,
                new String[]{Constantes.COLUMNA_ID},
                Constantes.COLUMNA_EMAIL + "=? AND " + Constantes.COLUMNA_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID));
            cursor.close();
            return userId;
        }
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }

    public boolean existeEmail(String email) {
        Cursor cursor = database.query(Constantes.TABLA_USUARIOS,
                new String[]{Constantes.COLUMNA_EMAIL},
                Constantes.COLUMNA_EMAIL + "=?",
                new String[]{email}, null, null, null);
        boolean existe = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return existe;
    }

    public long insertarUsuario(String nombre, String email, String password) {
        ContentValues values = new ContentValues();
        values.put(Constantes.COLUMNA_NOMBRE, nombre);
        values.put(Constantes.COLUMNA_EMAIL, email);
        values.put(Constantes.COLUMNA_PASSWORD, password);
        return database.insert(Constantes.TABLA_USUARIOS, null, values);
    }

    public long insertarActividad(Actividad actividad) {
        ContentValues values = new ContentValues();
        values.put(Constantes.COLUMNA_TITULO, actividad.getTitulo());
        values.put(Constantes.COLUMNA_DESCRIPCION, actividad.getDescripcion());
        values.put(Constantes.COLUMNA_FECHA, actividad.getFecha());
        values.put(Constantes.COLUMNA_LUGAR, actividad.getLugar());
        values.put(Constantes.COLUMNA_ID_CREADOR, actividad.getIdCreador());
        values.put(Constantes.COLUMNA_RESPONSABLES, actividad.getResponsables());
        values.put(Constantes.COLUMNA_ESTADO, actividad.getEstado());
        values.put(Constantes.COLUMNA_IMAGEN_RUTA, actividad.getImagenRuta());
        values.put(Constantes.COLUMNA_PROMOCIONADA, actividad.isPromocionada() ? 1 : 0);
        return database.insert(Constantes.TABLA_ACTIVIDADES, null, values);
    }

    public List<Actividad> obtenerActividades() {
        return getAllActividades();
    }

    public List<Actividad> getAllActividades() {
        List<Actividad> actividades = new ArrayList<>();
        Cursor cursor = database.query(Constantes.TABLA_ACTIVIDADES,
                null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Actividad actividad = new Actividad();
            actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
            actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
            actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
            actividad.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA)));
            actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
            actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR))); // Usamos getString()
            actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
            actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
            actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
            actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
            actividad.setAsistido(false); // No hay userId disponible, establecer en false
            actividades.add(actividad);
        }
        cursor.close();
        return actividades;
    }
    // Corregir también obtenerActividadesPorUsuario para consistencia
    public List<Actividad> obtenerActividadesPasadasPorUsuario(int userId) {
        List<Actividad> actividades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today;
        try {
            today = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return actividades; // Retornar lista vacía si hay error
        }

        String query = "SELECT * FROM " + Constantes.TABLA_ACTIVIDADES +
                " WHERE " + Constantes.COLUMNA_ID_CREADOR + "=?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            try {
                String fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA));
                Date fechaActividad = sdf.parse(fechaStr);
                if (fechaActividad != null && fechaActividad.before(today)) {
                    Actividad actividad = new Actividad();
                    actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
                    actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
                    actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
                    actividad.setFecha(fechaStr);
                    actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
                    actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
                    actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
                    actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
                    actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
                    actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
                    actividad.setAsistido(existeAsistencia(userId, actividad.getId()));
                    actividad.setPasada(true); // Explicitamente marcar como pasada
                    actividades.add(actividad);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return actividades;
    }

    public List<Actividad> obtenerActividadesPorUsuario(int userId) {
        List<Actividad> actividades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today;
        try {
            today = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return actividades;
        }

        Cursor cursor = database.query(Constantes.TABLA_ACTIVIDADES,
                null,
                Constantes.COLUMNA_ID_CREADOR + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        while (cursor.moveToNext()) {
            try {
                String fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA));
                Date fechaActividad = sdf.parse(fechaStr);
                if (fechaActividad != null && !fechaActividad.before(today)) {
                    Actividad actividad = new Actividad();
                    actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
                    actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
                    actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
                    actividad.setFecha(fechaStr);
                    actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
                    actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
                    actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
                    actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
                    actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
                    actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
                    actividad.setAsistido(existeAsistencia(userId, actividad.getId()));
                    actividades.add(actividad);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return actividades;
    }

    public List<Actividad> obtenerActividadesPromocionadas() {
        List<Actividad> actividades = new ArrayList<>();
        Cursor cursor = database.query(Constantes.TABLA_ACTIVIDADES,
                null,
                Constantes.COLUMNA_PROMOCIONADA + "=?",
                new String[]{"1"},
                null, null, null);

        while (cursor.moveToNext()) {
            Actividad actividad = new Actividad();
            actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
            actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
            actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
            actividad.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA)));
            actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
            actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
            actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
            actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
            actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
            actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
            actividad.setAsistido(false); // No hay userId disponible, establecer en false
            actividades.add(actividad);
        }
        cursor.close();
        return actividades;
    }

    public void actualizarActividad(Actividad actividad) {
        ContentValues values = new ContentValues();
        values.put(Constantes.COLUMNA_TITULO, actividad.getTitulo());
        values.put(Constantes.COLUMNA_DESCRIPCION, actividad.getDescripcion());
        values.put(Constantes.COLUMNA_FECHA, actividad.getFecha());
        values.put(Constantes.COLUMNA_LUGAR, actividad.getLugar());
        values.put(Constantes.COLUMNA_ID_CREADOR, actividad.getIdCreador());
        values.put(Constantes.COLUMNA_RESPONSABLES, actividad.getResponsables());
        values.put(Constantes.COLUMNA_ESTADO, actividad.getEstado());
        values.put(Constantes.COLUMNA_IMAGEN_RUTA, actividad.getImagenRuta());
        values.put(Constantes.COLUMNA_PROMOCIONADA, actividad.isPromocionada() ? 1 : 0);
        database.update(Constantes.TABLA_ACTIVIDADES, values,
                Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(actividad.getId())});
    }

    public void eliminarActividad(int id) {
        database.delete(Constantes.TABLA_ACTIVIDADES,
                Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(id)});
    }

    public long insertarAsistente(Asistente asistente) {
        ContentValues values = new ContentValues();
        values.put(Constantes.CAMPO_ID_ASISTENTE, asistente.getIdAsistente());
        values.put(Constantes.COLUMNA_ID_ACTIVIDAD, asistente.getIdActividad());
        values.put(Constantes.CAMPO_NOMBRE_COMPLETO, asistente.getNombreCompleto());
        values.put(Constantes.CAMPO_CORREO, asistente.getCorreo());
        values.put(Constantes.CAMPO_ACTIVIDAD_NOMBRE, asistente.getActividadNombre());
        return database.insert(Constantes.TABLA_ASISTENTE, null, values);
    }

    public List<Asistente> obtenerAsistentesPorActividad(int idActividad) {
        List<Asistente> asistentes = new ArrayList<>();
        Cursor cursor = database.query(Constantes.TABLA_ASISTENTE,
                null,
                Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(idActividad)},
                null, null, null);

        while (cursor.moveToNext()) {
            Asistente asistente = new Asistente();
            asistente.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID)));
            asistente.setIdAsistente(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID_ASISTENTE)));
            asistente.setIdActividad(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
            asistente.setNombreCompleto(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_NOMBRE_COMPLETO)));
            asistente.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_CORREO)));
            asistente.setActividadNombre(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ACTIVIDAD_NOMBRE)));
            asistente.setNombre(asistente.getNombreCompleto().split(" ")[0]); // Primer nombre para UI
            asistentes.add(asistente);
        }
        cursor.close();
        return asistentes;
    }

    public List<Asistente> obtenerAsistentesPorUsuario(int userId) {
        List<Asistente> asistentes = new ArrayList<>();
        Cursor cursor = database.query(Constantes.TABLA_ASISTENTE,
                null,
                Constantes.CAMPO_ID_ASISTENTE + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        while (cursor.moveToNext()) {
            Asistente asistente = new Asistente();
            asistente.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID)));
            asistente.setIdAsistente(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID_ASISTENTE)));
            asistente.setIdActividad(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
            asistente.setNombreCompleto(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_NOMBRE_COMPLETO)));
            asistente.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_CORREO)));
            asistente.setActividadNombre(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ACTIVIDAD_NOMBRE)));
            asistente.setNombre(asistente.getNombreCompleto().split(" ")[0]); // Primer nombre para UI
            asistentes.add(asistente);
        }
        cursor.close();
        return asistentes;
    }

    public void actualizarAsistente(Asistente asistente) {
        ContentValues values = new ContentValues();
        values.put(Constantes.CAMPO_ID_ASISTENTE, asistente.getIdAsistente());
        values.put(Constantes.COLUMNA_ID_ACTIVIDAD, asistente.getIdActividad());
        values.put(Constantes.CAMPO_NOMBRE_COMPLETO, asistente.getNombreCompleto());
        values.put(Constantes.CAMPO_CORREO, asistente.getCorreo());
        values.put(Constantes.CAMPO_ACTIVIDAD_NOMBRE, asistente.getActividadNombre());
        database.update(Constantes.TABLA_ASISTENTE, values,
                Constantes.CAMPO_ID + "=?",
                new String[]{String.valueOf(asistente.getId())});
    }

    public void eliminarAsistente(int id) {
        // Obtener información del asistente antes de eliminarlo
        Cursor cursor = database.query(
                Constantes.TABLA_ASISTENTE,
                new String[]{Constantes.CAMPO_ID_ASISTENTE, Constantes.COLUMNA_ID_ACTIVIDAD},
                Constantes.CAMPO_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        int idAsistente = -1;
        int idActividad = -1;
        if (cursor != null && cursor.moveToFirst()) {
            idAsistente = cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID_ASISTENTE));
            idActividad = cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD));
            cursor.close();
        }

        // Eliminar el asistente
        database.delete(
                Constantes.TABLA_ASISTENTE,
                Constantes.CAMPO_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        // Eliminar notificaciones asociadas al usuario y la actividad
        if (idAsistente != -1 && idActividad != -1) {
            database.delete(
                    Constantes.TABLA_NOTIFICACION,
                    Constantes.CAMPO_ID_USUARIO + "=? AND " + Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                    new String[]{String.valueOf(idAsistente), String.valueOf(idActividad)}
            );
        }
    }

//    public List<Actividad> buscarActividades(String busqueda, String fechaFiltro, String lugar, String estadoFiltro) {
//        List<Actividad> actividades = new ArrayList<>();
//        StringBuilder query = new StringBuilder("SELECT * FROM " + Constantes.TABLA_ACTIVIDADES + " WHERE 1=1");
//        List<String> selectionArgs = new ArrayList<>();
//
//        if (busqueda != null && !busqueda.trim().isEmpty()) {
//            query.append(" AND (").append(Constantes.COLUMNA_TITULO).append(" LIKE ? OR ")
//                    .append(Constantes.COLUMNA_DESCRIPCION).append(" LIKE ?)");
//            selectionArgs.add("%" + busqueda + "%");
//            selectionArgs.add("%" + busqueda + "%");
//        }
//
//        if (lugar != null && !lugar.trim().isEmpty()) {
//            query.append(" AND ").append(Constantes.COLUMNA_LUGAR).append(" LIKE ?");
//            selectionArgs.add("%" + lugar + "%");
//        }
//
//        if ("Promocionadas".equals(estadoFiltro)) {
//            query.append(" AND ").append(Constantes.COLUMNA_PROMOCIONADA).append(" = ?");
//            selectionArgs.add("1");
//        }
//
//        if (fechaFiltro != null && !fechaFiltro.equals("Todas")) {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            String today = sdf.format(new Date());
//            if ("Próximas".equals(fechaFiltro)) {
//                query.append(" AND ").append(Constantes.COLUMNA_FECHA).append(" >= ?");
//                selectionArgs.add(today);
//            } else if ("Pasadas".equals(fechaFiltro)) {
//                query.append(" AND ").append(Constantes.COLUMNA_FECHA).append(" < ?");
//                selectionArgs.add(today);
//            }
//        }
//
//        Cursor cursor = database.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));
//        while (cursor.moveToNext()) {
//            Actividad actividad = new Actividad();
//            actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
//            actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
//            actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
//            actividad.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA)));
//            actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
//            actividad.setIdCreador(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
//            actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
//            actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
//            actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
//            actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
//            actividad.setAsistido(false);
//            actividades.add(actividad);
//        }
//        cursor.close();
//        return actividades;
//    }

    public List<ActividadModel> buscarActividades(String busqueda, String fechaFiltro, String lugar, String estadoFiltro) {
        List<ActividadModel> actividadesFiltradas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today;
        try {
            today = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return actividadesFiltradas;
        }

        String query = "SELECT * FROM " + Constantes.TABLA_ACTIVIDADES;
        Cursor cursor = database.rawQuery(query, null);

        while (cursor.moveToNext()) {
            try {
                Actividad actividad = new Actividad();
                actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
                actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
                actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
                actividad.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA)));
                actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
                actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
                actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
                actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
                actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
                actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
                // Not setting isAsistido here since it's not needed for search

                // Determine if the activity is past
                Date fechaActividad = sdf.parse(actividad.getFecha());
                boolean isPasada = fechaActividad != null && fechaActividad.before(today);
                actividad.setPasada(isPasada); // This line should now work with the setter

                // Filter based on search criteria
                boolean matchesQuery = busqueda.isEmpty() ||
                        (actividad.getTitulo() != null && actividad.getTitulo().toLowerCase().contains(busqueda.toLowerCase())) ||
                        (actividad.getDescripcion() != null && actividad.getDescripcion().toLowerCase().contains(busqueda.toLowerCase()));

                boolean matchesLugar = lugar.isEmpty() ||
                        (actividad.getLugar() != null && actividad.getLugar().toLowerCase().contains(lugar.toLowerCase()));

                boolean matchesFecha = true;
                if (fechaFiltro.equals("Próximas")) {
                    matchesFecha = !actividad.isPasada();
                } else if (fechaFiltro.equals("Pasadas")) {
                    matchesFecha = actividad.isPasada();
                }

                boolean matchesEstado = true;
                if (estadoFiltro.equals("Promocionadas")) {
                    matchesEstado = actividad.isPromocionada();
                }

                if (matchesQuery && matchesLugar && matchesFecha && matchesEstado) {
                    ActividadModel actividadModel = new ActividadModel();
                    actividadModel.setId(String.valueOf(actividad.getId()));
                    actividadModel.setTitle(actividad.getTitulo());
                    actividadModel.setDescription(actividad.getDescripcion());
                    actividadModel.setPlace(actividad.getLugar());
                    actividadModel.setDate(actividad.getFecha());
                    
                    // Convertir responsables de String a List<String>
                    List<String> responsablesList = new ArrayList<>();
                    if (actividad.getResponsables() != null && !actividad.getResponsables().isEmpty()) {
                        responsablesList = Arrays.asList(actividad.getResponsables().split(","));
                    }
                    actividadModel.setResponsible(responsablesList);
                    
                    actividadModel.setPromoted(actividad.isPromocionada());
                    actividadModel.setAsistido(actividad.isAsistido());
                    actividadesFiltradas.add(actividadModel);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return actividadesFiltradas;
    }

    public List<Actividad> obtenerActividadesOtrosUsuarios(int userId) {
        List<Actividad> actividades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today;
        try {
            today = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return actividades;
        }

        String query = "SELECT * FROM " + Constantes.TABLA_ACTIVIDADES +
                " WHERE " + Constantes.COLUMNA_ID_CREADOR + "!=?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            try {
                String fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA));
                Date fechaActividad = sdf.parse(fechaStr);
                if (fechaActividad != null) {
                    boolean isPasada = fechaActividad.before(today);
                    // Incluir actividades del día actual o futuras (fecha >= hoy)
                    if (!isPasada || fechaActividad.equals(today)) {
                        Actividad actividad = new Actividad();
                        actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
                        actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
                        actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
                        actividad.setFecha(fechaStr);
                        actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
                        actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
                        actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
                        actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
                        actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
                        actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
                        actividad.setPasada(isPasada);
                        actividad.setAsistido(existeAsistencia(userId, actividad.getId()));
                        actividades.add(actividad);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return actividades;
    }

    public boolean existeAsistencia(int idAsistente, int idActividad) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    Constantes.TABLA_ASISTENTE,
                    new String[]{Constantes.CAMPO_ID},
                    Constantes.CAMPO_ID_ASISTENTE + " = ? AND " + Constantes.COLUMNA_ID_ACTIVIDAD + " = ?",
                    new String[]{String.valueOf(idAsistente), String.valueOf(idActividad)},
                    null, null, null);
            return cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public long insertarNotificacion(Notificacion notificacion) {
        ContentValues values = new ContentValues();
        values.put(Constantes.CAMPO_MENSAJE, notificacion.getMensaje());
        values.put(Constantes.CAMPO_FECHA, notificacion.getFecha());
        values.put(Constantes.CAMPO_ID_USUARIO, notificacion.getIdUsuario());
        values.put(Constantes.CAMPO_DIAS, notificacion.getDias());
        values.put(Constantes.CAMPO_DIAS_RESTANTES, notificacion.getDiasRestantes());
        values.put(Constantes.CAMPO_NOMBRE_ACTIVIDAD, notificacion.getNombreActividad());
        values.put(Constantes.CAMPO_DIAS_ACTIVIDAD, notificacion.getDiasActividad());
        values.put(Constantes.COLUMNA_ID_ACTIVIDAD, notificacion.getIdActividad());
        return database.insert(Constantes.TABLA_NOTIFICACION, null, values);
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(int userId) {
        List<Notificacion> notificaciones = new ArrayList<>();
        Cursor cursor = database.query(Constantes.TABLA_NOTIFICACION,
                null,
                Constantes.CAMPO_ID_USUARIO + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        while (cursor.moveToNext()) {
            Notificacion notificacion = new Notificacion();
            notificacion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID)));
            notificacion.setMensaje(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_MENSAJE)));
            notificacion.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_FECHA)));
            notificacion.setIdUsuario(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID_USUARIO)));
            notificacion.setDias(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_DIAS)));
            notificacion.setDiasRestantes(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_DIAS_RESTANTES)));
            notificacion.setNombreActividad(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.CAMPO_NOMBRE_ACTIVIDAD)));
            notificacion.setDiasActividad(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_DIAS_ACTIVIDAD)));
            notificacion.setIdActividad(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));

            // Verificar si la actividad ha terminado
            try {
                Date fechaActividad = sdf.parse(notificacion.getFecha());
                if (fechaActividad != null && fechaActividad.after(today)) {
                    notificaciones.add(notificacion);
                } else {
                    // Eliminar notificación si la actividad ha terminado
                    eliminarNotificacion(notificacion.getId());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return notificaciones;
    }

    public void actualizarNotificacion(Notificacion notificacion) {
        ContentValues values = new ContentValues();
        values.put(Constantes.CAMPO_MENSAJE, notificacion.getMensaje());
        values.put(Constantes.CAMPO_FECHA, notificacion.getFecha());
        values.put(Constantes.CAMPO_ID_USUARIO, notificacion.getIdUsuario());
        values.put(Constantes.CAMPO_DIAS, notificacion.getDias());
        values.put(Constantes.CAMPO_DIAS_RESTANTES, notificacion.getDiasRestantes());
        values.put(Constantes.CAMPO_NOMBRE_ACTIVIDAD, notificacion.getNombreActividad());
        values.put(Constantes.CAMPO_DIAS_ACTIVIDAD, notificacion.getDiasActividad());
        values.put(Constantes.COLUMNA_ID_ACTIVIDAD, notificacion.getIdActividad());
        database.update(Constantes.TABLA_NOTIFICACION, values,
                Constantes.CAMPO_ID + "=?",
                new String[]{String.valueOf(notificacion.getId())});
    }

    public void eliminarNotificacion(int id) {
        database.delete(Constantes.TABLA_NOTIFICACION,
                Constantes.CAMPO_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public void actualizarDiasRestantes() {
        List<Notificacion> notificaciones = obtenerNotificacionesPorUsuario(-1); // Obtener todas
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (Notificacion notificacion : notificaciones) {
            try {
                Date fechaActividad = sdf.parse(notificacion.getFecha());
                if (fechaActividad != null) {
                    // Calcular días hasta la actividad
                    long diffInMillies = fechaActividad.getTime() - today.getTime();
                    long diasTotales = diffInMillies / (1000 * 60 * 60 * 24);

                    // Calcular días restantes para la notificación
                    int diasRestantes = (int) Math.max(0, Math.min(notificacion.getDias(), diasTotales));
                    notificacion.setDiasRestantes(diasRestantes);
                    notificacion.setDiasActividad((int) diasTotales);

                    // Actualizar la notificación en la base de datos
                    actualizarNotificacion(notificacion);

                    // Eliminar notificación si la actividad ya pasó
                    if (diasTotales < 0) {
                        eliminarNotificacion(notificacion.getId());
                    }
                }
            } catch (ParseException e) {
                Log.e("ManagerDb", "Error al parsear fecha de notificación ID: " + notificacion.getId(), e);
            }
        }
    }

    // Método para verificar si existe una notificación para un usuario y actividad
    public boolean existeNotificacion(int idUsuario, int idActividad) {
        Cursor cursor = database.query(
                Constantes.TABLA_NOTIFICACION,
                new String[]{Constantes.CAMPO_ID},
                Constantes.CAMPO_ID_USUARIO + "=? AND " + Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(idUsuario), String.valueOf(idActividad)},
                null, null, null
        );
        boolean existe = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return existe;
    }

    // Método para obtener el ID de una notificación existente (opcional, para actualizar)
    public int obtenerIdNotificacion(int idUsuario, int idActividad) {
        Cursor cursor = database.query(
                Constantes.TABLA_NOTIFICACION,
                new String[]{Constantes.CAMPO_ID},
                Constantes.CAMPO_ID_USUARIO + "=? AND " + Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(idUsuario), String.valueOf(idActividad)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.CAMPO_ID));
            cursor.close();
            return id;
        }
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }

    public Actividad obtenerActividadPorId(int idActividad) {
        Cursor cursor = database.query(Constantes.TABLA_ACTIVIDADES,
                null,
                Constantes.COLUMNA_ID_ACTIVIDAD + "=?",
                new String[]{String.valueOf(idActividad)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Actividad actividad = new Actividad();
            actividad.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_ACTIVIDAD)));
            actividad.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_TITULO)));
            actividad.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_DESCRIPCION)));
            actividad.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_FECHA)));
            actividad.setLugar(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_LUGAR)));
            actividad.setIdCreador(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ID_CREADOR)));
            actividad.setResponsables(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_RESPONSABLES)));
            actividad.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_ESTADO)));
            actividad.setImagenRuta(cursor.getString(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_IMAGEN_RUTA)));
            actividad.setPromocionada(cursor.getInt(cursor.getColumnIndexOrThrow(Constantes.COLUMNA_PROMOCIONADA)) == 1);
            actividad.setAsistido(false); // No hay userId disponible, establecer en false
            cursor.close();
            return actividad;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}