package com.example.cryptotracker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AdapterInversiones extends RecyclerView.Adapter<AdapterInversiones.MyViewHolder> implements View.OnClickListener{
    private final Context context;
    private final ArrayList<String> simboloList, precioList, cantidadList, totalList, idList;

    public AdapterInversiones(Context context, ArrayList<String> simbolo, ArrayList<String> precio, ArrayList<String> cantidad, ArrayList<String> total, ArrayList<String> ids) {
        this.context = context;
        this.simboloList = simbolo;
        this.precioList = precio;
        this.cantidadList = cantidad;
        this.totalList = total;
        this.idList = ids;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.fila_inversiones, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.simbolo.setText(String.valueOf(simboloList.get(i)));
        myViewHolder.precio.setText(String.valueOf(precioList.get(i)));
        if (i != 0 && precioList.get(i) != null && precioList.get(i).length() > 0) myViewHolder.precio.setText(BigDecimal.valueOf(Double.parseDouble(precioList.get(i))).toPlainString());
        myViewHolder.cantidad.setText(String.valueOf(cantidadList.get(i)));
        if (i != 0 && cantidadList.get(i) != null && cantidadList.get(i).length() > 0) myViewHolder.cantidad.setText(BigDecimal.valueOf(Double.parseDouble(cantidadList.get(i))).toPlainString());
        myViewHolder.total.setText(String.valueOf(totalList.get(i)));
        if (i != 0 && totalList.get(i) != null && totalList.get(i).length() > 0) myViewHolder.total.setText(BigDecimal.valueOf(Double.parseDouble(totalList.get(i))).toPlainString());

        if (!(idList.get(i).equals("FIRST") || idList.get(i).equals("DOLAR"))) {
            myViewHolder.itemView.setTag(R.id.TAG_ID, idList.get(i));
            myViewHolder.itemView.setTag(R.id.TAG_SIMBOLO, simboloList.get(i));
            myViewHolder.itemView.setTag(R.id.TAG_CANTIDAD, cantidadList.get(i));
            myViewHolder.itemView.setTag(R.id.TAG_PRECIO, precioList.get(i));
            myViewHolder.itemView.setOnClickListener(this);
        }

        if ((i % 2) == 0) myViewHolder.itemView.setBackground(context.getDrawable(R.drawable.layout_filapar));

    }

    @Override
    public int getItemCount() {
        return simboloList.size();
    }

    @Override
    public void onClick(View view) {
        DialogCompraVenta newFragment = DialogCompraVenta.newInstance((String) view.getTag(R.id.TAG_ID), view.getTag(R.id.TAG_SIMBOLO).toString(), view.getTag(R.id.TAG_CANTIDAD).toString(), view.getTag(R.id.TAG_PRECIO).toString());
        newFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "CompraVenta");
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView simbolo, precio, cantidad, total;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            simbolo = itemView.findViewById(R.id.textViewInvSigno);
            precio = itemView.findViewById(R.id.textViewInvPrecio);
            cantidad = itemView.findViewById(R.id.textViewInvCantidad);
            total = itemView.findViewById(R.id.textViewInvTotal);
        }
    }

}