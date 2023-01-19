package com.example.cryptotracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DialogHistorial extends AppCompatActivity {
    RecyclerView recyclerView;
    AdapterHistorial adapterHistorial;
    ArrayList<String> simbolList, precioList, cantidadList, totalList, operacionList, fechaList;
    SQLiteDatabase db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_historial);

        recyclerView = findViewById(R.id.recyclerViewHistorial);

        simbolList = new ArrayList<>();
        precioList = new ArrayList<>();
        cantidadList = new ArrayList<>();
        totalList = new ArrayList<>();
        operacionList = new ArrayList<>();
        fechaList = new ArrayList<>();

        //cargamos la primera fila
        simbolList.add(getResources().getString(R.string.signo));
        precioList.add(getResources().getString(R.string.precio_usd));
        cantidadList.add(getResources().getString(R.string.cantidad));
        totalList.add(getResources().getString(R.string.total));
        operacionList.add(getResources().getString(R.string.oper));
        fechaList.add(getResources().getString(R.string.fecha));

        db = openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);

        Cursor cursorBuscar = db.rawQuery("SELECT * FROM historicoTab ORDER BY fecha DESC", null);
        if (cursorBuscar.getCount() > 0) {
            while (cursorBuscar.moveToNext()) {
                simbolList.add(cursorBuscar.getString(0));
                fechaList.add(cursorBuscar.getString(1));
                operacionList.add(String.valueOf(cursorBuscar.getInt(2)));
                precioList.add(cursorBuscar.getString(3));
                cantidadList.add(cursorBuscar.getString(4));
                totalList.add(cursorBuscar.getString(5));
            }
        }
        cursorBuscar.close();

        adapterHistorial = new AdapterHistorial(this, simbolList, fechaList, operacionList, precioList, cantidadList, totalList);
        recyclerView.setAdapter(adapterHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

}