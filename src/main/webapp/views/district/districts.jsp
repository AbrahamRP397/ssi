<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Distritos · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Gestión de Distritos"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Administración institucional</p>
                    <h1>Configuración del Sistema</h1>
                    <p>Consulta el catálogo territorial externo y administra salas y magistrados locales.</p>
                </div>
            </div>

            <section class="summary-grid summary-grid--admin">
                <article class="summary-card">
                    <div class="summary-icon summary-icon--crimson">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M4 21V6l8-4 8 4v15M2 21h20M8 9h2M14 9h2M8 13h2M14 13h2M10 21v-4h4v4"/>
                        </svg>
                    </div>
                    <div>
                        <p>Total de Salas</p>
                        <strong>${not empty stats ? stats.totalRooms : 0}</strong>
                        <small>${not empty stats ? stats.roomsHelper : ''}</small>
                    </div>
                </article>
                <article class="summary-card">
                    <div class="summary-icon summary-icon--green">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="9" cy="8" r="3"/>
                            <path d="M3 20c.4-4 2.4-6 6-6s5.6 2 6 6M16 5a3 3 0 0 1 0 6M17 14c2.6.4 3.9 2.4 4 6"/>
                        </svg>
                    </div>
                    <div>
                        <p>Magistrados activos</p>
                        <strong>${not empty stats ? stats.activeMagistrates : 0}</strong>
                        <small>${not empty stats ? stats.magistratesHelper : ''}</small>
                    </div>
                </article>
            </section>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Distritos Judiciales</h2>
                        <p>Catálogo de consulta proveniente del sistema de primera instancia</p>
                    </div>
                    <label class="search-box">
                        <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
                        </svg>
                        <input type="search" id="searchDistricts" placeholder="Buscar distrito…">
                    </label>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Distrito Judicial</th>
                            <th>Cabecera</th>
                            <th>Salas locales</th>
                            <th>Estado</th>
                        </tr>
                        </thead>
                        <tbody id="districtsBody">
                        <c:choose>
                            <c:when test="${not empty districts}">
                                <c:forEach var="d" items="${districts}">
                                    <tr class="district-row"
                                        data-search="${fn:toLowerCase(d.name)} ${fn:toLowerCase(d.headCity)}">
                                        <td><strong><c:out value="${d.name}"/></strong></td>
                                        <td><c:out value="${d.headCity}"/></td>
                                        <td>
                                                <%-- Usamos el mapa enviado desde el servlet para contar las salas --%>
                                            <c:out value="${roomCountByDistrict[d.idDistrict] != null ? roomCountByDistrict[d.idDistrict] : 0}"/>
                                        </td>
                                        <td>
                                                <%-- Evaluamos el estado numérico (1 = Activo, 0 = Inactivo) --%>
                                            <span class="status-badge status-badge--${d.status == 1 ? 'green' : 'gold'}">
                        <span></span>
                        ${d.status == 1 ? 'Activo' : 'Inactivo'}
                    </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="4" style="text-align:center;color:var(--color-muted);">
                                        No hay distritos registrados en el catálogo.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">
                    <span>${not empty districts ? fn:length(districts) : 0} distritos judiciales registrados</span>
                </div>
            </section>
        </main>
    </div>
</div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script>
    (function () {
        const search = document.getElementById("searchDistricts");
        const rows = Array.from(document.querySelectorAll(".district-row"));
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