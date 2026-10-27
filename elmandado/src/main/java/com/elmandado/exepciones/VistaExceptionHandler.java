package com.elmandado.exepciones;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.elmandado.controlador.PedidoViewController;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(assignableTypes = PedidoViewController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VistaExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ModelAndView handleValidacionFormulario(BindException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("pedidos/formulario");
        mav.addAllObjects(ex.getBindingResult().getModel());

        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        boolean esEdicion = variables != null && variables.containsKey("id");
        mav.addObject("esEdicion", esEdicion);
        if (esEdicion) {
            mav.addObject("idPedido", Long.valueOf(variables.get("id")));
        }

        return mav;
    }
}