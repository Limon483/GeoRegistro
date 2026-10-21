package cl.ipvg.georegistro;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** RF03: recupera el listado persistente desde Firestore. */
public class ListadoActivity extends AppCompatActivity {

    private static final SimpleDateFormat FORMATO =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private ListView lista;
    private TextView tvVacio;
    private final List<String> items = new ArrayList<>();
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        lista = findViewById(R.id.lista);
        tvVacio = findViewById(R.id.tvVacio);
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lista.setAdapter(adaptador);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        FirebaseFirestore.getInstance().collection(Incidencia.COLECCION)
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    items.clear();
                    snap.getDocuments().forEach(doc -> {
                        Incidencia i = Incidencia.desde(doc);
                        items.add(String.format(Locale.getDefault(),
                                "%s  [%s]\n%s\n%s  (%.5f, %.5f)",
                                i.getTitulo(), i.getCategoria(), i.getDescripcion(),
                                FORMATO.format(new Date(i.getFechaHora())),
                                i.getLatitud(), i.getLongitud()));
                    });
                    adaptador.notifyDataSetChanged();
                    tvVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        // Sin red: se informa al usuario y la app sigue abierta
                        Toast.makeText(this, R.string.msg_error_red, Toast.LENGTH_LONG).show());
    }
}
