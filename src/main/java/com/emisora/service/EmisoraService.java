package com.emisora.service;

import com.emisora.entity.Emisora;
import com.emisora.exception.BusinessRuleException;
import com.emisora.exception.DuplicateResourceException;
import com.emisora.exception.ResourceNotFoundException;
import com.emisora.repository.EmisoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de servicio para la entidad Emisora.
 * Implementa la lógica de negocio, validaciones de frecuencia y reportes parametrizados.
 *
 * @author Jose Antonio Quintero Herrera (7502510055)
 */
@Service
@Transactional
public class EmisoraService {

    public static final double FM_MIN = 87.0;
    public static final double FM_MAX = 108.0;
    public static final int AM_MIN = 530;
    public static final int AM_MAX = 1710;

    private final EmisoraRepository emisoraRepository;

    public EmisoraService(EmisoraRepository emisoraRepository) {
        this.emisoraRepository = emisoraRepository;
    }

    @Transactional(readOnly = true)
    public List<Emisora> listarTodas() {
        return emisoraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Emisora buscarPorId(Long id) {
        return emisoraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la emisora con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Emisora buscarPorCodigo(String codigo) {
        return emisoraRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la emisora con código: " + codigo));
    }

    @Transactional(readOnly = true)
    public List<Emisora> buscarPorTermino(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodas();
        }
        return emisoraRepository.buscarPorTermino(termino.trim());
    }

    public Emisora guardar(Emisora emisora) {
        validarReglasDeNegocio(emisora, null);
        return emisoraRepository.save(emisora);
    }

    public Emisora actualizar(Long id, Emisora datosActualizados) {
        Emisora existente = buscarPorId(id);
        validarReglasDeNegocio(datosActualizados, id);

        existente.setCodigo(datosActualizados.getCodigo());
        existente.setNombre(datosActualizados.getNombre());
        existente.setCanal(datosActualizados.getCanal());
        existente.setBandaFm(datosActualizados.getBandaFm());
        existente.setBandaAm(datosActualizados.getBandaAm());
        existente.setNumLocutores(datosActualizados.getNumLocutores());
        existente.setGenero(datosActualizados.getGenero());
        existente.setHorario(datosActualizados.getHorario());
        existente.setPatrocinador(datosActualizados.getPatrocinador());
        existente.setPais(datosActualizados.getPais());
        existente.setDescripcion(datosActualizados.getDescripcion());
        existente.setNumProgramas(datosActualizados.getNumProgramas());
        existente.setNumCiudades(datosActualizados.getNumCiudades());

        return emisoraRepository.save(existente);
    }

    public void eliminar(Long id) {
        Emisora emisora = buscarPorId(id);
        emisoraRepository.delete(emisora);
    }

    @Transactional(readOnly = true)
    public long contarTotal() {
        return emisoraRepository.count();
    }

    // ---------------------------------------------------------------------
    // REPORTES PARAMETRIZADOS
    // ---------------------------------------------------------------------

    /**
     * REPORTE 1: Emisoras por País y Género (opcional).
     */
    @Transactional(readOnly = true)
    public List<Emisora> reportePorPaisYGenero(String pais, String genero) {
        if (pais == null || pais.isBlank()) {
            throw new BusinessRuleException("Debe seleccionar un país para generar el reporte.");
        }
        return emisoraRepository.reportePorPaisYGenero(pais.trim(), (genero != null && !genero.isBlank()) ? genero.trim() : null);
    }

    /**
     * REPORTE 2: Emisoras por Cobertura (Rango de ciudades y mínimo de locutores).
     */
    @Transactional(readOnly = true)
    public List<Emisora> reportePorCobertura(int minCiudades, int maxCiudades, int minLocutores) {
        if (minCiudades < 0 || maxCiudades < 0 || minLocutores < 0) {
            throw new BusinessRuleException("Los valores de cobertura y locutores no pueden ser negativos.");
        }
        if (minCiudades > maxCiudades) {
            throw new BusinessRuleException("El mínimo de ciudades no puede ser mayor al máximo de ciudades.");
        }
        return emisoraRepository.reportePorCobertura(minCiudades, maxCiudades, minLocutores);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerPaises() {
        return emisoraRepository.findDistinctPaises();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerGeneros() {
        return emisoraRepository.findDistinctGeneros();
    }

    // ---------------------------------------------------------------------
    // VALIDACIONES DE NEGOCIO
    // ---------------------------------------------------------------------
    private void validarReglasDeNegocio(Emisora emisora, Long idExistente) {
        // Validar unicidad de código
        if (idExistente == null) {
            if (emisoraRepository.existsByCodigo(emisora.getCodigo())) {
                throw new DuplicateResourceException("Ya existe una emisora registrada con el código: " + emisora.getCodigo());
            }
            if (emisoraRepository.existsByNombre(emisora.getNombre())) {
                throw new DuplicateResourceException("Ya existe una emisora registrada con el nombre: " + emisora.getNombre());
            }
        } else {
            if (emisoraRepository.existsByCodigoAndIdNot(emisora.getCodigo(), idExistente)) {
                throw new DuplicateResourceException("El código " + emisora.getCodigo() + " ya está en uso por otra emisora.");
            }
            if (emisoraRepository.existsByNombreAndIdNot(emisora.getNombre(), idExistente)) {
                throw new DuplicateResourceException("El nombre '" + emisora.getNombre() + "' ya está en uso por otra emisora.");
            }
        }

        // Debe transmitir al menos en FM o en AM
        if (emisora.getBandaFm() == null && emisora.getBandaAm() == null) {
            throw new BusinessRuleException("La emisora debe tener asignada al menos una frecuencia de transmisión (FM o AM).");
        }

        // Validación rango FM
        if (emisora.getBandaFm() != null && (emisora.getBandaFm() < FM_MIN || emisora.getBandaFm() > FM_MAX)) {
            throw new BusinessRuleException("La frecuencia FM debe estar en el rango de " + FM_MIN + " a " + FM_MAX + " MHz.");
        }

        // Validación rango AM
        if (emisora.getBandaAm() != null && (emisora.getBandaAm() < AM_MIN || emisora.getBandaAm() > AM_MAX)) {
            throw new BusinessRuleException("La frecuencia AM debe estar en el rango de " + AM_MIN + " a " + AM_MAX + " kHz.");
        }
    }
}
