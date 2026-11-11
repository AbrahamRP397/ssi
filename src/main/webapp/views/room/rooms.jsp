<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Gestión de Salas · SSI</title>
  <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
  <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
  <jsp:include page="/components/sidebar.jsp"/>

  <div class="workspace">
    <jsp:include page="/components/topbar.jsp">
      <jsp:param name="pageName" value="Gestión de Salas"/>
    </jsp:include>

    <main class="main-content">
      <div class="page-heading">
        <div>
          <p class="section-kicker">Estructura jurisdiccional</p>
          <h1>Gestión de Salas</h1>
          <p>Administra las salas, asignaciones y carga activa.</p>
        </div>
        <button class="primary-compact" type="button" data-modal-open="modalSala">
          <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Registrar Nueva Sala
        </button>
      </div>

      <section class="data-card">
        <div class="card-toolbar">
          <div>
            <h2>Salas registradas</h2>
            <p>${not empty rooms ? fn:length(rooms) : 0} salas en ${not empty stats ? stats.totalDistricts : 0} distritos judiciales</p>
          </div>
          <div class="table-tools">
            <label class="search-box">
              <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
              </svg>
              <input type="search" id="searchRooms" placeholder="Buscar sala…">
            </label>
          </div>
        </div>

        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th>Número de Sala</th>
              <th>Dirección</th>
              <th>Distrito</th>
              <th>Magistrado Asignado</th>
              <th>Tocas Activas</th>
              <th>Acciones</th>
            </tr>
            </thead>
            <tbody id="roomsBody">
            <c:choose>
              <c:when test="${not empty rooms}">
                <c:forEach var="r" items="${rooms}">
                  <tr class="room-row"
                      data-search="${fn:toLowerCase(r.name)} ${fn:toLowerCase(r.districtLabel)} ${fn:toLowerCase(r.magistrateName)}">
                    <td><strong><c:out value="${r.name}"/></strong></td>
                    <td><c:out value="${r.address}"/></td>
                    <td><c:out value="${r.districtLabel}"/></td>
                    <td>
                      <div class="magistrate-cell">
                        <span class="${r.magistrateInitials == '—' ? 'unassigned' : ''}">
                            <c:out value="${r.magistrateInitials}"/>
                        </span>
                        <strong><c:out value="${r.magistrateName}"/></strong>
                      </div>
                    </td>
                    <td><span class="case-count"><c:out value="${r.activeTocas}"/></span></td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-text-button btn-editar-sala" type="button"
                                data-id="${r.idRoom}"
                                data-name="${fn:escapeXml(r.name)}"
                                data-address="${fn:escapeXml(r.address)}"
                                data-district="${r.idDistrict}">
                          <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m4 20 4.5-1 11-11a2.1 2.1 0 0 0-3-3l-11 11zM14.5 7l3 3"/>
                          </svg>
                          Editar
                        </button>
                        <!-- BOTÓN DINÁMICO (ACTIVAR / DESACTIVAR) -->
                        <button class="icon-text-button ${r.active ? 'icon-text-button--danger' : ''} btn-toggle-sala"
                                type="button" data-id="${r.idRoom}" data-name="${fn:escapeXml(r.name)}" data-active="${r.active}">
                          <c:choose>
                            <c:when test="${r.active}">
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
                  <td colspan="6" style="text-align:center;color:var(--color-muted);">
                    No hay salas registradas.
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <span>Mostrando ${not empty rooms ? fn:length(rooms) : 0} salas</span>
        </div>
      </section>
    </main>
  </div>
</div>

<!-- MODAL DE REGISTRO -->
<div class="modal-backdrop" id="modalSala" style="display:none;" role="presentation">
  <section class="resolution-modal" role="dialog" aria-modal="true" aria-labelledby="salaTitle">
    <header>
      <div class="modal-icon">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M4 21V6l8-4 8 4v15M2 21h20M8 9h2M14 9h2M8 13h2M14 13h2M10 21v-4h4v4"/>
        </svg>
      </div>
      <div>
        <p class="section-kicker">Estructura jurisdiccional</p>
        <h2 id="salaTitle">Registrar Nueva Sala</h2>
      </div>
      <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m6 6 12 12M18 6 6 18"/>
        </svg>
      </button>
    </header>

    <form id="formSala" action="${contextPath}/admin/salas/save" method="post">
      <input type="hidden" name="csrfToken" value="<c:out value='${requestScope.csrfToken}'/>">

      <label class="modal-field">
        <span>Nombre de la Sala</span>
        <input type="text" name="name" required placeholder="Ej. Sala Sexta">
      </label>

      <label class="modal-field">
        <span>Dirección física</span>
        <input type="text" name="address" required placeholder="Ej. Av. Morelos #123, Col. Centro">
      </label>

      <label class="modal-field">
        <span>Distrito Judicial</span>
        <select name="idDistrict" required>
          <option value="" disabled selected>Selecciona el distrito</option>
          <c:forEach var="d" items="${districts}">
            <option value="${d.idDistrict}"><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </label>

      <div class="modal-actions">
        <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
        <button class="primary-action" type="submit">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m5 12 4 4L19 6"/>
          </svg>
          Guardar Sala
        </button>
      </div>
    </form>
  </section>
</div>

<!-- MODAL DE EDICIÓN -->
<div class="modal-backdrop" id="modalEditarSala" style="display:none;" role="presentation">
  <section class="resolution-modal" role="dialog" aria-modal="true" aria-labelledby="editarSalaTitle">
    <header>
      <div class="modal-icon">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m4 20 4.5-1 11-11a2.1 2.1 0 0 0-3-3l-11 11zM14.5 7l3 3"/>
        </svg>
      </div>
      <div>
        <p class="section-kicker">Estructura jurisdiccional</p>
        <h2 id="editarSalaTitle">Editar Sala</h2>
      </div>
      <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m6 6 12 12M18 6 6 18"/>
        </svg>
      </button>
    </header>

    <form id="formEditarSala" action="${contextPath}/admin/salas/editar" method="post">
      <input type="hidden" name="csrfToken" value="<c:out value='${requestScope.csrfToken}'/>">
      <input type="hidden" name="idRoom" id="editIdRoom">

      <label class="modal-field">
        <span>Nombre de la Sala</span>
        <input type="text" name="name" id="editName" required>
      </label>

      <label class="modal-field">
        <span>Dirección física</span>
        <input type="text" name="address" id="editAddress" required>
      </label>

      <label class="modal-field">
        <span>Distrito Judicial</span>
        <select name="idDistrict" id="editDistrict" required>
          <c:forEach var="d" items="${districts}">
            <option value="${d.idDistrict}"><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </label>

      <div class="modal-actions">
        <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
        <button class="primary-action" type="submit">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m5 12 4 4L19 6"/>
          </svg>
          Guardar Cambios
        </button>
      </div>
    </form>
  </section>
</div>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-rooms.js"></script>
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