package br.ufpr.pid.hc.config;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.omnifaces.filter.HttpFilter;
import org.omnifaces.util.Servlets;

import java.io.IOException;
import java.security.Principal;

@WebFilter("/pages/*")
public class UsuarioAtivoFilter extends HttpFilter {

    @Inject
    private UsuarioService usuarioService;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            FilterChain chain) throws ServletException, IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null && !valido(principal.getName(), request)) {
            request.logout();
            if (session != null) {
                session.invalidate();
            }
            Servlets.facesRedirect(request, response, request.getContextPath() + "/pages/auth/login");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean valido(String email, HttpServletRequest request) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return usuario != null && !usuario.isInativo() && request.isUserInRole(usuario.getPerfil().name());
    }
}
