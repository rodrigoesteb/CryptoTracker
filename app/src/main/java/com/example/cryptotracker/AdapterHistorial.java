package com.example.cryptotracker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.math.BigDecimal;
import java.util.ArrayList;

public class AdapterHistorial extends RecyclerView.Adapter<AdapterHistorial.MyViewHolder> {
    private final Context context;
    private final ArrayList<String> simboloList, precioList, cantidadList, totalList, operacionList, fechasList;

    public AdapterHistorial(Context context, ArrayList<String> simbolo, ArrayList<String> fechasList, ArrayList<String> operacion, ArrayList<String> precio, ArrayList<String> cantidad, ArrayList<String> total) {
        this.context = context;
        this.simboloList = simbolo;
        this.precioList = precio;
        this.cantidadList = cantidad;
        this.totalList = total;
        this.operacionList = operacion;
        this.fechasList = fechasList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.fila_historial, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.simbolo.setText(String.valueOf(simboloList.get(i)));
        myViewHolder.precio.setText(String.valueOf(precioList.get(i)));
        myViewHolder.cantidad.setText(String.valueOf(cantidadList.get(i)));
        myViewHolder.total.setText(String.valueOf(totalList.get(i)));
        if (operacionList.get(i).equals("0")) myViewHolder.operacion.setText(R.string.compra);
        else if (operacionList.get(i).equals(context.getResources().getString(R.string.oper))) myViewHolder.operacion.setText(R.string.oper);
        else myViewHolder.operacion.setText(R.string.venta);
        myViewHolder.fecha.setText(String.valueOf(fechasList.get(i)));

        if ((i % 2) == 0) myViewHolder.itemView.setBackground(context.getDrawable(R.drawable.layout_filapar));
    }

    @Override
    public int getItemCount() {
        return simboloList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView simbolo, fecha, operacion, precio, cantidad, total;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            simbolo = itemView.findViewById(R.id.textViewSimbHist);
            fecha = itemView.findViewById(R.id.textViewFechaHist);
            operacion = itemView.findViewById(R.id.textViewOperHist);
            precio = itemView.findViewById(R.id.textViewPrecioHist);
            cantidad = itemView.findViewById(R.id.textViewCantHist);
            total = itemView.findViewById(R.id.textViewTotalHist);
        }
    }

}