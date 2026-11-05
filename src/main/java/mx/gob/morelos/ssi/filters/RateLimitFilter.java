package mx.gob.morelos.ssi.filters;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@WebFilter("/*")
public class RateLimitFilter implements Filter {

    private static final String LOGIN_PATH = "/login";
    private static final String RECOVERY_PATH = "/verify-email";

    private static final int LOGIN_MAX_REQUESTS = 20;
    private static final long LOGIN_WINDOW_MILLIS = 5 * 60 * 1000L;
    private static final int RECOVERY_MAX_REQUESTS = 5;
    private static final long RECOVERY_WINDOW_MILLIS = 15 * 60 * 1000L;

    private static final long CLEANUP_INTERVAL_MILLIS = 5 * 60 * 1000L;
    private static final long ENTRY_MAX_IDLE_MILLIS = 30 * 60 * 1000L;

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-CSRF-Token";
    private static final String CSRF_PARAMETER_NAME = "csrfToken";

    private static final ConcurrentHashMap<String, RequestBucket> BUCKETS = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_CLEANUP = new AtomicLong(System.currentTimeMillis());

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

        String path = getRequestPath(request);

        if (!shouldLimit(request, path)) {
            chain.doFilter(req, res);
            return;
        }

        if (!hasValidCsrfToken(request)) {
            chain.doFilter(req, res);
            return;
        }

        cleanupIfNecessary();

        RateLimitPolicy policy = getPolicy(path);
        if (policy == null) {
            chain.doFilter(req, res);
            return;
        }

        String clientIp = getClientIp(request);
        String key = path + "|" + clientIp;
        long now = System.currentTimeMillis();

        RequestBucket bucket = BUCKETS.computeIfAbsent(key, k -> new RequestBucket());
        RateLimitResult result = bucket.register(now, policy.maxRequests(), policy.windowMillis());

        if (!result.allowed()) {
            handleRateLimitExceeded(request, response, path, result.retryAfterSeconds());
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(policy.maxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        chain.doFilter(req, res);
    }

    private boolean shouldLimit(HttpServletRequest request, String path) {
        if (request == null || !"POST".equalsIgnoreCase(request.getMethod())) return false;
        return LOGIN_PATH.equals(path) || RECOVERY_PATH.equals(path);
    }

    private RateLimitPolicy getPolicy(String path) {
        if (LOGIN_PATH.equals(path)) return new RateLimitPolicy(LOGIN_MAX_REQUESTS, LOGIN_WINDOW_MILLIS);
        if (RECOVERY_PATH.equals(path)) return new RateLimitPolicy(RECOVERY_MAX_REQUESTS, RECOVERY_WINDOW_MILLIS);
        return null;
    }

    private String getRequestPath(HttpServletRequest request) {
        if (request == null) return "";
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();
        if (uri == null || uri.isBlank()) return "";
        if (ctx == null || ctx.isBlank()) return uri;
        return uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
    }

    private boolean hasValidCsrfToken(HttpServletRequest request) {
        String cookie = getCsrfCookieToken(request);
        if (!isValidCsrfTokenFormat(cookie)) return false;
        String received = getReceivedCsrfToken(request);
        if (!isValidCsrfTokenFormat(received)) return false;
        return constantTimeEquals(cookie, received);
    }

    private String getCsrfCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return "";
        for (Cookie c : cookies) {
            if (c != null && CSRF_COOKIE_NAME.equals(c.getName())) {
                String v = c.getValue();
                if (v != null && !v.isBlank()) return v.trim();
            }
        }
        return "";
    }

    private String getReceivedCsrfToken(HttpServletRequest request) {
        String t = request.getHeader(CSRF_HEADER_NAME);
        if (t == null || t.isBlank()) t = request.getParameter(CSRF_PARAMETER_NAME);
        return t == null ? "" : t.trim();
    }

    private boolean isValidCsrfTokenFormat(String token) {
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

    private String getClientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        return (remote == null || remote.isBlank()) ? "unknown" : remote.trim();
    }

    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response,
                                         String path, long retryAfterSeconds) throws IOException, ServletException {
        disableCache(response);
        long safe = Math.max(1, retryAfterSeconds);

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(safe));

        if (isAjaxRequest(request)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("success", false);
            m.put("type", "warning");
            m.put("message", buildRateLimitMessage(path, safe));
            m.put("retryAfterSeconds", safe);
            response.getWriter().write(gson.toJson(m));
            return;
        }

        request.setAttribute("error", buildRateLimitMessage(path, safe));

        if (LOGIN_PATH.equals(path)) {
            request.setAttribute("emailIngresado", normalizeText(request.getParameter("email")));
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        if (RECOVERY_PATH.equals(path)) {
            request.getRequestDispatcher("/verify-email.jsp").forward(request, response);
            return;
        }
        response.sendError(429, "Se realizaron demasiadas solicitudes.");
    }

    private String buildRateLimitMessage(String path, long retryAfterSeconds) {
        long minutes = Math.max(1, (long) Math.ceil(retryAfterSeconds / 60.0));
        if (LOGIN_PATH.equals(path)) {
            return "Se realizaron demasiados intentos de inicio de sesión desde esta conexión. " +
                    "Intenta nuevamente en aproximadamente " + minutes +
                    (minutes == 1 ? " minuto." : " minutos.");
        }
        return "Se realizaron demasiadas solicitudes de recuperación desde esta conexión. " +
                "Intenta nuevamente en aproximadamente " + minutes +
                (minutes == 1 ? " minuto." : " minutos.");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) return true;
        String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json");
    }

    private void cleanupIfNecessary() {
        long now = System.currentTimeMillis();
        long prev = LAST_CLEANUP.get();
        if (now - prev < CLEANUP_INTERVAL_MILLIS) return;
        if (!LAST_CLEANUP.compareAndSet(prev, now)) return;
        BUCKETS.entrySet().removeIf(e -> e.getValue().isExpired(now, ENTRY_MAX_IDLE_MILLIS));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private void disableCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    @Override
    public void destroy() {
        BUCKETS.clear();
        gson = null;
    }

    private record RateLimitPolicy(int maxRequests, long windowMillis) {}
    private record RateLimitResult(boolean allowed, int remaining, long retryAfterSeconds) {}

    private static final class RequestBucket {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private long lastAccess = System.currentTimeMillis();

        private synchronized RateLimitResult register(long now, int maxRequests, long windowMillis) {
            lastAccess = now;
            long minimum = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= minimum) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxRequests) {
                Long oldest = timestamps.peekFirst();
                long retryAfterMs = oldest == null ? windowMillis : oldest + windowMillis - now;
                long retryAfterSec = Math.max(1, (long) Math.ceil(retryAfterMs / 1000.0));
                return new RateLimitResult(false, 0, retryAfterSec);
            }
            timestamps.addLast(now);
            int remaining = Math.max(0, maxRequests - timestamps.size());
            return new RateLimitResult(true, remaining, 0);
        }

        private synchronized boolean isExpired(long now, long maxIdleMillis) {
            return now - lastAccess > maxIdleMillis;
        }
    }
}