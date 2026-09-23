package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DtoTest {

    static class ElementoDePrueba extends ElementoConectable {
    }

    @Test
    void actividadRespuestaDTO_desde_conProceso_mapeaTodosLosCampos() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        Proceso proceso = new Proceso();
        proceso.setId(10L);

        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Radicar solicitud");
        actividad.setTipoActividad("Manual");
        actividad.setLaneId(2L);
        actividad.setPosicionX(5);
        actividad.setPosicionY(6);
        actividad.setProceso(proceso);

        ActividadRespuestaDTO dto = ActividadRespuestaDTO.desde(actividad);

        assertEquals(1L, dto.id());
        assertEquals("Radicar solicitud", dto.nombre());
        assertEquals("Manual", dto.tipoActividad());
        assertEquals(2L, dto.laneId());
        assertEquals(5, dto.posicionX());
        assertEquals(6, dto.posicionY());
        assertEquals(10L, dto.procesoId());
    }

    @Test
    void actividadRespuestaDTO_desde_sinProceso_procesoIdEsNulo() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Radicar solicitud");
        actividad.setTipoActividad("Manual");
        actividad.setLaneId(2L);
        actividad.setPosicionX(0);
        actividad.setPosicionY(0);

        ActividadRespuestaDTO dto = ActividadRespuestaDTO.desde(actividad);

        assertNull(dto.procesoId());
    }

    @Test
    void empresaRespuestaDTO_desde_mapeaTodosLosCampos() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Empresa Demo");
        empresa.setNit("900123456-7");
        empresa.setCorreo("contacto@demo.com");

        EmpresaRespuestaDTO dto = EmpresaRespuestaDTO.desde(empresa);

        assertEquals(1L, dto.id());
        assertEquals("Empresa Demo", dto.nombre());
        assertEquals("900123456-7", dto.nit());
        assertEquals("contacto@demo.com", dto.correo());
    }

    @Test
    void arcoRespuestaDTO_desde_conOrigenYDestino_mapeaTodosLosCampos() {
        Proceso proceso = new Proceso();
        proceso.setId(10L);

        ElementoDePrueba origen = new ElementoDePrueba();
        origen.setId(1L);
        origen.setNombre("Radicar");
        ElementoDePrueba destino = new ElementoDePrueba();
        destino.setId(2L);
        destino.setNombre("Revisar");

        Arco arco = new Arco();
        arco.setId(5L);
        arco.setNombre("flujo-1");
        arco.setCondicion("aprobado");
        arco.setOrigen(origen);
        arco.setDestino(destino);
        arco.setProceso(proceso);

        ArcoRespuestaDTO dto = ArcoRespuestaDTO.desde(arco);

        assertEquals(5L, dto.id());
        assertEquals("flujo-1", dto.nombre());
        assertEquals("aprobado", dto.condicion());
        assertEquals(1L, dto.origenId());
        assertEquals("Radicar", dto.origenNombre());
        assertEquals(2L, dto.destinoId());
        assertEquals("Revisar", dto.destinoNombre());
        assertEquals(10L, dto.procesoId());
    }

    @Test
    void arcoRespuestaDTO_desde_sinOrigenNiDestinoNiProceso_todoQuedaNulo() {
        Arco arco = new Arco();
        arco.setId(5L);
        arco.setNombre("flujo-suelto");

        ArcoRespuestaDTO dto = ArcoRespuestaDTO.desde(arco);

        assertNull(dto.origenId());
        assertNull(dto.origenNombre());
        assertNull(dto.destinoId());
        assertNull(dto.destinoNombre());
        assertNull(dto.procesoId());
    }

    @Test
    void elementoRespuestaDTO_desde_conProceso_mapeaTodosLosCampos() {
        Proceso proceso = new Proceso();
        proceso.setId(10L);

        ElementoDePrueba elemento = new ElementoDePrueba();
        elemento.setId(1L);
        elemento.setNombre("Radicar solicitud");
        elemento.setPosicionX(3);
        elemento.setPosicionY(4);
        elemento.setProceso(proceso);

        ElementoRespuestaDTO dto = ElementoRespuestaDTO.desde(elemento);

        assertEquals(1L, dto.id());
        assertEquals("Radicar solicitud", dto.nombre());
        assertEquals(3, dto.posicionX());
        assertEquals(4, dto.posicionY());
        assertEquals(10L, dto.procesoId());
    }

    @Test
    void elementoRespuestaDTO_desde_sinProceso_procesoIdEsNulo() {
        ElementoDePrueba elemento = new ElementoDePrueba();
        elemento.setId(1L);
        elemento.setNombre("Radicar solicitud");
        elemento.setPosicionX(0);
        elemento.setPosicionY(0);

        ElementoRespuestaDTO dto = ElementoRespuestaDTO.desde(elemento);

        assertNull(dto.procesoId());
    }

    @Test
    void gatewayRespuestaDTO_desde_conProceso_mapeaTodosLosCampos() {
        Proceso proceso = new Proceso();
        proceso.setId(10L);

        Gateway gateway = new Gateway();
        gateway.setId(1L);
        gateway.setNombre("Decision 1");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        gateway.setProceso(proceso);

        GatewayRespuestaDTO dto = GatewayRespuestaDTO.desde(gateway);

        assertEquals(1L, dto.id());
        assertEquals("Decision 1", dto.nombre());
        assertEquals(Gateway.TipoGateway.EXCLUSIVO, dto.tipo());
        assertEquals(10L, dto.procesoId());
    }

    @Test
    void gatewayRespuestaDTO_desde_sinProceso_procesoIdEsNulo() {
        Gateway gateway = new Gateway();
        gateway.setId(1L);
        gateway.setNombre("Decision 1");
        gateway.setTipo(Gateway.TipoGateway.PARALELO);

        GatewayRespuestaDTO dto = GatewayRespuestaDTO.desde(gateway);

        assertNull(dto.procesoId());
    }

    @Test
    void procesoRespuestaDTO_desde_conEmpresa_mapeaTodosLosCampos() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Proceso proceso = new Proceso();
        proceso.setId(5L);
        proceso.setNombre("Solicitud de vacaciones");
        proceso.setDescripcion("Proceso de RRHH");
        proceso.setCategoria("RRHH");
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);
        proceso.setEmpresa(empresa);

        ProcesoRespuestaDTO dto = ProcesoRespuestaDTO.desde(proceso);

        assertEquals(5L, dto.id());
        assertEquals("Solicitud de vacaciones", dto.nombre());
        assertEquals("Proceso de RRHH", dto.descripcion());
        assertEquals("RRHH", dto.categoria());
        assertEquals(Proceso.EstadoProceso.BORRADOR, dto.estado());
        assertEquals(1L, dto.empresaId());
    }

    @Test
    void procesoRespuestaDTO_desde_sinEmpresa_empresaIdEsNulo() {
        Proceso proceso = new Proceso();
        proceso.setId(5L);
        proceso.setNombre("Solicitud de vacaciones");
        proceso.setDescripcion("Proceso de RRHH");
        proceso.setCategoria("RRHH");
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        ProcesoRespuestaDTO dto = ProcesoRespuestaDTO.desde(proceso);

        assertNull(dto.empresaId());
    }
}