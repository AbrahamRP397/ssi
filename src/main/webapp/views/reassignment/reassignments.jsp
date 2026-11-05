<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reasignación Excepcional · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Reasignación excepcional"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Administración institucional</p>
                    <h1>Reasignación Excepcional</h1>
                    <p>Disponible únicamente para atender una baja o impedimento del magistrado asignado.</p>
                </div>
                <!-- NUEVO BOTÓN -->
                <button class="primary-compact" type="button" data-modal-open="modalNuevaReasignacion">
                    <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M12 5v14M5 12h14"/>
                    </svg>
                    Nueva Reasignación
                </button>
            </div>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Solicitudes de reasignación</h2>
                        <p>Las tocas resueltas no pueden reasignarse</p>
                    </div>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Toca</th>
                            <th>Sala actual</th>
                            <th>Motivo</th>
                            <th>Estado</th>
                            <th>Acción</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty reassignments}">
                                <c:forEach var="re" items="${reassignments}">
                                    <tr>
                                        <td>
                                            <strong class="case-number">
                                                <c:out value="${re.tocaNumber}"/>
                                            </strong>
                                        </td>
                                        <td><c:out value="${re.currentRoomName}"/></td>
                                        <td><c:out value="${re.reason}"/></td>
                                        <td>
                                                <span class="status-badge status-badge--gold">
                                                    <span></span>
                                                    <c:out value="${re.status}"/>
                                                </span>
                                        </td>
                                        <td>
                                            <button class="icon-text-button" type="button"
                                                    data-modal-open="modalReasignar">
                                                <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                                    <path d="M3 7h3c5 0 7 10 12 10h3M18 14l3 3-3 3M3 17h3c2 0 3.4-1.6 4.7-3.6M15 7h6M18 4l3 3-3 3"/>
                                                </svg>
                                                Reasignar
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center;color:var(--color-muted);">
                                        No hay solicitudes de reasignación pendientes.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>

            <p class="support">
                La reasignación conserva el expediente original y deja un evento permanente en el historial de la toca.
            </p>
        </main>
    </div>
</div>

<div class="modal-backdrop" id="modalReasignar" style="display:none;" role="presentation">
    <section class="resolution-modal" role="dialog" aria-modal="true" aria-labelledby="reasTitle">
        <header>
            <div class="modal-icon">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M3 7h3c5 0 7 10 12 10h3M18 14l3 3-3 3M3 17h3c2 0 3.4-1.6 4.7-3.6M15 7h6M18 4l3 3-3 3"/>
                </svg>
            </div>
            <div>
                <p class="section-kicker">Reasignación</p>
                <h2 id="reasTitle">Reasignar Toca</h2>
            </div>
            <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m6 6 12 12M18 6 6 18"/>
                </svg>
            </button>
        </header>

        <form id="formReasignar" action="${contextPath}/admin/reasignacion/asignar" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${requestScope.csrfToken}'/>">
            <input type="hidden" name="idReassignment" value="">

            <label class="modal-field">
                <span>Nueva Sala destino</span>
                <select name="idRoomTo" required>
                    <option value="" disabled selected>Selecciona la sala</option>
                    <c:forEach var="r" items="${rooms}">
                        <option value="${r.idRoom}"><c:out value="${r.name}"/></option>
                    </c:forEach>
                </select>
            </label>
            <label class="modal-field">
                <span>Motivo / Justificación</span>
                <textarea name="reason" rows="3" required></textarea>
            </label>

            <div class="modal-actions">
                <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
                <button class="primary-action" type="submit">
                    <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="m5 12 4 4L19 6"/>
                    </svg>
                    Confirmar Reasignación
                </button>
            </div>
        </form>
    </section>
</div>

<div class="modal-backdrop" id="modalNuevaReasignacion" style="display:none;" role="presentation">
    <section class="resolution-modal" role="dialog" aria-modal="true">
        <header>
            <div class="modal-icon">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M3 7h3c5 0 7 10 12 10h3M18 14l3 3-3 3M3 17h3c2 0 3.4-1.6 4.7-3.6M15 7h6M18 4l3 3-3 3"/>
                </svg>
            </div>
            <div>
                <p class="section-kicker">Reasignación</p>
                <h2>Nueva Reasignación</h2>
            </div>
            <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m6 6 12 12M18 6 6 18"/>
                </svg>
            </button>
        </header>

        <form id="formNuevaReasignacion" action="${contextPath}/admin/reasignacion/crear" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${requestScope.csrfToken}'/>">

            <label class="modal-field">
                <span>Seleccionar Toca</span>
                <!-- NOTA: Para que esto funcione, el Servlet debe enviar una lista de "tocasActivas" -->
                <select name="idToca" required>
                    <option value="" disabled selected>Selecciona la toca</option>
                    <c:forEach var="t" items="${tocasActivas}">
                        <option value="${t.idToca}"><c:out value="${t.tocaNumber}"/> - <c:out value="${t.roomName}"/></option>
                    </c:forEach>
                </select>
            </label>

            <label class="modal-field">
                <span>Nueva Sala destino</span>
                <select name="idRoomTo" required>
                    <option value="" disabled selected>Selecciona la sala</option>
                    <c:forEach var="r" items="${rooms}">
                        <option value="${r.idRoom}"><c:out value="${r.name}"/></option>
                    </c:forEach>
                </select>
            </label>

            <label class="modal-field">
                <span>Motivo / Justificación</span>
                <textarea name="reason" rows="3" required></textarea>
            </label>

            <div class="modal-actions">
                <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
                <button class="primary-action" type="submit">Crear Solicitud</button>
            </div>
        </form>
    </section>
</div>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-reassignments.js"></script>
</body>
</html>