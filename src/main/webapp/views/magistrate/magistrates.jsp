<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Gestión de Magistrados · SSI</title>
  <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
  <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
  <jsp:include page="/components/sidebar.jsp"/>

  <div class="workspace">
    <jsp:include page="/components/topbar.jsp">
      <jsp:param name="pageName" value="Gestión de Magistrados"/>
    </jsp:include>

    <main class="main-content">
      <div class="page-heading">
        <div>
          <p class="section-kicker">Administración institucional</p>
          <h1>Gestión de Magistrados</h1>
          <p>Alta, actualización, asignación uno a uno con una sala y desactivación controlada.</p>
        </div>
        <button class="primary-compact" type="button" data-modal-open="modalMagistrado">
          <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Registrar Magistrado
        </button>
      </div>

      <section class="data-card">
        <div class="card-toolbar">
          <div>
            <h2>Magistrados registrados</h2>
            <p>Un magistrado solo puede estar asociado a una sala activa</p>
          </div>
          <label class="search-box">
            <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
            </svg>
            <input type="search" id="searchMagistrates" placeholder="Buscar magistrado…">
          </label>
        </div>

        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th>Magistrado</th>
              <th>Correo institucional</th>
              <th>Sala asignada</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
              <c:when test="${not empty magistrates}">
                <c:forEach var="m" items="${magistrates}">
                  <tr class="mag-row"
                      data-search="${fn:toLowerCase(m.fullName)} ${fn:toLowerCase(m.email)} ${fn:toLowerCase(m.roomName)}">
                    <td><strong><c:out value="${m.fullName}"/></strong></td>
                    <td><c:out value="${m.email}"/></td>
                    <td><c:out value="${m.roomName}"/></td>
                    <td>
                        <span class="status-badge status-badge--${m.status == 1 ? 'green' : 'crimson'}">
                            <span></span>
                            ${m.status == 1 ? 'Activo' : 'Inactivo'}
                        </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-text-button btn-editar-mag" type="button">
                          <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m4 20 4.5-1 11-11a2.1 2.1 0 0 0-3-3l-11 11zM14.5 7l3 3"/>
                          </svg>
                          Editar
                        </button>

                        <!-- BOTÓN DINÁMICO (ACTIVAR / DESACTIVAR) -->
                        <button class="icon-text-button ${m.status == 1 ? 'icon-text-button--danger' : ''} btn-toggle-mag"
                                type="button" data-id="${m.idMagistrate}" data-name="${fn:escapeXml(m.fullName)}" data-active="${m.status == 1}">
                          <c:choose>
                            <c:when test="${m.status == 1}">
                              <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                <circle cx="12" cy="12" r="9"/><path d="m6 6 12 12"/>
                              </svg>
                              Desactivar
                            </c:when>
                            <c:otherwise>
                              <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                <path d="m5 12 4 4L19 6"/>
                              </svg>
                              Activar
                            </c:otherwise>
                          </c:choose>
                        </button>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="5" style="text-align:center;color:var(--color-muted);">
                    No hay magistrados registrados.
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</div>

<div class="modal-backdrop" id="modalMagistrado" style="display:none;" role="presentation">
  <section class="resolution-modal" role="dialog" aria-modal="true" aria-labelledby="magTitle">
    <header>
      <div class="modal-icon">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="9" cy="8" r="3"/>
          <path d="M3 20c.4-4 2.4-6 6-6s5.6 2 6 6M16 5a3 3 0 0 1 0 6M17 14c2.6.4 3.9 2.4 4 6"/>
        </svg>
      </div>
      <div>
        <p class="section-kicker">Administración institucional</p>
        <h2 id="magTitle">Registrar Magistrado</h2>
      </div>
      <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m6 6 12 12M18 6 6 18"/>
        </svg>
      </button>
    </header>

    <form id="formMagistrado">
      <input type="hidden" name="csrfToken" value="<c:out value='${requestScope.csrfToken}'/>">

      <label class="modal-field">
        <span>Nombre completo</span>
        <input type="text" name="fullName" required placeholder="Ej. Mtra. Elena Salgado Ríos">
      </label>
      <label class="modal-field">
        <span>Correo institucional</span>
        <input type="email" name="email" required placeholder="nombre@tsjmorelos.gob.mx">
      </label>
      <label class="modal-field">
        <span>Sala asignada</span>
        <select name="idRoom" required>
          <option value="" disabled selected>Selecciona la sala</option>
          <c:forEach var="r" items="${rooms}">
            <option value="${r.idRoom}"><c:out value="${r.name}"/> · <c:out value="${r.districtLabel}"/></option>
          </c:forEach>
        </select>
      </label>

      <div class="modal-actions">
        <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
        <button class="primary-action" type="submit">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m5 12 4 4L19 6"/>
          </svg>
          Guardar Magistrado
        </button>
      </div>
    </form>
  </section>
</div>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-magistrates.js"></script>
<script>
  // Implementación básica de Toast si no la tienes en este archivo
  window.showToast = window.showToast || function(msg, type) {
    const toast = document.getElementById("toast");
    if(!toast) return;
    toast.textContent = msg;
    toast.dataset.type = type;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 3000);
  };
</script>
</body>
</html>