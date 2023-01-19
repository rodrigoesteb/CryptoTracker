package com.example.cryptotracker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class ActividadLista extends AppCompatActivity implements SearchView.OnQueryTextListener, DialogoConfigura.RespuestaDialogoConfig {
    SQLiteDatabase db;
    RecyclerView recyclerView;
    ArrayList<String> nombre, simbolo, logo, precio, cambio24h;
    MyAdapter adapter;
    AdapterSeguimiento adapterSeguimiento;
    SearchView busquedaView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());


        setContentView(R.layout.activity_actividad_lista);
        busquedaView = findViewById(R.id.searchView);
        busquedaView.setOnQueryTextListener(this);
        recyclerView = findViewById(R.id.recyclerViewSeguimieto);
        Button btnUpdate = findViewById(R.id.btnUpdateList);
        btnUpdate.setOnClickListener(view -> {
            ActualizaBD probamosActua = new ActualizaBD();
            probamosActua.onReceive(getApplicationContext(), null);
            cargarListaSeguimiento();
        });
        db = openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);

        //carga 2 veces la lista al abrir la actividad ver metodo onPostResume()

        //cargarListaSeguimiento();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        //carga 2 veces la lista al abrir la actividad

        cargarListaSeguimiento();
    }



    @Override
    public boolean onQueryTextSubmit(String s) {
        nombre = new ArrayList<>();
        simbolo = new ArrayList<>();
        logo = new ArrayList<>();

        Cursor cursorBuscar = db.rawQuery("SELECT id, simbolo, nombre FROM monedasTab WHERE simbolo LIKE '%" + s + "%'" + " OR nombre LIKE '%" + s + "%'", null);
        if (cursorBuscar.getCount() > 0) {
            Toast.makeText(ActividadLista.this, "" + cursorBuscar.getCount() +  " coincidencias encontradas", Toast.LENGTH_SHORT).show();
            while (cursorBuscar.moveToNext()) {
                logo.add(cursorBuscar.getString(0));
                simbolo.add(cursorBuscar.getString(1));
                nombre.add(cursorBuscar.getString(2));
            }
        }else{
            Toast.makeText(ActividadLista.this, "No se ha encontrado la busqueda", Toast.LENGTH_LONG).show();
        }
        cursorBuscar.close();
        adapter = new MyAdapter(this, logo, simbolo, nombre);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void cargarListaSeguimiento(){
        nombre = new ArrayList<>();
        simbolo = new ArrayList<>();
        logo = new ArrayList<>();
        precio = new ArrayList<>();
        cambio24h = new ArrayList<>();
        Cursor cursorBuscar = db.rawQuery("SELECT id, simbolo, nombre, precioUsd, cambio24h FROM monedasTab WHERE seguimiento = 1", null);
        if (cursorBuscar.getCount() > 0) {
            Toast.makeText(ActividadLista.this, "Monedas en seguimiento: " + cursorBuscar.getCount(), Toast.LENGTH_SHORT).show();
            while (cursorBuscar.moveToNext()) {
                logo.add(cursorBuscar.getString(0));
                simbolo.add(cursorBuscar.getString(1));
                nombre.add(cursorBuscar.getString(2));
                precio.add(cursorBuscar.getString(3));
                cambio24h.add(cursorBuscar.getString(4));
            }
            adapterSeguimiento = new AdapterSeguimiento(this, logo, simbolo, nombre, precio, cambio24h);
            recyclerView.setAdapter(adapterSeguimiento);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        }else{
            Toast.makeText(ActividadLista.this, "Lista de seguimiento vacia", Toast.LENGTH_SHORT).show();
        }
        cursorBuscar.close();
    }

    /**
     * Método que construye el menú de la aplicación
     * @param menu menú creado
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_actividad_lista,menu);
        return true;
    }
    /**
     * Método que maneja los eventos al hacer click en los elementos del menú
     * @param item componente pulsado
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.velocidad_actualizacion) {
            DialogoConfigura configura = new DialogoConfigura();
            configura.show(getSupportFragmentManager(), "Configurar");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Método que abre el diálogo selector de hora
     */
    private void showTimePickerDialog() {
        TimePickerFragment newFragment = TimePickerFragment.newInstance((timePicker, hour, min) -> setAlarma(hour, min));
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    /**
     * Método que programa el AlarmManager a una hora concreta todos los días
     * @param hora hora a la que se activa el AlarmManager
     * @param minutos minuto al que se activa el AlarmManager
     */
    public void setAlarma(int hora, int minutos) {
        Toast.makeText(this, "Hora de actualización: " + hora + ":" + minutos, Toast.LENGTH_SHORT).show();
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minutos);
        Intent intent = new Intent(getApplicationContext(), ActualizaBD.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void setAlarma(long intervalo, String intervaloTxt) {
        Toast.makeText(this, "Intervalo de actualización: " + intervaloTxt, Toast.LENGTH_SHORT).show();
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        Intent intent = new Intent(getApplicationContext(), ActualizaBD.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalo, alarmIntent);
    }

    /**
     * Método que actualiza la velocidad de consulta de precios
     * @param i nivel de velocidad seleccionado
     */
    @Override
    public void onRespuestaDialogoConfig(int i) {

        switch (i) {
            case 0:
                showTimePickerDialog();
                break;
            case 1:
                setAlarma(AlarmManager.INTERVAL_HALF_DAY, getString(R.string.horas12));
                break;
            case 2:
                setAlarma(AlarmManager.INTERVAL_HOUR, getString(R.string.hora1));
                break;
            case 3:
                setAlarma(AlarmManager.INTERVAL_HALF_HOUR, getString(R.string.min30));
                break;
            case 4:
                setAlarma(AlarmManager.INTERVAL_FIFTEEN_MINUTES, getString(R.string.min15));
                break;
            case 5:
                AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                alarmMgr.cancel(PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ActualizaBD.class), PendingIntent.FLAG_IMMUTABLE));
                Toast.makeText(this, "Detenida la actualización de precios", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}