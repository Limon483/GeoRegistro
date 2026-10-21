package cl.ipvg.georegistro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** RF01: pantalla principal con navegación a registro, listado, mapa y sensor. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnRegistrar).setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
        findViewById(R.id.btnListado).setOnClickListener(v ->
                startActivity(new Intent(this, ListadoActivity.class)));
        findViewById(R.id.btnMapa).setOnClickListener(v ->
                startActivity(new Intent(this, MapaActivity.class)));
        findViewById(R.id.btnSensor).setOnClickListener(v ->
                startActivity(new Intent(this, SensorActivity.class)));
    }
}
