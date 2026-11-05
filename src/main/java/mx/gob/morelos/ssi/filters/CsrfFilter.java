package mx.gob.morelos.ssi.filters;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@WebFilter("/*")
public class CsrfFilter implements Filter {

    private static final String REQUEST_ATTRIBUTE = "csrfToken";
    private static final String COOKIE_NAME = "XSRF-TOKEN";
    private static final String HEADER_NAME = "X-CSRF-Token";
    private static final String PARAMETER_NAME = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Gson gson;

    @Override
    public void init(FilterConfig filterConfig) {
        gson = new Gson();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri != null && contextPath != null && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length()) : uri;

        // ¡ESCUDO PARA LA API!
        if (path != null && path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        String cookieToken = getCookieToken(request);

        if (!isUnsafeMethod(request.getMethod())) {
            if (!isValidTokenFormat(cookieToken)) {
                cookieToken = generateToken();
                writeCookie(request, response, cookieToken);
            }
            request.setAttribute(REQUEST_ATTRIBUTE, cookieToken);
            chain.doFilter(req, res);
            return;
        }

        if (!isValidTokenFormat(cookieToken)) {
            handleInvalidToken(request, response);
            return;
        }

        String received = getReceivedToken(request);
        if (!isValidTokenFormat(received) || !constantTimeEquals(cookieToken, received)) {
            handleInvalidToken(request, response);
            return;
        }

        request.setAttribute(REQUEST_ATTRIBUTE, cookieToken);
        chain.doFilter(req, res);
    }

    private boolean isUnsafeMethod(String method) {
        if (method == null) return false;
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    private String getReceivedToken(HttpServletRequest request) {
        String t = request.getHeader(HEADER_NAME);
        if (t == null || t.isBlank()) t = request.getParameter(PARAMETER_NAME);
        return t == null ? "" : t.trim();
    }

    private String getCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return "";
        for (Cookie c : cookies) {
            if (c != null && COOKIE_NAME.equals(c.getName())) {
                String v = c.getValue();
                if (v != null && !v.isBlank()) return v.trim();
            }
        }
        return "";
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isValidTokenFormat(String token) {
        if (token == null || token.isBlank()) return false;
        if (token.length() < 40 || token.length() > 64) return false;
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            boolean ok = (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9') || ch == '-' || ch == '_';
            if (!ok) return false;
        }
        return true;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

    private void writeCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        String path = request.getContextPath();
        if (path == null || path.isBlank()) path = "/";

        StringBuilder sb = new StringBuilder();
        sb.append(COOKIE_NAME).append("=").append(token)
                .append("; Path=").append(path)
                .append("; SameSite=Strict");

        if (request.isSecure()) sb.append("; Secure");
        response.addHeader("Set-Cookie", sb.toString());
    }

    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (isAjaxRequest(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("success", false);
            m.put("type", "error");
            m.put("message", "La solicitud de seguridad no es válida. Recarga la página.");
            response.getWriter().write(gson.toJson(m));
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "La solicitud de seguridad no es válida. Recarga la página.");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) return true;
        String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json");
    }

    @Override
    public void destroy() { gson = null; }
}