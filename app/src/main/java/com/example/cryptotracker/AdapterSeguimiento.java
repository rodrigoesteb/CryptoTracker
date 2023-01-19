package com.example.cryptotracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.math.BigDecimal;
import java.util.ArrayList;

public class AdapterSeguimiento extends RecyclerView.Adapter<AdapterSeguimiento.MyViewHolder> implements View.OnClickListener{

    private final Context context;
    private final ArrayList<String> logoList, simboloList, nombreList, precioUsd, cambio24hList;
    SQLiteDatabase db;

    public AdapterSeguimiento(Context context, ArrayList<String> logo, ArrayList<String> simbolo, ArrayList<String> nombre, ArrayList<String> precioUsd, ArrayList<String> cambio24h) {
        this.context = context;
        this.logoList = logo;
        this.simboloList = simbolo;
        this.nombreList = nombre;
        this.precioUsd = precioUsd;
        this.cambio24hList = cambio24h;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.fila_seguimiento, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.simbolo.setText(String.valueOf(simboloList.get(i)));
        myViewHolder.nombre.setText(String.valueOf(nombreList.get(i)));
        Picasso.with(context).load("https://s2.coinmarketcap.com/static/img/coins/64x64/" + logoList.get(i) + ".png").into(myViewHolder.logo);
        //cambiamos el color de la campana si no esta activada ninguna notificación
        db = context.openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);
        Cursor cursorTest = db.rawQuery("SELECT notif_sube, notif_baja FROM monedasTab WHERE id=" + logoList.get(i), null);
        if (cursorTest.getCount() == 0) {
            Toast.makeText(context, "Error al consultar la Base de datos", Toast.LENGTH_LONG).show();
        }else{
            cursorTest.moveToNext();
            int sube, baja;
            sube = cursorTest.getInt(0);
            baja = cursorTest.getInt(1);
            if ((sube == 0) && (baja == 0)) myViewHolder.campana.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        cursorTest.close();
        db.close();
        // poner el evento en la vista
        myViewHolder.itemView.setTag(logoList.get(i));
        myViewHolder.itemView.setOnClickListener(this);
        myViewHolder.precio.setText(String.valueOf(precioUsd.get(i)));
        // String.trim()' on a null object reference
        if (precioUsd.get(i) != null && precioUsd.get(i).length() > 0) myViewHolder.precio.setText(BigDecimal.valueOf(Double.parseDouble(precioUsd.get(i))).toPlainString());
        myViewHolder.cambio24h.setText(String.valueOf(cambio24hList.get(i)));
        // si tenemos un numero negativo en cambio24h ajustamos la flechita
        if (cambio24hList.get(i) != null && cambio24hList.get(i).length() > 0) {
            if (Double.parseDouble(cambio24hList.get(i)) < 0) {
                //@android:drawable/arrow_up_float
                myViewHolder.cambio.setBackground(ContextCompat.getDrawable(context, android.R.drawable.arrow_down_float));
                myViewHolder.cambio.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.red_down, null)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return nombreList.size();
    }

    @Override
    public void onClick(View view) {
        // crear el panel de notificacion y enviarle el id view.getTag()
        Intent intent = new Intent(context, ActividadPerfilNotificacion.class);
        intent.putExtra("id", (String) view.getTag());
        context.startActivity(intent);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView simbolo, nombre, precio, cambio24h;
        private final ImageView logo, cambio, campana;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            cambio = itemView.findViewById(R.id.imageViewPorcentaje);
            simbolo = itemView.findViewById(R.id.textSimboloMoneda);
            nombre = itemView.findViewById(R.id.textNombreMoneda);
            logo = itemView.findViewById(R.id.imageSimbol);
            campana = itemView.findViewById(R.id.imageViewCampanaNotif);
            precio = itemView.findViewById(R.id.textPrecio);
            cambio24h = itemView.findViewById(R.id.textViewPorcentaje);
        }
    }
}
