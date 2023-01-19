package com.example.cryptotracker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Diálogo de selección de la dificultad del juego
 * @author Rodrigo Esteban-Infantes Fernández
 * @version 21.11.2020
 */
public class DialogoConfigura extends DialogFragment implements RadioGroup.OnCheckedChangeListener {
    RespuestaDialogoConfig respuestaConfig;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    /**
     * Método principal que muestra el diálogo
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        pref = getDefaultSharedPreferences(requireActivity().getApplicationContext());
        editor = pref.edit();
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View radioGroup = factory.inflate(R.layout.dialog_configura, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(radioGroup);
        RadioGroup radioDificultad = radioGroup.findViewById(R.id.radiogroup);
        builder.setTitle(getString(R.string.velocidad_actualizacion));
        builder.setNeutralButton(getString(R.string.volver), (dialog, id) -> {});
        int seleccion = pref.getInt(getString(R.string.velocidad_actualizacion), 5);
        radioDificultad.clearCheck();
        // marcamos la velocidad de actulaizacion guardada si hay alguna
        switch (seleccion){
            case 0:
                radioDificultad.check(R.id.horas24);
                break;
            case 1:
                radioDificultad.check(R.id.horas12);
                break;
            case 2:
                radioDificultad.check(R.id.hora1);
                break;
            case 3:
                radioDificultad.check(R.id.min30);
                break;
            case 4:
                radioDificultad.check(R.id.min15);
                break;
            case 5:
                radioDificultad.check(R.id.noActualizar);
                break;
        }
        radioDificultad.setOnCheckedChangeListener(this);
        return builder.create();
    }
    /**
     * Método que maneja los eventos al seleccionar los elementos del RadioGroup
     * @param group RadioGroup pulsado
     * @param a elemento del RadioGroup seleccionado
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(RadioGroup group, int a) {
        switch(a){
            case R.id.horas24:
                editor.putInt(getString(R.string.velocidad_actualizacion), 0);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(0);
                dismiss();
                break;
            case R.id.horas12:
                editor.putInt(getString(R.string.velocidad_actualizacion), 1);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(1);
                dismiss();
                break;
            case R.id.hora1:
                editor.putInt(getString(R.string.velocidad_actualizacion), 2);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(2);
                dismiss();
                break;
            case R.id.min30:
                editor.putInt(getString(R.string.velocidad_actualizacion), 3);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(3);
                dismiss();
                break;
            case R.id.min15:
                editor.putInt(getString(R.string.velocidad_actualizacion), 4);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(4);
                dismiss();
                break;
            case R.id.noActualizar:
                editor.putInt(getString(R.string.velocidad_actualizacion), 5);
                editor.apply();
                respuestaConfig.onRespuestaDialogoConfig(5);
                dismiss();
                break;
        }
    }
    //interfaz para la comunicación entre la Actividad y el Fragmento
    public interface RespuestaDialogoConfig{
        void onRespuestaDialogoConfig(int i);
    }
    //Se invoca cuando el fragmento se añade a la actividad
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        respuestaConfig=(RespuestaDialogoConfig)context;
    }
}