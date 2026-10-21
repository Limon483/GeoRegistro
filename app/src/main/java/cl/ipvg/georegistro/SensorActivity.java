package cl.ipvg.georegistro;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/** RF06: acelerómetro con detección de movimiento y control de dispositivos sin sensor. */
public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    // Umbral (m/s²) de variación respecto a la gravedad para considerar "movimiento"
    private static final float UMBRAL_MOVIMIENTO = 2.0f;

    private SensorManager sensorManager;
    private Sensor acelerometro;
    private TextView tvValores, tvEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvValores = findViewById(R.id.tvValores);
        tvEstado = findViewById(R.id.tvEstadoSensor);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager != null
                ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;

        if (acelerometro == null) {
            // Dispositivo sin sensor: se informa y la app no falla
            tvEstado.setText(R.string.msg_sin_acelerometro);
            tvValores.setText("—");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];
        tvValores.setText(String.format(Locale.getDefault(),
                "X: %.2f\nY: %.2f\nZ: %.2f  (m/s²)", x, y, z));

        double magnitud = Math.sqrt(x * x + y * y + z * z);
        double variacion = Math.abs(magnitud - SensorManager.GRAVITY_EARTH);
        tvEstado.setText(variacion > UMBRAL_MOVIMIENTO ? R.string.msg_movimiento : R.string.msg_quieto);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
