package cl.ipvg.georegistro;

import com.google.firebase.firestore.DocumentSnapshot;

/** Modelo simple de una incidencia georreferenciada. */
public class Incidencia {
    public static final String COLECCION = "incidencias";

    private String id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private long fechaHora; // epoch en milisegundos
    private double latitud;
    private double longitud;

    public Incidencia() { }

    public Incidencia(String titulo, String descripcion, String categoria,
                      long fechaHora, double latitud, double longitud) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaHora = fechaHora;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /** Construye una incidencia desde un documento de Firestore. */
    public static Incidencia desde(DocumentSnapshot doc) {
        Incidencia i = new Incidencia();
        i.id = doc.getId();
        i.titulo = doc.getString("titulo");
        i.descripcion = doc.getString("descripcion");
        i.categoria = doc.getString("categoria");
        Long f = doc.getLong("fechaHora");
        i.fechaHora = f != null ? f : 0L;
        Double lat = doc.getDouble("latitud");
        Double lon = doc.getDouble("longitud");
        i.latitud = lat != null ? lat : 0d;
        i.longitud = lon != null ? lon : 0d;
        return i;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public long getFechaHora() { return fechaHora; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}
