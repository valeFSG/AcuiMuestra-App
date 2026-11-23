package com.acuimuestra.backend.service;

import com.acuimuestra.backend.dto.CambioEstadoRequest;
import com.acuimuestra.backend.dto.MuestraRequest;
import com.acuimuestra.backend.dto.ResumenResponse;
import com.acuimuestra.backend.model.EstadoRevision;
import com.acuimuestra.backend.model.FotoMuestra;
import com.acuimuestra.backend.model.Muestra;
import com.acuimuestra.backend.model.Rol;
import com.acuimuestra.backend.model.Usuario;
import com.acuimuestra.backend.repository.FotoMuestraRepository;
import com.acuimuestra.backend.repository.MuestraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class MuestraService {

    public static final int MIN_COMENTARIO = 10;
    public static final int MAX_FOTO_BYTES = 3 * 1024 * 1024;

    private static final EnumSet<EstadoRevision> REVISABLES = EnumSet.of(EstadoRevision.PENDIENTE, EstadoRevision.CORREGIDO);
    private static final EnumSet<EstadoRevision> DECISIONES = EnumSet.of(
            EstadoRevision.OBSERVADO, EstadoRevision.VALIDADO, EstadoRevision.DESCARTADO);

    public record Creacion(Muestra muestra, boolean nueva) {}

    private final MuestraRepository muestras;
    private final FotoMuestraRepository fotos;
    private final AccesoService acceso;

    public MuestraService(MuestraRepository muestras, FotoMuestraRepository fotos, AccesoService acceso) {
        this.muestras = muestras;
        this.fotos = fotos;
        this.acceso = acceso;
    }

    @Transactional(readOnly = true)
    public List<Muestra> listar(String usuario, String centro, EstadoRevision estado) {
        acceso.requerir(usuario);
        return muestras.filtrar(vacioANull(centro), estado);
    }

    @Transactional(readOnly = true)
    public Muestra obtener(String usuario, String id) {
        acceso.requerir(usuario);
        return buscar(id);
    }

    @Transactional
    public Creacion crear(String usuario, MuestraRequest req) {
        Usuario operador = acceso.requerir(usuario, Rol.OPERADOR);
        return muestras.findById(req.id())
                .map(existente -> new Creacion(existente, false))
                .orElseGet(() -> {
                    Muestra m = new Muestra(req.id());
                    copiarDatos(req, m);
                    m.setCentro(req.centro().trim());
                    m.setTren(req.tren().trim());
                    m.setLinea(req.linea().trim());
                    m.setFechaHora(req.fechaHora());
                    m.setHoraVerificada(req.horaVerificada() == null || req.horaVerificada());
                    m.setOperador(operador.getNombreUsuario());
                    return new Creacion(muestras.save(m), true);
                });
    }

    @Transactional
    public Muestra corregir(String usuario, String id, MuestraRequest req) {
        acceso.requerir(usuario, Rol.OPERADOR);
        Muestra m = buscar(id);
        if (m.getEstado() != EstadoRevision.OBSERVADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden corregir muestras observadas");
        }
        copiarDatos(req, m);
        m.setEstado(EstadoRevision.CORREGIDO);
        return muestras.save(m);
    }

    @Transactional
    public Muestra cambiarEstado(String usuario, String id, CambioEstadoRequest req) {
        Usuario revisor = acceso.requerir(usuario, Rol.SUPERVISOR);
        Muestra m = buscar(id);
        if (!REVISABLES.contains(m.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La muestra está " + m.getEstado() + " y no se puede revisar");
        }
        if (!DECISIONES.contains(req.estado())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado no permitido: use OBSERVADO, VALIDADO o DESCARTADO");
        }
        String comentario = req.comentario() == null ? "" : req.comentario().trim();
        boolean exigeComentario = req.estado() != EstadoRevision.VALIDADO;
        if (exigeComentario && comentario.length() < MIN_COMENTARIO) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El comentario debe tener al menos " + MIN_COMENTARIO + " caracteres");
        }
        m.setEstado(req.estado());
        m.setComentarioSupervisor(comentario.isEmpty() ? null : comentario);
        m.setRevisadoPor(revisor.getNombreUsuario());
        return muestras.save(m);
    }

    @Transactional
    public void eliminar(String usuario, String id) {
        acceso.requerir(usuario, Rol.SUPERVISOR);
        Muestra m = buscar(id);
        fotos.deleteById(m.getId());
        muestras.delete(m);
    }

    @Transactional
    public void guardarFoto(String usuario, String id, byte[] datos) {
        acceso.requerir(usuario, Rol.OPERADOR);
        Muestra m = buscar(id);
        if (!esJpeg(datos)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La foto debe ser una imagen JPEG");
        }
        if (datos.length > MAX_FOTO_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La foto supera los 3 MB");
        }
        FotoMuestra foto = fotos.findById(id).orElseGet(() -> new FotoMuestra(id, datos));
        foto.setDatos(datos);
        fotos.save(foto);
        m.setTieneFoto(true);
        muestras.save(m);
    }

    @Transactional(readOnly = true)
    public byte[] obtenerFoto(String usuario, String id) {
        acceso.requerir(usuario);
        return fotos.findById(id)
                .map(FotoMuestra::getDatos)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La muestra " + id + " no tiene foto"));
    }

    private static boolean esJpeg(byte[] datos) {
        return datos != null && datos.length > 2 && (datos[0] & 0xFF) == 0xFF && (datos[1] & 0xFF) == 0xD8;
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen(String usuario) {
        acceso.requerir(usuario);
        List<Muestra> todas = muestras.findAll();
        Map<String, Long> porEstado = new TreeMap<>(todas.stream()
                .collect(Collectors.groupingBy(m -> m.getEstado().name(), Collectors.counting())));
        Map<String, Long> porCentro = new TreeMap<>(todas.stream()
                .collect(Collectors.groupingBy(Muestra::getCentro, Collectors.counting())));
        long individuos = todas.stream().mapToLong(Muestra::getConteo).sum();
        Map<String, Double> densidad = new TreeMap<>();
        todas.stream()
                .filter(m -> m.getDensidadPorMetro() != null)
                .collect(Collectors.groupingBy(Muestra::getCentro, Collectors.averagingDouble(Muestra::getDensidadPorMetro)))
                .forEach((centro, promedio) -> densidad.put(centro, Math.round(promedio * 10.0) / 10.0));
        return new ResumenResponse(todas.size(), individuos, porEstado, porCentro, densidad);
    }

    private Muestra buscar(String id) {
        return muestras.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la muestra " + id));
    }

    private static void copiarDatos(MuestraRequest req, Muestra m) {
        m.setTramoCm(req.tramoCm());
        m.setConteo(req.conteo());
        m.setConteoMarcado(req.conteoMarcado());
        m.setObservaciones(vacioANull(req.observaciones()));
        m.setCalibrePromedioMm(req.calibrePromedioMm());
        m.setPesoGramos(req.pesoGramos());
        m.setFotoNombre(vacioANull(req.fotoNombre()));
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
