package com.example.cryptotracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Diálogo de selección de la dificultad del juego
 * @author Rodrigo Esteban-Infantes Fernández
 * @version 21.11.2020
 */
public class DialogCompraVenta extends DialogFragment implements TextWatcher {
    RespuestaDialogCompraventa respuestaDialogCompraventa;
    String signoF, cantidadF, balanceUsdF, precioF, id;
    SQLiteDatabase db;
    EditText editTextCantidadComprarFCV, editTextTotalComprarFCV, editTextCantidadVenderFCV, editTextTotalVenderFCV;
    double precioDouble, balanceDouble, cantidadDouble;
    private final int DIGITOS_PRECIO = 6;

    public static DialogCompraVenta newInstance(String id, String signo, String cantidad, String precio) {
        DialogCompraVenta fragment = new DialogCompraVenta();
        fragment.setParametros(id, signo, cantidad, precio);
        return fragment;
    }

    public void setParametros(String id, String signo, String cantidad, String precio) {
        this.id = id;
        this.signoF = signo;
        this.cantidadF = cantidad;
        this.precioF = precio;
        this.precioDouble = Double.parseDouble(precio);
        this.cantidadDouble = Double.parseDouble(cantidad);
    }

    /**
     * Método principal que muestra el diálogo
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View FCV = factory.inflate(R.layout.dialog_compra_venta, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(FCV);
        db = requireContext().openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);

        TextView textViewSignoFCV = FCV.findViewById(R.id.textViewSignoFCV);
        TextView textViewPrecioFCV = FCV.findViewById(R.id.textViewPrecioFCV);
        TextView textViewBalanceUsdFCV = FCV.findViewById(R.id.textViewBalanceUsdFCV);
        editTextCantidadComprarFCV = FCV.findViewById(R.id.editTextCantidadComprarFCV);
        editTextCantidadComprarFCV.addTextChangedListener(this);
        editTextTotalComprarFCV = FCV.findViewById(R.id.editTextTotalComprarFCV);
        editTextTotalComprarFCV.addTextChangedListener(this);
        TextView textViewSignoTotalFCV = FCV.findViewById(R.id.textViewSignoTotalFCV);
        Button buttonComprarFCV = FCV.findViewById(R.id.buttonComprarFCV);
        TextView textViewBalanceIdFCV = FCV.findViewById(R.id.textViewBalanceIdFCV);
        TextView textViewSignoDispVenderFCV = FCV.findViewById(R.id.textViewSignoDispVenderFCV);
        TextView textViewSignoCantidadFCV = FCV.findViewById(R.id.textViewSignoCantidadFCV);
        editTextCantidadVenderFCV = FCV.findViewById(R.id.editTextCantidadVenderFCV);
        editTextCantidadVenderFCV.addTextChangedListener(this);
        editTextTotalVenderFCV = FCV.findViewById(R.id.editTextTotalVenderFCV);
        editTextTotalVenderFCV.addTextChangedListener(this);
        Button buttonVender = FCV.findViewById(R.id.buttonVenderFCV);

        textViewSignoFCV.setText(signoF);
        textViewSignoTotalFCV.setText(signoF);
        textViewSignoDispVenderFCV.setText(signoF);
        textViewSignoCantidadFCV.setText(signoF);
        textViewBalanceIdFCV.setText(cantidadF);

        // coger el balance de la db inversiones tab id dollar
        Cursor cursorTest = db.rawQuery("SELECT cantidad FROM inversionesTab WHERE id='DOLAR'", null);
        if ((cursorTest.getCount() == 0)) {
            Toast.makeText(getContext(), "Error en la Base de datos", Toast.LENGTH_LONG).show();
        }else {
            cursorTest.moveToNext();
            balanceUsdF = cursorTest.getString(0);
            balanceDouble = Double.parseDouble(balanceUsdF);
            textViewBalanceUsdFCV.setText(balanceUsdF);
        }
        cursorTest.close();

        textViewPrecioFCV.setText(precioF);
        if (precioF != null && precioF.length() > 0) textViewPrecioFCV.setText(BigDecimal.valueOf(Double.parseDouble(precioF)).toPlainString());

        buttonComprarFCV.setOnClickListener(view -> {
            if (editTextCantidadComprarFCV.getText() != null && editTextCantidadComprarFCV.getText().length() > 0 && editTextTotalComprarFCV.getText() != null && editTextTotalComprarFCV.getText().length() > 0) {
                double compraCantidad = Double.parseDouble(String.valueOf(editTextCantidadComprarFCV.getText()));
                double compraTotal = Double.parseDouble(String.valueOf(editTextTotalComprarFCV.getText()));
                db.execSQL("UPDATE inversionesTab SET cantidad=" + (balanceDouble - compraCantidad) + " WHERE id='DOLAR'");
                db.execSQL("UPDATE inversionesTab SET cantidad=" + (compraTotal + cantidadDouble) + " WHERE id=" + id);
                respuestaDialogCompraventa.onRespuestaDialogCompraVenta(signoF, ActividadInversiones.OPERACION_COMPRA, precioDouble, compraCantidad, compraTotal);
                dismiss();
            }
        });

        buttonVender.setOnClickListener(view -> {
            if (editTextCantidadVenderFCV.getText() != null && editTextCantidadVenderFCV.getText().length() > 0 && editTextTotalVenderFCV.getText() != null && editTextTotalVenderFCV.getText().length() > 0) {
                double ventaCantidad = Double.parseDouble(String.valueOf(editTextCantidadVenderFCV.getText()));
                double ventaTotal = Double.parseDouble(String.valueOf(editTextTotalVenderFCV.getText()));
                db.execSQL("UPDATE inversionesTab SET cantidad=" + (balanceDouble + ventaTotal) + " WHERE id='DOLAR'");
                db.execSQL("UPDATE inversionesTab SET cantidad=" + (cantidadDouble - ventaCantidad) + " WHERE id=" + id);
                respuestaDialogCompraventa.onRespuestaDialogCompraVenta(signoF, ActividadInversiones.OPERACION_VENTA, precioDouble, ventaCantidad, ventaTotal);
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable == editTextCantidadComprarFCV.getEditableText()) {
            double numeroIntroducido = 0;
            // si hay algo escrito
            if (editTextCantidadComprarFCV.getText() != null && editTextCantidadComprarFCV.getText().length() > 0)
                numeroIntroducido = Double.parseDouble(String.valueOf(editTextCantidadComprarFCV.getText()));
            // no puedes gastar mas de lo que tienes
            if (numeroIntroducido > balanceDouble) editTextCantidadComprarFCV.setText(BigDecimal.valueOf(balanceDouble).toPlainString());
            else {
                double totalAux = 0;
                // si hay algo escrito en el otro campo
                if (editTextTotalComprarFCV.getText() != null && editTextTotalComprarFCV.getText().length() > 0)
                    totalAux = Double.parseDouble(String.valueOf(editTextTotalComprarFCV.getText()));
                // comprobamos que coincide el total con la cantidad * precio
                if (numeroIntroducido != BigDecimal.valueOf(precioDouble * totalAux).round(new MathContext(7)).doubleValue())
                    //redondeamos a 7 cifras significativas y evitamos que se escriba notacion cientifica
                    editTextTotalComprarFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(numeroIntroducido / precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            }

        } else if (editable == editTextTotalComprarFCV.getEditableText()) {
            double numeroIntroducido = 0;
            if (editTextTotalComprarFCV.getText() != null && editTextTotalComprarFCV.getText().length() > 0)
                numeroIntroducido = Double.parseDouble(String.valueOf(editTextTotalComprarFCV.getText()));
            if (numeroIntroducido > (balanceDouble / precioDouble)) editTextTotalComprarFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(balanceDouble / precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            else {
                double cantidadAux = 0;
                if (editTextCantidadComprarFCV.getText() != null && editTextCantidadComprarFCV.getText().length() > 0)
                    cantidadAux = Double.parseDouble(String.valueOf(editTextCantidadComprarFCV.getText()));
                if (numeroIntroducido != (BigDecimal.valueOf(cantidadAux / precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()))
                    editTextCantidadComprarFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(numeroIntroducido * precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            }

        } else if (editable == editTextCantidadVenderFCV.getEditableText()) {
            double numeroIntroducido = 0;
            if (editTextCantidadVenderFCV.getText() != null && editTextCantidadVenderFCV.getText().length() > 0)
                numeroIntroducido = Double.parseDouble(String.valueOf(editTextCantidadVenderFCV.getText()));
            if (numeroIntroducido > cantidadDouble) editTextCantidadVenderFCV.setText(BigDecimal.valueOf(cantidadDouble).toPlainString());
            else {
                double totalAux = 0;
                if (editTextTotalVenderFCV.getText() != null && editTextTotalVenderFCV.getText().length() > 0)
                    totalAux = Double.parseDouble(String.valueOf(editTextTotalVenderFCV.getText()));
                if (numeroIntroducido != BigDecimal.valueOf(totalAux / precioDouble).round(new MathContext(7)).doubleValue())
                    //redondeamos a 7 cifras significativas y evitamos que se escriba notacion cientifica
                    editTextTotalVenderFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(numeroIntroducido * precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            }

        } else if (editable == editTextTotalVenderFCV.getEditableText()) {
            double numeroIntroducido = 0;
            if (editTextTotalVenderFCV.getText() != null && editTextTotalVenderFCV.getText().length() > 0)
                numeroIntroducido = Double.parseDouble(String.valueOf(editTextTotalVenderFCV.getText()));
            if (numeroIntroducido > (cantidadDouble * precioDouble)) editTextTotalVenderFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(cantidadDouble * precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            else {
                double cantidadAux = 0;
                if (editTextCantidadVenderFCV.getText() != null && editTextCantidadVenderFCV.getText().length() > 0)
                    cantidadAux = Double.parseDouble(String.valueOf(editTextCantidadVenderFCV.getText()));
                if (numeroIntroducido != (BigDecimal.valueOf(cantidadAux * precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()))
                    editTextCantidadVenderFCV.setText(BigDecimal.valueOf(BigDecimal.valueOf(numeroIntroducido / precioDouble).round(new MathContext(DIGITOS_PRECIO, RoundingMode.DOWN)).doubleValue()).toPlainString());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }

    //interfaz para la comunicación entre la Actividad y el Fragmento
    public interface RespuestaDialogCompraventa{
        void onRespuestaDialogCompraVenta(String simbolo, int operacion, double precio, double cantidad, double total);
    }
    //Se invoca cuando el fragmento se añade a la actividad
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        respuestaDialogCompraventa=(RespuestaDialogCompraventa) context;
    }
}