<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="isAdmin" value="${sessionScope.rol == 'Administrador'}" />
<c:set var="view" value="${requestScope.activeView}" />

<aside class="sidebar">
  <jsp:include page="/components/brand.jsp">
    <jsp:param name="variant" value="workspace"/>
  </jsp:include>

  <div class="sidebar-section-label">
    <c:choose>
      <c:when test="${isAdmin}">Administración</c:when>
      <c:otherwise>Área jurisdiccional</c:otherwise>
    </c:choose>
  </div>

  <nav class="sidebar-nav" aria-label="Navegación principal">
    <c:choose>
      <c:when test="${isAdmin}">
        <a href="${pageContext.request.contextPath}/admin/distritos"
           class="${view == 'districts' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m3 5 6-3 6 3 6-3v17l-6 3-6-3-6 3zM9 2v17M15 5v17"/>
          </svg>
          <span>Gestión de Distritos</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/salas"
           class="${view == 'rooms' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 21V6l8-4 8 4v15M2 21h20M8 9h2M14 9h2M8 13h2M14 13h2M10 21v-4h4v4"/>
          </svg>
          <span>Gestión de Salas</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/magistrados"
           class="${view == 'magistrates' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="9" cy="8" r="3"/>
            <path d="M3 20c.4-4 2.4-6 6-6s5.6 2 6 6M16 5a3 3 0 0 1 0 6M17 14c2.6.4 3.9 2.4 4 6"/>
          </svg>
          <span>Gestión de Magistrados</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/reasignacion"
           class="${view == 'reassignment' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M3 7h3c5 0 7 10 12 10h3M18 14l3 3-3 3M3 17h3c2 0 3.4-1.6 4.7-3.6M15 7h6M18 4l3 3-3 3"/>
          </svg>
          <span>Reasignación excepcional</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/identidad"
           class="${view == 'appearance' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 3a9 9 0 1 0 0 18h1.4a1.7 1.7 0 0 0 1.2-2.9 1.7 1.7 0 0 1 1.2-2.9H18a3 3 0 0 0 3-3A9 9 0 0 0 12 3Z"/>
            <circle cx="7.5" cy="11" r=".8"/><circle cx="10" cy="7" r=".8"/><circle cx="15" cy="7.5" r=".8"/>
          </svg>
          <span>Identidad visual</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/auditoria"
           class="${view == 'audit' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 3h12v18H6zM9 7h6M9 11h6M9 15h3"/>
            <path d="m14 16 1.5 1.5L19 14"/>
          </svg>
          <span>Auditoría</span>
        </a>
      </c:when>
      <c:otherwise>
        <a href="${pageContext.request.contextPath}/mis-tocas"
           class="${view == 'cases' or view == 'case-detail' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M3 6h7l2 2h9v11H3z"/><path d="M3 9h18"/>
          </svg>
          <span>Mis Tocas</span>
        </a>
        <a href="${pageContext.request.contextPath}/agenda"
           class="${view == 'agenda' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <rect x="3" y="7" width="18" height="13" rx="2"/>
            <path d="M8 7V4h8v3M3 12h18M10 12v2h4v-2"/>
          </svg>
          <span>Mi Agenda</span>
        </a>
        <a href="${pageContext.request.contextPath}/resoluciones"
           class="${view == 'resolutions' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 2h9l4 4v16H6z"/><path d="M14 2v5h5M9 12h6M9 16h6"/>
          </svg>
          <span>Resoluciones emitidas</span>
        </a>
        <a href="${pageContext.request.contextPath}/perfil"
           class="${view == 'profile' ? 'active' : ''}">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="8" r="4"/><path d="M4 21c.7-4.2 3.4-6 8-6s7.3 1.8 8 6"/>
          </svg>
          <span>Perfil</span>
        </a>
      </c:otherwise>
    </c:choose>
  </nav>

  <div class="sidebar-footer">
    <form action="${pageContext.request.contextPath}/logout" method="post"
          style="margin:0;padding:0;width:100%;">
      <input type="hidden" name="csrfToken"
             value="<c:out value='${requestScope.csrfToken}'/>">
      <button type="submit"
              style="display:flex;width:100%;align-items:center;gap:0.8rem;
                           border:0;background:transparent;color:inherit;cursor:pointer;
                           padding:0.75rem 0.8rem;font-size:0.72rem;">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M10 4H4v16h6M14 8l4 4-4 4M8 12h10"/>
        </svg>
        <span>Cerrar sesión</span>
      </button>
    </form>
    <p>SSI · Versión 1.1</p>
  </div>
</aside>