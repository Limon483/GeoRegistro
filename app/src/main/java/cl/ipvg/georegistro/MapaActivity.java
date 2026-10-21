package cl.ipvg.georegistro;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/** RF05: muestra marcadores con la ubicación de las incidencias guardadas. */
public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment fragmento = (SupportMapFragment) fm.findFragmentById(R.id.mapa);
        if (fragmento != null) fragmento.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);

        FirebaseFirestore.getInstance().collection(Incidencia.COLECCION)
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, R.string.msg_sin_incidencias_mapa, Toast.LENGTH_LONG).show();
                        return;
                    }
                    LatLngBounds.Builder limites = new LatLngBounds.Builder();
                    int cantidad = 0;
                    for (var doc : snap.getDocuments()) {
                        Incidencia i = Incidencia.desde(doc);
                        LatLng pos = new LatLng(i.getLatitud(), i.getLongitud());
                        mapa.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(i.getTitulo())
                                .snippet(i.getCategoria()));
                        limites.include(pos);
                        cantidad++;
                    }
                    final int total = cantidad;
                    final LatLngBounds area = limites.build();
                    mapa.setOnMapLoadedCallback(() -> {
                        if (total == 1) {
                            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(area.getCenter(), 15f));
                        } else {
                            mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(area, 120));
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.msg_error_red, Toast.LENGTH_LONG).show());
    }
}
