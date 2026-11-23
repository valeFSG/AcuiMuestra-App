package com.acuimuestra.backend.config;

import com.acuimuestra.backend.model.EstadoRevision;
import com.acuimuestra.backend.model.Muestra;
import com.acuimuestra.backend.model.Rol;
import com.acuimuestra.backend.model.Usuario;
import com.acuimuestra.backend.repository.MuestraRepository;
import com.acuimuestra.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DatosIniciales {

    private static final long DIA = 86_400_000L;

    @Bean
    CommandLineRunner cargarDatos(UsuarioRepository usuarios, MuestraRepository muestras) {
        return args -> {
            List.of(
                    new Usuario("operador", "Camila Rojas", Rol.OPERADOR),
                    new Usuario("operador2", "Diego Muñoz", Rol.OPERADOR),
                    new Usuario("supervisor", "Andrea Soto", Rol.SUPERVISOR),
                    new Usuario("analista", "Felipe Vargas", Rol.ANALISTA)
            ).forEach(base -> usuarios.findByNombreUsuario(base.getNombreUsuario()).ifPresentOrElse(
                    existente -> {
                        existente.actualizar(base.getNombre(), base.getRol());
                        usuarios.save(existente);
                    },
                    () -> usuarios.save(base)));
            if (muestras.count() == 0) {
                long ahora = System.currentTimeMillis();
                muestras.saveAll(List.of(
                        muestra("demo-0001", "Centro Ficticio Norte", "Tren 1", "Línea 1", 30, 180, ahora - 3 * DIA, EstadoRevision.VALIDADO),
                        muestra("demo-0002", "Centro Ficticio Norte", "Tren 2", "Línea 1", 30, 165, ahora - 2 * DIA, EstadoRevision.PENDIENTE),
                        muestra("demo-0003", "Centro Ficticio Sur", "Tren 1", "Línea 2", 25, 140, ahora - DIA, EstadoRevision.OBSERVADO)));
            }
        };
    }

    private static Muestra muestra(String id, String centro, String tren, String linea,
                                   int tramo, int conteo, long fecha, EstadoRevision estado) {
        Muestra m = new Muestra(id);
        m.setCentro(centro);
        m.setTren(tren);
        m.setLinea(linea);
        m.setTramoCm(tramo);
        m.setConteo(conteo);
        m.setFechaHora(fecha);
        m.setOperador("operador");
        m.setEstado(estado);
        if (estado == EstadoRevision.OBSERVADO) m.setComentarioSupervisor("Repetir el conteo del tramo");
        return m;
    }
}
