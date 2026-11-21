package com.acuimuestra.backend.repository;

import com.acuimuestra.backend.model.EstadoRevision;
import com.acuimuestra.backend.model.Muestra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MuestraRepository extends JpaRepository<Muestra, String> {

    @Query("""
            select m from Muestra m
            where (:centro is null or m.centro = :centro)
              and (:estado is null or m.estado = :estado)
            order by m.fechaHora desc
            """)
    List<Muestra> filtrar(@Param("centro") String centro, @Param("estado") EstadoRevision estado);
}
