package com.imprenta.personas.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imprenta.personas.dto.ActualizarCreditoDTO;
import com.imprenta.personas.dto.AprobarCreditoRequest;
import com.imprenta.personas.model.Cliente;
import com.imprenta.personas.dto.ClienteUltimaCompraDTO;
import com.imprenta.personas.model.DocumentoCliente;
import com.imprenta.personas.service.ClienteService;
import com.imprenta.personas.service.DocumentoService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;
    private final DocumentoService documentoService;

    public ClienteController(ClienteService clienteService, DocumentoService documentoService) {
        this.clienteService = clienteService;
        this.documentoService = documentoService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> getAll() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> create(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.save(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Integer id, @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.update(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Cliente>> buscar(@RequestParam String cliente) {
        return ResponseEntity.ok(clienteService.buscarClientesPorTermino(cliente));
    }

    @PostMapping("/{id}/documentos")
    public ResponseEntity<?> subirDocumentos(
            @PathVariable Integer id, @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            DocumentoCliente docGuardado = documentoService.guardarDocumento(id, tipoDocumento, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(docGuardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/documentos")
    public ResponseEntity<List<DocumentoCliente>> listarDocumentos(@PathVariable Integer id) {
        List<DocumentoCliente> documentos = documentoService.listarDocumentosCliente(id);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{idCliente}/documentos/{idDocumento}/descargar")
    public ResponseEntity<?> descargarDocumento(
            @PathVariable Integer idCliente,
            @PathVariable Long idDocumento) {

        try {
            DocumentoCliente doc = documentoService.obtenerDocumentoPorId(idDocumento);

            // Seguridad: Validar que el documento realmente sea de este cliente
            if (doc.getIdCliente().intValue() != idCliente.intValue()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El documento no pertenece a este cliente.");
            }

            // Construir la ruta absoluta de forma segura
            String rutaLimpia = doc.getRutaArchivo().startsWith("/") ? doc.getRutaArchivo()
                    : "/" + doc.getRutaArchivo();
            File archivo = new File(rutaLimpia);

            // Si falla, en lugar de un 404 vacío, nos dirá EXACTAMENTE dónde buscó el
            // archivo
            if (!archivo.exists() || !archivo.canRead()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Archivo físico no encontrado o sin permisos. Ruta exacta buscada por Java: "
                                + archivo.getAbsolutePath());
            }

            Resource recurso = new FileSystemResource(archivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNombreArchivo() + "\"")
                    .body(recurso);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/aprobar-credito")
    public ResponseEntity<?> aprobarCredito(
            @PathVariable Integer id,
            @RequestBody AprobarCreditoRequest request,
            @RequestHeader(value = "X-Role", required = false) String rolUsuario) {

        if (rolUsuario == null
                || (!rolUsuario.equals("DUEÑO") && !rolUsuario.equals("ADMIN") && !rolUsuario.equals("CONTADORA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: El usuario no tiene permisos para aprobar créditos.");
        }

        try {
            Cliente clienteActualizado = clienteService.aprobarCredito(id, request);
            return ResponseEntity.ok(clienteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Cliente>> getByTipo(@RequestParam String tipo) {
        return ResponseEntity.ok(clienteService.buscarPorTipo(tipo));
    }

    @PutMapping("/{idCliente}/credito")
    public ResponseEntity<?> actualizarCredito(
            @PathVariable Integer idCliente,
            @RequestBody ActualizarCreditoDTO dto) {

        Cliente clienteActualizado = clienteService.actualizarCredito(idCliente, dto);
        return ResponseEntity.ok(clienteActualizado);
    }

    @GetMapping(params = "tipo")
    public ResponseEntity<List<Cliente>> listarCliente(
            @RequestParam(name = "tipo") String tipo) {

        List<Cliente> clientes = clienteService.obtenerClientePorTIpo(tipo);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}/ultima-compra")
    public ResponseEntity<ClienteUltimaCompraDTO> obtenerClienteConCompra(@PathVariable Integer id) {

        return clienteService.obtenerClienteConUltimaCompraPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
