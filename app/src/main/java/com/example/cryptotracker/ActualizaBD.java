package com.example.cryptotracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActualizaBD extends BroadcastReceiver {
    SQLiteDatabase db;
    private String uri = "https://pro-api.coinmarketcap.com/v2/cryptocurrency/quotes/latest?id=";
    private static final String apiKey = "f8f953f1-8d58-43b6-9849-ba1a9302d8ee";
    Context context;
    String ids = "";
    Logger myLogger;
    ArrayList<String> textoNotif;
    private final int DIGITOS_PRECIO = 6;
    private final int DIGITOS_CAMBIO24H = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        db = context.openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);
        myLogger = Logger.getLogger("ActualizaDB");
        myLogger.setLevel(Level.SEVERE);
        actualizaDB();
    }

    public void actualizaDB(){
        Cursor cursorSeguimiento = db.rawQuery("SELECT id FROM monedasTab WHERE seguimiento = 1", null);
        if (cursorSeguimiento.getCount() > 0) {
            // creating a variable for request queue.
            int limite = 1;
            ids = "";
            StringBuilder strBuilder = new StringBuilder(ids);
            while (cursorSeguimiento.moveToNext()){
                strBuilder.append(cursorSeguimiento.getString(0));
                if (limite < cursorSeguimiento.getCount()){
                    strBuilder.append(",");
                }
                limite++;
            }
            ids = strBuilder.toString();
            uri += ids;
            RequestQueue queue = Volley.newRequestQueue(context);
            // making a json object request to fetch data from API.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, response -> {
                // inside on response method extracting data
                // from response and passing it to db
                try {
                    // extracting data from json.
                    JSONObject dataTodo = response.getJSONObject("data");
                    String[] parts = ids.split(",");
                    for (String part : parts) {
                        JSONObject dataId = dataTodo.getJSONObject(part);
                        JSONObject dataQuote = dataId.getJSONObject("quote");
                        JSONObject dataUsd = dataQuote.getJSONObject("USD");
                        //redondeamos el precio a digitosPrecio
                        Double precioDouble = dataUsd.getDouble("price");
                        BigDecimal bdPrecio = new BigDecimal(precioDouble);
                        bdPrecio = bdPrecio.round(new MathContext(DIGITOS_PRECIO));
                        double roundedPrecio = bdPrecio.doubleValue();
                        //redondeamos el cambio24h a digitos_cambio24h
                        Double cambio24hDouble = dataUsd.getDouble("percent_change_7d");
                        BigDecimal bdCambio24h = new BigDecimal(cambio24hDouble);
                        bdCambio24h = bdCambio24h.round(new MathContext(DIGITOS_CAMBIO24H));
                        double roundedCambio24h = bdCambio24h.doubleValue();
                        // adding all data to our db
                        db.execSQL("UPDATE monedasTab SET precioUsd = " + roundedPrecio + ", cambio24h = " + roundedCambio24h + " WHERE id=" + part);

                        // asi evitamos el trim()' on a null object reference, llamando al metodo aqui en el response, no en onreceive
                        actualizaNotif();
                    }
                } catch (JSONException error) {
                    // handling json exception.
                    Toast.makeText(context, "JOSNException", Toast.LENGTH_LONG).show();
                    myLogger.log(Level.SEVERE, error.toString());
                }
            }, error -> {
                // displaying error response when received any error.
                Toast.makeText(context, "VolleyError", Toast.LENGTH_LONG).show();
                myLogger.log(Level.SEVERE, error.toString());
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    // in this method passing headers as
                    // key along with value as API keys.
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-CMC_PRO_API_KEY", apiKey);
                    // at last returning headers
                    return headers;
                }
            };
            // calling a method to add our
            // json object request to our queue.
            queue.add(jsonObjectRequest);
        }
        cursorSeguimiento.close();
    }

    //si de la lista de seguimiento el precio supera 'sube' enviar notif y actualiza db no notificar mas
    // lo mismo para notif_baja
    @SuppressLint("Range")
    public void actualizaNotif(){
        Cursor cursorNotif = db.rawQuery("SELECT * FROM monedasTab WHERE seguimiento = 1", null);
        if (cursorNotif.getCount() > 0) {
            textoNotif = new ArrayList<>();
            while(cursorNotif.moveToNext()) {
                String id = cursorNotif.getString(cursorNotif.getColumnIndex("id"));
                String simbolo = cursorNotif.getString(cursorNotif.getColumnIndex("simbolo"));
                String precioUsd = cursorNotif.getString(cursorNotif.getColumnIndex("precioUsd"));
                String sube = cursorNotif.getString(cursorNotif.getColumnIndex("sube"));
                String baja = cursorNotif.getString(cursorNotif.getColumnIndex("baja"));
                //formatear los numero
                double precioDouble = 0;
                double subeDouble = 0;
                double bajaDouble = 0;
                if (precioUsd != null && precioUsd.length() > 0) precioDouble = Double.parseDouble(precioUsd);
                if (sube != null && sube.length() > 0) subeDouble = Double.parseDouble(sube);
                if (baja != null && baja.length() > 0) bajaDouble = Double.parseDouble(baja);
                int notif_sube = cursorNotif.getInt(cursorNotif.getColumnIndex("notif_sube"));
                int notif_baja = cursorNotif.getInt(cursorNotif.getColumnIndex("notif_baja"));
                if ((notif_sube == 1) && (precioDouble > subeDouble)) {
                    db.execSQL("UPDATE monedasTab SET notif_sube = 0 WHERE id=" + id);
                    textoNotif.add(simbolo + " sube a " + precioUsd + "$");
                } else if ((notif_baja == 1) && (precioDouble < bajaDouble)) {
                    db.execSQL("UPDATE monedasTab SET notif_baja = 0 WHERE id=" + id);
                    textoNotif.add(simbolo + " baja a " + precioUsd + "$");
                }
            }
            if(textoNotif.size() > 0) {
                CrearNotificacion(context);
            }
        }
        cursorNotif.close();
        //db.close();
    }

    public void CrearNotificacion(Context context) {
        int notifId=1; //Identificador de la notificación, para futuras modificaciones.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(context.getResources().getString(R.string.notif_titulo), context.getResources().getString(R.string.notif_titulo), NotificationManager.IMPORTANCE_DEFAULT);

            // PASO 1: Crear la notificación con sus propiedades
            Notification.Builder constructorNotif = new Notification.Builder(context, context.getResources().getString(R.string.notif_titulo));
            constructorNotif.setSmallIcon(R.drawable.logoapp);
            Notification.InboxStyle inboxStyle =new Notification.InboxStyle();
            inboxStyle.setBigContentTitle(context.getResources().getString(R.string.notif_titulo));
            for (String linea : textoNotif) {
                inboxStyle.addLine(linea);
            }
            constructorNotif.setStyle(inboxStyle);
            // PASO 2: Creamos un intent para abrir la actividad cuando se pulse la notificación
            Intent resultadoIntent = new Intent(context, MainActivity.class);
            //El objeto stackBuilder crea un back stack que nos asegura que el botón de "Atrás" del
            //dispositivo nos lleva desde la Actividad a la pantalla principal
            TaskStackBuilder pila = TaskStackBuilder.create(context);
            // El padre del stack será la actividad a crear
            pila.addParentStack(MainActivity.class);
            // Añade el Intent que comienza la Actividad al inicio de la pila
            pila.addNextIntent(resultadoIntent);
            PendingIntent resultadoPendingIntent = pila.getPendingIntent(0,PendingIntent.FLAG_MUTABLE);
            constructorNotif.setContentIntent(resultadoPendingIntent);
            // PASO 4. Crear y enviar
            NotificationManager notificador = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificador.createNotificationChannel(channel);

            notificador.notify(notifId, constructorNotif.build());
        } else {

            // PASO 1: Crear la notificación con sus propiedades
            NotificationCompat.Builder constructorNotif = new NotificationCompat.Builder(context, context.getResources().getString(R.string.notif_titulo));
            constructorNotif.setSmallIcon(R.drawable.logoapp);
            NotificationCompat.InboxStyle inboxStyle =new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(context.getResources().getString(R.string.notif_titulo));
            for (String linea : textoNotif) {
                inboxStyle.addLine(linea);
            }
            constructorNotif.setStyle(inboxStyle);
            // PASO 2: Creamos un intent para abrir la actividad cuando se pulse la notificación
            Intent resultadoIntent = new Intent(context, MainActivity.class);
            //El objeto stackBuilder crea un back stack que nos asegura que el botón de "Atrás" del
            //dispositivo nos lleva desde la Actividad a la pantalla principal
            TaskStackBuilder pila = TaskStackBuilder.create(context);
            // El padre del stack será la actividad a crear
            pila.addParentStack(MainActivity.class);
            // Añade el Intent que comienza la Actividad al inicio de la pila
            pila.addNextIntent(resultadoIntent);
            PendingIntent resultadoPendingIntent = pila.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            constructorNotif.setContentIntent(resultadoPendingIntent);
            // PASO 4. Crear y enviar
            NotificationManagerCompat notificador = NotificationManagerCompat.from(context);

            constructorNotif.setPriority(Notification.PRIORITY_DEFAULT);

            notificador.notify(notifId, constructorNotif.build());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
    }


}