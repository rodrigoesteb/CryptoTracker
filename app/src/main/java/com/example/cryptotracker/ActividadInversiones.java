package com.example.cryptotracker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

public class ActividadInversiones extends AppCompatActivity implements SearchView.OnQueryTextListener, DialogCompraVenta.RespuestaDialogCompraventa {



    SQLiteDatabase db;
    RecyclerView recyclerView;
    ArrayList<String> nombre, simbolo, logo, precio, cantidad, total, idList;
    AdapterBuscaInv adapter;
    AdapterInversiones adapterInversiones;
    SearchView busquedaView;
    Button btnReset;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    double balanceTotalDouble;
    TextView textViewBalanceTotal;
    private final static int DIGITOS_INVERSIONES = 6;
    final static int OPERACION_COMPRA = 0;
    final static int OPERACION_VENTA = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());




        setContentView(R.layout.activity_actividad_inversiones);
        sharedPref = getDefaultSharedPreferences(this);
        sharedPrefEditor = sharedPref.edit();

        textViewBalanceTotal = findViewById(R.id.textViewBalanceTotal);
        busquedaView = findViewById(R.id.searchInversiones);
        busquedaView.setOnQueryTextListener(this);
        recyclerView = findViewById(R.id.recyclerViewInversiones);
        db = openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS inversionesTab(id VARCHAR(5) PRIMARY KEY, cantidad VARCHAR(15));");
        db.execSQL("CREATE TABLE IF NOT EXISTS historicoTab(id VARCHAR(5), fecha DATETIME DEFAULT CURRENT_TIMESTAMP, operacion INTEGER(1), precio VARCHAR(15), cantidad VARCHAR(15), total VARCHAR(15));");

        cargarListaInversiones();

        btnReset = findViewById(R.id.btnResetInversiones);
        btnReset.setOnClickListener(view -> {
            sharedPrefEditor.putBoolean("Reset", true);
            sharedPrefEditor.apply();
            cargarListaInversiones();
        });
    }

    public void cargarListaInversiones(){
        balanceTotalDouble = 0;
        boolean reset = sharedPref.getBoolean("Reset", true);
        if (reset) {
            db.execSQL("DROP TABLE IF EXISTS inversionesTab");
            db.execSQL("DROP TABLE IF EXISTS historicoTab");
            db.execSQL("CREATE TABLE IF NOT EXISTS inversionesTab(id VARCHAR(5) PRIMARY KEY, cantidad VARCHAR(15));");
            db.execSQL("CREATE TABLE IF NOT EXISTS historicoTab(id VARCHAR(5), fecha DATETIME DEFAULT CURRENT_TIMESTAMP, operacion INTEGER(1), precio VARCHAR(15), cantidad VARCHAR(15), total VARCHAR(15));");

            db.execSQL("INSERT INTO inversionesTab (id, cantidad) VALUES ('DOLAR', 10000)");
            sharedPrefEditor.putBoolean("Reset", false);
            sharedPrefEditor.apply();
        }
        simbolo = new ArrayList<>();
        precio = new ArrayList<>();
        cantidad = new ArrayList<>();
        total = new ArrayList<>();
        idList = new ArrayList<>();
        //cargamos la primera fila de la tabla
        simbolo.add(this.getResources().getString(R.string.signo));
        precio.add(this.getResources().getString(R.string.precio_usd));
        cantidad.add(this.getResources().getString(R.string.cantidad));
        total.add(this.getResources().getString(R.string.total_usd));
        idList.add("FIRST");

        Cursor cursorBuscar = db.rawQuery("SELECT id, cantidad FROM inversionesTab", null);
        if (cursorBuscar.getCount() > 0) {
            while (cursorBuscar.moveToNext()) {
                String id = cursorBuscar.getString(0);
                idList.add(id);
                if (id.equals("DOLAR")) {
                    simbolo.add("USD");
                    precio.add("1");
                    //meter el total multiplicado y redondeado
                    //redondeamos el cantidad a digitosPrecio
                    double cantidadDouble = 0;
                    if (cursorBuscar.getString(1) != null && cursorBuscar.getString(1).length() > 0) cantidadDouble = Double.parseDouble(cursorBuscar.getString(1));
                    BigDecimal bdCantidad = new BigDecimal(cantidadDouble);
                    bdCantidad = bdCantidad.round(new MathContext(DIGITOS_INVERSIONES));
                    double roundedCantidad = bdCantidad.doubleValue();
                    cantidad.add(String.valueOf(roundedCantidad));
                    total.add(String.valueOf(roundedCantidad));
                    //manera temporal de llevar el balance
                    balanceTotalDouble += roundedCantidad;

                }else {
                    Cursor cursorSecundario = db.rawQuery("SELECT simbolo, precioUsd FROM monedasTab WHERE id=" + id, null);
                    if ((cursorSecundario.getCount() == 0)) {
                        Toast.makeText(this, "Error en la Base de datos", Toast.LENGTH_LONG).show();
                    }else {
                        cursorSecundario.moveToNext();
                        simbolo.add(cursorSecundario.getString(0));
                        //redondeamos el cantidad a digitosPrecio
                        double cantidadDouble = 0;
                        if (cursorBuscar.getString(1) != null && cursorBuscar.getString(1).length() > 0) cantidadDouble = Double.parseDouble(cursorBuscar.getString(1));
                        BigDecimal bdCantidad = new BigDecimal(cantidadDouble);
                        bdCantidad = bdCantidad.round(new MathContext(DIGITOS_INVERSIONES));
                        double roundedCantidad = bdCantidad.doubleValue();
                        cantidad.add(String.valueOf(roundedCantidad));
                        //meter el total multiplicado y redondeado
                        double precioDouble = 0;
                        if (cursorSecundario.getString(1) != null && cursorSecundario.getString(1).length() > 0) precioDouble = Double.parseDouble(cursorSecundario.getString(1));
                        precio.add(String.valueOf(precioDouble));
                        double totalDouble = roundedCantidad * precioDouble;
                        BigDecimal bdTotal = new BigDecimal(totalDouble);
                        bdTotal = bdTotal.round(new MathContext(DIGITOS_INVERSIONES));
                        double roundedTotal = bdTotal.doubleValue();
                        total.add(String.valueOf(roundedTotal));
                        //manera temporal de llevar el balance
                        balanceTotalDouble += roundedTotal;
                    }
                    cursorSecundario.close();
                }
            }
            textViewBalanceTotal.setText(BigDecimal.valueOf(BigDecimal.valueOf(balanceTotalDouble).round(new MathContext(DIGITOS_INVERSIONES)).doubleValue()).toPlainString());
            //BigDecimal.valueOf(BigDecimal.valueOf(balanceTotalDouble).round(new MathContext(DIGITOS_INVERSIONES)).doubleValue()).toPlainString()
            adapterInversiones = new AdapterInversiones(this, simbolo, precio, cantidad, total, idList);
            recyclerView.setAdapter(adapterInversiones);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }else{
            Toast.makeText(ActividadInversiones.this, "Lista de inversiones vacia", Toast.LENGTH_LONG).show();
        }
        cursorBuscar.close();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        nombre = new ArrayList<>();
        simbolo = new ArrayList<>();
        logo = new ArrayList<>();
        Cursor cursorBuscar = db.rawQuery("SELECT id, simbolo, nombre FROM monedasTab WHERE simbolo LIKE '%" + s + "%'" + " OR nombre LIKE '%" + s + "%'", null);
        if (cursorBuscar.getCount() > 0) {
            Toast.makeText(ActividadInversiones.this, "" + cursorBuscar.getCount() +  " coincidencias encontradas", Toast.LENGTH_SHORT).show();
            while (cursorBuscar.moveToNext()) {
                logo.add(cursorBuscar.getString(0));
                simbolo.add(cursorBuscar.getString(1));
                nombre.add(cursorBuscar.getString(2));
            }
        }else{
            Toast.makeText(ActividadInversiones.this, "No se ha encontrado la busqueda", Toast.LENGTH_LONG).show();
        }
        cursorBuscar.close();
        adapter = new AdapterBuscaInv(this, logo, simbolo, nombre);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_actividad_inversiones,menu);
        return true;
    }
    /**
     * Método que maneja los eventos al hacer click en los elementos del menú
     * @param item componente pulsad
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.ver_historial) {
            ActividadInversiones.this.startActivity(new Intent(this, DialogHistorial.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRespuestaDialogCompraVenta(String simbolo, int operacion, double precio, double cantidad, double total) {
        db.execSQL("INSERT INTO historicoTab (id, operacion, precio, cantidad, total) VALUES ('" + simbolo + "', " + operacion + ", '" + precio + "', '" + cantidad + "', '" + total + "')"  );
        cargarListaInversiones();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}