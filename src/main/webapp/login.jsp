<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Iniciar sesión · SSI</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ssi.css">
</head>
<body>

<main class="login-page">
  <div class="ambient ambient--top"></div>
  <div class="ambient ambient--bottom"></div>

  <section class="login-card" aria-labelledby="login-title">
    <aside class="institution-panel">
      <jsp:include page="/components/brand.jsp">
        <jsp:param name="variant" value="lockup"/>
        <jsp:param name="compact" value="false"/>
      </jsp:include>

      <div class="institution-message">
        <p class="eyebrow">Sistema institucional</p>
        <h1>Gestión de Tocas de Segunda Instancia</h1>
        <p>Acceso seguro para la administración y seguimiento de asuntos jurisdiccionales.</p>
      </div>

      <div class="security-note">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 3 5 6v5c0 4.6 2.8 8.1 7 10 4.2-1.9 7-5.4 7-10V6l-7-3Z"/>
          <path d="m9 12 2 2 4-4"/>
        </svg>
        <span>Conexión institucional protegida</span>
      </div>
    </aside>

    <div class="form-panel">
      <div class="mobile-brand">
        <jsp:include page="/components/brand.jsp">
          <jsp:param name="variant" value="lockup"/>
          <jsp:param name="compact" value="true"/>
        </jsp:include>
      </div>

      <header class="form-heading">
        <p class="eyebrow">Bienvenido de nuevo</p>
        <h2 id="login-title">Iniciar sesión</h2>
        <p>Ingresa tus credenciales para continuar al sistema.</p>
      </header>

      <c:if test="${not empty error}">
        <div style="margin-bottom:1rem;padding:0.75rem 1rem;border-radius:0.5rem;
                            background:#f8e9ee;color:#a64f64;font-size:0.75rem;">
          <c:out value="${error}"/>
        </div>
      </c:if>

      <form id="loginForm"
            action="${pageContext.request.contextPath}/login"
            method="post"
            novalidate>

        <input type="hidden" name="csrfToken" id="loginCsrfToken"
               value="<c:out value='${requestScope.csrfToken}'/>">

        <div class="field">
          <label for="email">Correo electrónico</label>
          <div class="input-shell">
            <input type="email" id="email" name="email"
                   placeholder="nombre@tsjmorelos.gob.mx"
                   autocomplete="email"
                   value="${emailIngresado}"
                   required>
          </div>
        </div>

        <div class="field">
          <label for="password">Contraseña</label>
          <div class="input-shell">
            <input type="password" id="password" name="password"
                   placeholder="Ingresa tu contraseña"
                   autocomplete="current-password"
                   required>
            <button class="visibility-button" type="button"
                    id="togglePassword" aria-label="Mostrar contraseña">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M3 12s3.7-5 9-5 9 5 9 5-3.7 5-9 5-9-5-9-5Z"/>
                <circle cx="12" cy="12" r="2.6"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember">
            <input type="checkbox" name="remember">
            <span>Recordar sesión</span>
          </label>
          <button class="text-button" type="button" id="btnRecovery">
            Recuperar contraseña
          </button>
        </div>

        <button class="primary-button" type="submit" id="btnLogin">
          <span id="btnLoginLabel">Ingresar</span>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M5 12h14m-5-5 5 5-5 5"/>
          </svg>
        </button>

        <div id="submittedMsg"
             class="status-message"
             role="status"
             style="display:none;">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="m7 12 3 3 7-7"/>
            <circle cx="12" cy="12" r="9"/>
          </svg>
          <span>Credenciales recibidas. Validando acceso…</span>
        </div>

        <div id="recoveryMsg"
             class="status-message"
             role="status"
             style="display:none;">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 6h16v12H4z"/>
            <path d="m4 7 8 6 8-6"/>
          </svg>
          <span>Si el correo está registrado y activo, se enviará un enlace de recuperación.</span>
        </div>
      </form>

      <p class="support">
        ¿Necesitas ayuda?
        <button type="button">Contacta a soporte técnico</button>
      </p>
    </div>
  </section>

  <footer class="page-footer">
    <span>© 2025 Poder Judicial del Estado de Morelos</span>
    <span class="footer-divider" aria-hidden="true"></span>
    <button type="button">Aviso de privacidad</button>
  </footer>
</main>

<script src="${pageContext.request.contextPath}/assets/js/ssi-login.js"></script>
</body>
</html>