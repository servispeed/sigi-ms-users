package com.imprenta.personas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imprenta.personas.model.Cliente;
import com.imprenta.personas.repository.ClienteRepository;
import com.imprenta.personas.dto.ActualizarCreditoDTO;
import com.imprenta.personas.dto.AprobarCreditoRequest;
import com.imprenta.personas.dto.ClienteUltimaCompraDTO;
import com.imprenta.personas.exception.*;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Cliente findById(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID" + id + " no encontrado"));
    }

    public Cliente save(Cliente cliente) {

        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (cliente.getRfc() != null && !cliente.getRfc().isBlank()) {
            if (clienteRepository.existsByRfc(cliente.getRfc())) {
                throw new RuntimeException("El RFC ya está registrado");
            }
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente update(Integer id, Cliente datosActualizados) {

        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID " + id + " no encontrado"));

        if (datosActualizados.getEmail() != null && !datosActualizados.getEmail().isBlank()) {
            String nuevoEmail = datosActualizados.getEmail().trim();

            if (clienteRepository.existsByEmailAndIdClienteNot(nuevoEmail, id)) {
                throw new BadRequestException("El email ya está en uso por otro cliente");
            }
            clienteExistente.setEmail(nuevoEmail);
        }

        if (datosActualizados.getRfc() != null && !datosActualizados.getRfc().isBlank()) {
            String nuevoRfc = datosActualizados.getRfc().trim().toUpperCase();

            if (clienteRepository.existsByRfcAndIdClienteNot(nuevoRfc, id)) {
                throw new BadRequestException("El RFC ya está registrado por otro cliente");
            }
            clienteExistente.setRfc(nuevoRfc);
        }

        clienteExistente.setNombre(datosActualizados.getNombre());
        clienteExistente.setTelefono(datosActualizados.getTelefono());
        clienteExistente.setRazonSocial(datosActualizados.getRazonSocial());
        clienteExistente.setDireccionFiscal(datosActualizados.getDireccionFiscal());
        clienteExistente.setIdUsoCfdi(datosActualizados.getIdUsoCfdi());
        clienteExistente.setIdRegimenFiscal(datosActualizados.getIdRegimenFiscal());

        return clienteRepository.save(clienteExistente);
    }

    public void delete(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, usuario con ID " + id + " no existe");
        }
        clienteRepository.deleteById(id);
    }

    public List<Cliente> buscarClientesPorTermino(String terminoBusqueda) {
        return clienteRepository.findByNombreContainingIgnoreCaseOrRazonSocialContainingIgnoreCase(terminoBusqueda,
                terminoBusqueda);
    }

    @Transactional
    public Cliente aprobarCredito(Integer idCliente, AprobarCreditoRequest request) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));

        if (cliente.getRfc() == null || cliente.getRfc().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "No está permitido asignar crédito a clientes de mostrador (sin RFC registrado).");
        }

        cliente.setCreditoHabilitado(true);
        cliente.setDiasCredito(request.getDiasCredito());
        cliente.setLimiteCredito(request.getLimiteCredito());
        cliente.setEstatusCredito("AL_CORRIENTE");

        return clienteRepository.save(cliente);
    }

    public List<Cliente> buscarPorTipo(String tipo) {
        if ("mostrador".equalsIgnoreCase(tipo)) {
            return clienteRepository.findByRfcIsNull();
        } else if ("factura".equalsIgnoreCase(tipo)) {
            return clienteRepository.findByRfcIsNotNull();
        }

        return java.util.Collections.emptyList();
    }

    @Transactional
    public Cliente actualizarCredito(Integer idCliente, ActualizarCreditoDTO dto) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));

        // Actualizar solo los campos financieros
        if (dto.getLimiteCredito() != null)
            cliente.setLimiteCredito(dto.getLimiteCredito());
        if (dto.getDiasCredito() != null)
            cliente.setDiasCredito(dto.getDiasCredito());
        if (dto.getCreditoHabilitado() != null)
            cliente.setCreditoHabilitado(dto.getCreditoHabilitado());
        if (dto.getEstatusCredito() != null)
            cliente.setEstatusCredito(dto.getEstatusCredito());

        return clienteRepository.save(cliente);
    }

    public List<Cliente> obtenerClientePorTIpo(String tipo) {
        if ("factura".equalsIgnoreCase(tipo)) {
            return clienteRepository.findClientesFactura();
        } else if ("mostrador".equalsIgnoreCase(tipo)) {
            return clienteRepository.findClientesMostrador();
        }
        return clienteRepository.findAll();
    }

    public Optional<ClienteUltimaCompraDTO> obtenerClienteConUltimaCompraPorId(Integer id) {
        return clienteRepository.findByIdConUltimaCompra(id);
    }
}
