package com.elmandado.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;
import com.elmandado.servicio.PedidoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/pedidos")
public class PedidoViewController {

    private final PedidoService servicio;

    public PedidoViewController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", servicio.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("nuevoPedido", new CrearPedidoDTO("", "", null));
        model.addAttribute("esEdicion", false);
        return "pedidos/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto) {
        servicio.crear(dto);
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        PedidoDTO pedido = servicio.obtenerPorId(id);

        model.addAttribute("nuevoPedido",
                new CrearPedidoDTO(pedido.cliente(), pedido.plato(), pedido.precio()));
        model.addAttribute("idPedido", id);
        model.addAttribute("esEdicion", true);
        return "pedidos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
            @Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto) {
        servicio.actualizar(id, dto);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/entregar")
    public String marcarEntregado(@PathVariable Long id) {
        servicio.marcarEntregado(id);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return "redirect:/pedidos";
    }
}