package com.example.cryptotracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.math.BigDecimal;

public class ActividadPerfilNotificacion extends AppCompatActivity {

    TextView txtSimbolo, txtNombre, txtPrecio;
    EditText txtBoxNotifSube, txtBoxNotifBaja;
    Switch switchNotifSube, switchNotifBaja;
    Button btnGuardar, btnQuitarSeguimiento;
    ImageView logo;
    SQLiteDatabase db;
    String id;
    int sube, baja;
    final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());




        setContentView(R.layout.activity_actividad_perfil_notificacion);

        //cargamos los view
        logo = findViewById(R.id.imageViewIconoPerfil);
        txtSimbolo = findViewById(R.id.textSimboloPerfil);
        txtNombre = findViewById(R.id.textNombrePerfil);
        txtPrecio = findViewById(R.id.textPrecioPerfil);
        txtBoxNotifSube = findViewById(R.id.editTextNotifSube);
        txtBoxNotifBaja = findViewById(R.id.editTextNotifBaja);
        switchNotifSube = findViewById(R.id.switchNotifSube);
        switchNotifBaja = findViewById(R.id.switchNotifBaja);
        btnGuardar = findViewById(R.id.buttonGuardarPerfil);
        btnQuitarSeguimiento = findViewById(R.id.buttonQuitarSeguimiento);

        //identificamos el perfil y lo cargamo
        Bundle parametro = getIntent().getExtras();
        id = parametro.getString("id");
        Picasso.with(this).load("https://s2.coinmarketcap.com/static/img/coins/64x64/" + id + ".png").into(logo);
        db = openOrCreateDatabase("CryptoTrackerMonedas", Context.MODE_PRIVATE, null);
        Cursor cursorBuscar = db.rawQuery("SELECT simbolo, nombre, precioUsd, sube, baja, notif_sube, notif_baja FROM monedasTab WHERE id=" + id, null);
        if (cursorBuscar.getCount() == 0) {
            Toast.makeText(this, "Error al consultar la Base de datos", Toast.LENGTH_LONG).show();
        }else{
            cursorBuscar.moveToNext();
            txtSimbolo.setText(cursorBuscar.getString(0));
            txtNombre.setText(cursorBuscar.getString(1));
            txtPrecio.setText(cursorBuscar.getString(2));
            if (cursorBuscar.getString(2) != null && cursorBuscar.getString(2).length() > 0) txtPrecio.setText(BigDecimal.valueOf(Double.parseDouble(cursorBuscar.getString(2))).toPlainString());
            txtBoxNotifSube.setText(cursorBuscar.getString(3));
            txtBoxNotifBaja.setText(cursorBuscar.getString(4));
            sube = cursorBuscar.getInt(5);
            baja = cursorBuscar.getInt(6);
            if (sube == 1) switchNotifSube.setChecked(true);
            if (baja == 1) switchNotifBaja.setChecked(true);
        }
        cursorBuscar.close();

        //evento btn quitar seguimiento
        btnQuitarSeguimiento.setOnClickListener(view -> {
            db.execSQL("UPDATE monedasTab SET seguimiento = 0 WHERE id=" + id);
            Toast.makeText(getApplicationContext(), "Eliminado de la lista de seguimiento", Toast.LENGTH_SHORT).show();
            finish();
        });

        //evento guardar
        btnGuardar.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
            } else {
                // permiso de internet concedido, continuar con el programa
                if(switchNotifSube.isChecked()) {
                    sube = 1;
                }else {
                    sube = 0;
                }
                if(switchNotifBaja.isChecked()) {
                    baja = 1;
                }else{
                    baja = 0;
                }
                db.execSQL("UPDATE monedasTab SET sube = '" + txtBoxNotifSube.getText() + "', baja ='" +txtBoxNotifBaja.getText() + "', notif_sube =" + sube + ", notif_baja = " + baja + " WHERE id=" + id);
                Toast.makeText(getApplicationContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(R.string.acceso_notificaciones);
                builder1.setCancelable(true);
                builder1.setNeutralButton(R.string.entendido, (dialog, id) -> dialog.cancel());
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }
}