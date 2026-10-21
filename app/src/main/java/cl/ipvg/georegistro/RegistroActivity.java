package cl.ipvg.georegistro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** RF02 (formulario), RF03 (guardar en Firestore) y RF04 (GPS). */
public class RegistroActivity extends AppCompatActivity {

    private static final SimpleDateFormat FORMATO =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private TextInputEditText etTitulo, etDescripcion, etLatitud, etLongitud;
    private Spinner spCategoria;
    private TextView tvFechaHora, tvEstadoUbicacion;
    private Button btnGuardar;

    private FusedLocationProviderClient clienteUbicacion;
    private final long fechaHora = System.currentTimeMillis();

    private final ActivityResultLauncher<String[]> solicitudPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                boolean concedido = Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (concedido) {
                    obtenerUbicacion();
                } else {
                    // Permiso denegado: la app sigue funcionando con coordenadas manuales
                    tvEstadoUbicacion.setText(R.string.msg_permiso_denegado);
                    Toast.makeText(this, R.string.msg_permiso_denegado, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        spCategoria = findViewById(R.id.spCategoria);
        tvFechaHora = findViewById(R.id.tvFechaHora);
        tvEstadoUbicacion = findViewById(R.id.tvEstadoUbicacion);
        btnGuardar = findViewById(R.id.btnGuardar);

        spCategoria.setAdapter(ArrayAdapter.createFromResource(this, R.array.categorias,
                android.R.layout.simple_spinner_dropdown_item));
        tvFechaHora.setText(FORMATO.format(new Date(fechaHora)));

        clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.btnUbicacion).setOnClickListener(v -> pedirUbicacion());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // ---------------- GPS (RF04) ----------------

    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirUbicacion() {
        if (tienePermiso()) {
            obtenerUbicacion();
            return;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.msg_permiso_explicacion)
                    .setPositiveButton(android.R.string.ok, (d, w) -> lanzarSolicitud())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            lanzarSolicitud();
        }
    }

    private void lanzarSolicitud() {
        solicitudPermiso.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @SuppressLint("MissingPermission") // se verifica en tienePermiso()
    private void obtenerUbicacion() {
        if (!tienePermiso()) return;
        clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                        new CancellationTokenSource().getToken())
                .addOnSuccessListener(this::mostrarUbicacion)
                .addOnFailureListener(e -> tvEstadoUbicacion.setText(R.string.msg_gps_desactivado));
    }

    private void mostrarUbicacion(Location loc) {
        if (loc == null) {
            tvEstadoUbicacion.setText(R.string.msg_gps_desactivado);
            return;
        }
        etLatitud.setText(String.format(Locale.US, "%.6f", loc.getLatitude()));
        etLongitud.setText(String.format(Locale.US, "%.6f", loc.getLongitude()));
        tvEstadoUbicacion.setText(String.format(Locale.getDefault(), "%s: %.6f, %.6f",
                getString(R.string.msg_ubicacion_ok), loc.getLatitude(), loc.getLongitude()));
    }

    // ---------------- Guardado (RF02, RF03) ----------------

    private boolean validar() {
        boolean ok = true;
        if (TextUtils.isEmpty(texto(etTitulo))) {
            etTitulo.setError(getString(R.string.err_campo_obligatorio));
            ok = false;
        }
        if (TextUtils.isEmpty(texto(etDescripcion))) {
            etDescripcion.setError(getString(R.string.err_campo_obligatorio));
            ok = false;
        }
        Double lat = numero(etLatitud);
        Double lon = numero(etLongitud);
        if (lat == null || lat < -90 || lat > 90) {
            etLatitud.setError(getString(R.string.err_coordenadas));
            ok = false;
        }
        if (lon == null || lon < -180 || lon > 180) {
            etLongitud.setError(getString(R.string.err_coordenadas));
            ok = false;
        }
        return ok;
    }

    private void guardar() {
        if (!validar()) return;

        Map<String, Object> datos = new HashMap<>();
        datos.put("titulo", texto(etTitulo));
        datos.put("descripcion", texto(etDescripcion));
        datos.put("categoria", String.valueOf(spCategoria.getSelectedItem()));
        datos.put("fechaHora", fechaHora);
        datos.put("latitud", numero(etLatitud));
        datos.put("longitud", numero(etLongitud));

        btnGuardar.setEnabled(false);
        FirebaseFirestore.getInstance().collection(Incidencia.COLECCION)
                .add(datos)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, R.string.msg_guardado, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // RF03: el error de red no cierra la aplicación
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, R.string.msg_error_red, Toast.LENGTH_LONG).show();
                });
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private Double numero(TextInputEditText et) {
        try {
            return Double.parseDouble(texto(et).replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
