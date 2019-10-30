package pw.react.backend.reactbackend.security;

import javax.servlet.*;
import java.io.IOException;

public class CustomFilter extends GenericFilter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
