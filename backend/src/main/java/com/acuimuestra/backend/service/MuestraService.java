package com.acuimuestra.backend.service;

import com.acuimuestra.backend.dto.CambioEstadoRequest;
import com.acuimuestra.backend.dto.MuestraRequest;
import com.acuimuestra.backend.dto.ResumenResponse;
import com.acuimuestra.backend.model.EstadoRevision;
import com.acuimuestra.backend.model.Muestra;
import com.acuimuestra.backend.model.Rol;
import com.acuimuestra.backend.model.Usuario;
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

    private static final EnumSet<EstadoRevision> REVISABLES = EnumSet.of(EstadoRevision.PENDIENTE, EstadoRevision.CORREGIDO);
    private static final EnumSet<EstadoRevision> DECISIONES = EnumSet.of(
            EstadoRevision.OBSERVADO, EstadoRevision.VALIDADO, EstadoRevision.DESCARTADO);

    public record Creacion(Muestra muestra, boolean nueva) {}

    private final MuestraRepository muestras;
    private final AccesoService acceso;

    public MuestraService(MuestraRepository muestras, AccesoService acceso) {
        this.muestras = muestras;
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
        acceso.requerir(usuario, Rol.SUPERVISOR);
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
        return muestras.save(m);
    }

    @Transactional
    public void eliminar(String usuario, String id) {
        acceso.requerir(usuario, Rol.SUPERVISOR);
        muestras.delete(buscar(id));
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
        return new ResumenResponse(todas.size(), individuos, porEstado, porCentro);
    }

    private Muestra buscar(String id) {
        return muestras.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la muestra " + id));
    }

    private static void copiarDatos(MuestraRequest req, Muestra m) {
        m.setTramoCm(req.tramoCm());
        m.setConteo(req.conteo());
        m.setObservaciones(vacioANull(req.observaciones()));
        m.setCalibrePromedioMm(req.calibrePromedioMm());
        m.setPesoGramos(req.pesoGramos());
        m.setFotoNombre(vacioANull(req.fotoNombre()));
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
