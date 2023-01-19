package com.example.cryptotracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AdapterBuscaInv extends RecyclerView.Adapter<AdapterBuscaInv.MyViewHolder> implements View.OnClickListener{
    private final Context context;
    private final ArrayList<String> logoList, simboloList, nombreList;
    SQLiteDatabase db;

    public AdapterBuscaInv(Context context, ArrayList<String> logo, ArrayList<String> simbolo, ArrayList<String> nombre) {
        this.context = context;
        this.logoList = logo;
        this.simboloList = simbolo;
        this.nombreList = nombre;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.fila_buscar, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.simbolo.setText(String.valueOf(simboloList.get(i)));
        myViewHolder.nombre.setText(String.valueOf(nombreList.get(i)));
        Picasso.with(context).load("https://s2.coinmarketcap.com/static/img/coins/64x64/" + logoList.get(i) + ".png").into(myViewHolder.logo);
        myViewHolder.btnAdd.setTag(logoList.get(i));
        myViewHolder.btnAdd.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return nombreList.size();
    }

    @Override
    public void onClick(View view) {

        String tag = (String) view.getTag();
        db = context.openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);
        Cursor cursorTest = db.rawQuery("SELECT id FROM inversionesTab WHERE id=" + tag, null);
        if ((cursorTest.getCount() == 0)) {
            db.execSQL("INSERT INTO inversionesTab (id, cantidad) VALUES ('" + tag + "', 0)");
            cursorTest.close();
            db.close();
            Toast.makeText(context, "Añadido a la lista de inversiones", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, ActividadInversiones.class);
            context.startActivity(i);
        }else {
            Toast.makeText(context, "Ya estaba en seguimiento", Toast.LENGTH_SHORT).show();
        }
        cursorTest.close();
        db.close();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView simbolo, nombre;
        private final ImageView logo;
        private final Button btnAdd;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            simbolo = itemView.findViewById(R.id.textBuscarSimbol);
            nombre = itemView.findViewById(R.id.textBuscarNombre);
            logo = itemView.findViewById(R.id.imageBuscarIcono);
            btnAdd = itemView.findViewById(R.id.buttonBuscarAdd);
        }
    }

}
