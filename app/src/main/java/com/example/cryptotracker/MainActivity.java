package com.example.cryptotracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    private static final String apiKey = "f8f953f1-8d58-43b6-9849-ba1a9302d8ee";
    private final String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/map?limit=400&sort=cmc_rank";
    SQLiteDatabase db;
    Logger myLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());


        setContentView(R.layout.activity_main);

        myLogger = Logger.getLogger("MainActivity");
        myLogger.setLevel(Level.SEVERE);

        db = openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);

        // Borrar tabla para pruebas de carga
        //db.execSQL("DROP TABLE IF EXISTS monedasTab");

        db.execSQL("CREATE TABLE IF NOT EXISTS monedasTab(id VARCHAR(5) PRIMARY KEY, simbolo VARCHAR(10), nombre VARCHAR(100), precioUsd VARCHAR(15), seguimiento INTEGER(1), sube VARCHAR(10), baja VARCHAR(10), notif_sube INTEGER(1), notif_baja INTEGER(1), cambio24h VARCHAR(10));");


        /*
        PowerManager pw = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // avisar al usuario de los permisos para actualizar precios con el dispositivo bloqueado
        if (!pw.isIgnoringBatteryOptimizations(getPackageName())) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(R.string.acceso_internet);
            builder1.setCancelable(true);
            builder1.setNeutralButton(R.string.entendido, (dialog, id) -> dialog.cancel());
            AlertDialog alert11 = builder1.create();
            alert11.show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent)
        }

         */


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        } else {
            // permiso de internet concedido, continuar con el programa
            cargarBaseDatos();
        }
        Button btnLista = findViewById(R.id.buttonLista);
        btnLista.setOnClickListener(v -> MainActivity.this.startActivity(new Intent(MainActivity.this, ActividadLista.class)));
        Button btnSimulador = findViewById(R.id.buttonSimulador);
        btnSimulador.setOnClickListener(v -> MainActivity.this.startActivity(new Intent(MainActivity.this, ActividadInversiones.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void cargarBaseDatos(){
        //si no hay datos los cargamos
        Cursor cursorTest = db.rawQuery("SELECT * FROM monedasTab", null);
        if (cursorTest.getCount() == 0) {
            // creating a variable for request queue.
            RequestQueue queue = Volley.newRequestQueue(this);
            // making a json object request to fetch data from API.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, response -> {
                // inside on response method extracting data
                // from response and passing it to db
                try {
                    // extracting data from json.
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        String id = dataObj.getString("id");
                        String simbolo = dataObj.getString("symbol");
                        String nombre = dataObj.getString("name");
                        // adding all data to our db
                        db.execSQL("INSERT INTO monedasTab (id, simbolo, nombre, seguimiento) VALUES ('" + id + "', '" + simbolo + "', '" + nombre + "', 0)");
                    }
                } catch (JSONException error) {
                    // handling json exception.
                    Toast.makeText(MainActivity.this, "JOSNException", Toast.LENGTH_LONG).show();
                    myLogger.log(Level.SEVERE, error.toString());
                }
            }, error -> {
                // displaying error response when received any error.
                Toast.makeText(MainActivity.this, "VolleyError", Toast.LENGTH_LONG).show();
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
        cursorTest.close();
    }

    /**
     * Método que comprueba el resultado de la solicitud de acceso a Internet
     * @param requestCode identificador de la solicitud de permisos
     * @param permissions permisos solicitados
     * @param grantResults resultados de los permisos solicitados
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_INTERNET) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(R.string.acceso_internet);
                builder1.setCancelable(true);
                builder1.setNeutralButton(R.string.entendido, (dialog, id) -> dialog.cancel());
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }

}