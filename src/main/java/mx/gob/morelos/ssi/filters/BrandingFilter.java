package mx.gob.morelos.ssi.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import mx.gob.morelos.ssi.models.branding.Branding;
import mx.gob.morelos.ssi.models.branding.BrandingDao;

import java.io.IOException;

@WebFilter("/*")
public class BrandingFilter implements Filter {

    private BrandingDao brandingDao;
    private static volatile Branding cached;
    private static volatile long cachedAt;

    @Override
    public void init(FilterConfig filterConfig) {
        brandingDao = new BrandingDao();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        Branding branding = cached;
        long now = System.currentTimeMillis();

        if (branding == null || now - cachedAt > 60_000L) {
            synchronized (BrandingFilter.class) {
                if (cached == null || now - cachedAt > 60_000L) {
                    try {
                        cached = brandingDao.get();
                        cachedAt = now;
                    } catch (RuntimeException ex) {
                        request.getServletContext().log("No se pudo cargar el branding.", ex);
                        cached = Branding.defaults();
                    }
                }
                branding = cached;
            }
        }

        request.setAttribute("branding", branding);
        chain.doFilter(req, res);
    }

    /** Llamar al guardar cambios para invalidar el cache. */
    public static void invalidate() {
        cached = null;
        cachedAt = 0;
    }

    @Override
    public void destroy() { brandingDao = null; }
}