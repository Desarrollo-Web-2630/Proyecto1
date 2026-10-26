package com.proyecto1.thymeleaf.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Lane;
import com.proyecto1.thymeleaf.model.Mensaje;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.RolProceso;

class RespuestasModeladoDtoTest {

    @Test
    void poolRespuesta_copiaLosCampos() {
        Proceso proceso = new Proceso();
        proceso.setId(3L);
        Pool pool = new Pool();
        pool.setId(1L);
        pool.setNombre("Empresa");
        pool.setTipo(Pool.TipoPool.CLIENTE);
        pool.setProceso(proceso);

        PoolRespuestaDTO dto = PoolRespuestaDTO.desde(pool);

        assertEquals(1L, dto.id());
        assertEquals(Pool.TipoPool.CLIENTE, dto.tipo());
        assertEquals(3L, dto.procesoId());
    }

    @Test
    void poolRespuesta_sinProceso_dejaProcesoIdNulo() {
        assertNull(PoolRespuestaDTO.desde(new Pool()).procesoId());
    }

    @Test
    void laneRespuesta_copiaLosCampos() {
        Pool pool = new Pool();
        pool.setId(2L);
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Auditor");
        Lane lane = new Lane();
        lane.setId(1L);
        lane.setNombre("Auditoria");
        lane.setOrden(1);
        lane.setPool(pool);
        lane.setRolProceso(rol);

        LaneRespuestaDTO dto = LaneRespuestaDTO.desde(lane);

        assertEquals(2L, dto.poolId());
        assertEquals(5L, dto.rolProcesoId());
        assertEquals("Auditor", dto.rolProcesoNombre());
    }

    @Test
    void laneRespuesta_sinPoolNiRol_dejaRelacionesNulas() {
        LaneRespuestaDTO dto = LaneRespuestaDTO.desde(new Lane());

        assertNull(dto.poolId());
        assertNull(dto.rolProcesoId());
        assertNull(dto.rolProcesoNombre());
    }

    @Test
    void mensajeRespuesta_copiaLosCampos() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        Pool origen = new Pool();
        origen.setId(2L);
        Pool destino = new Pool();
        destino.setId(3L);
        Actividad actividad = new Actividad();
        actividad.setId(4L);
        Mensaje mensaje = new Mensaje();
        mensaje.setId(9L);
        mensaje.setNombre("Notificar");
        mensaje.setClaveCorrelacion("guia");
        mensaje.setProceso(proceso);
        mensaje.setPoolOrigen(origen);
        mensaje.setPoolDestino(destino);
        mensaje.setElementoOrigen(actividad);

        MensajeRespuestaDTO dto = MensajeRespuestaDTO.desde(mensaje);

        assertEquals(1L, dto.procesoId());
        assertEquals(2L, dto.poolOrigenId());
        assertEquals(3L, dto.poolDestinoId());
        assertEquals(4L, dto.elementoOrigenId());
    }

    @Test
    void mensajeRespuesta_sinRelaciones_dejaIdsNulos() {
        MensajeRespuestaDTO dto = MensajeRespuestaDTO.desde(new Mensaje());

        assertNull(dto.procesoId());
        assertNull(dto.poolOrigenId());
        assertNull(dto.poolDestinoId());
        assertNull(dto.elementoOrigenId());
    }
}
