<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoría · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Auditoría"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Control y trazabilidad</p>
                    <h1>Auditoría del Sistema</h1>
                    <p>Registro cronológico de acciones realizadas por magistrados y administradores.</p>
                </div>
                <div class="audit-count">
                    <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M6 3h12v18H6zM9 7h6M9 11h6M9 15h3"/>
                        <path d="m14 16 1.5 1.5L19 14"/>
                    </svg>
                    <span>${not empty events ? fn:length(events) : 0} movimientos registrados</span>
                </div>
            </div>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Bitácora de actividad</h2>
                        <p>Los eventos se conservan como evidencia de operación.</p>
                    </div>
                    <label class="search-box">
                        <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
                        </svg>
                        <input type="search" id="searchAudit" placeholder="Buscar movimiento…">
                    </label>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Fecha y hora</th>
                            <th>Usuario</th>
                            <th>Rol</th>
                            <th>Movimiento</th>
                            <th>Detalle</th>
                        </tr>
                        </thead>
                        <tbody id="auditBody">
                        <c:choose>
                            <c:when test="${not empty events}">
                                <c:forEach var="e" items="${events}">
                                    <tr class="audit-row"
                                        data-search="${fn:toLowerCase(e.actorName)} ${fn:toLowerCase(e.action)} ${fn:toLowerCase(e.detail)}">
                                        <td><c:out value="${e.occurredLabel}"/></td>
                                        <td><strong><c:out value="${e.actorName}"/></strong></td>
                                        <td>
                                                <span class="status-badge status-badge--${e.tone}">
                                                    <span></span>
                                                    <c:choose>
                                                        <c:when test="${e.role == 'magistrado'}">Magistrado</c:when>
                                                        <c:otherwise>Administrador</c:otherwise>
                                                    </c:choose>
                                                </span>
                                        </td>
                                        <td><c:out value="${e.action}"/></td>
                                        <td><c:out value="${e.detail}"/></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center;color:var(--color-muted);">
                                        No hay movimientos registrados.
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

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script>
    (function () {
        const search = document.getElementById("searchAudit");
        const rows = Array.from(document.querySelectorAll(".audit-row"));
        if (!search) return;
        search.addEventListener("input", function () {
            const q = search.value.toLowerCase().trim();
            rows.forEach(r => {
                const match = !q || (r.dataset.search || "").includes(q);
                r.style.display = match ? "" : "none";
            });
        });
    })();
</script>
</body>
</html>