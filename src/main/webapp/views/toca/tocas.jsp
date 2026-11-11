<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="user" value="${sessionScope.usuario}" />
<c:set var="totalTocas" value="${not empty tocas ? fn:length(tocas) : 0}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Tocas · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Mis Tocas"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Área jurisdiccional</p>
                    <h1>Mis Tocas Asignadas</h1>
                    <p>Consulta y gestiona los asuntos asignados a tu ponencia.</p>
                </div>
                <div class="date-chip">
                    <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <rect x="3" y="5" width="18" height="16" rx="2"/>
                        <path d="M7 3v4M17 3v4M3 10h18"/>
                    </svg>
                    <span id="todayLabel"></span>
                </div>
            </div>

            <section class="summary-grid" aria-label="Resumen de actividad">
                <article class="summary-card">
                    <div class="summary-icon summary-icon--gold">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/>
                        </svg>
                    </div>
                    <div>
                        <p>Tocas pendientes</p>
                        <strong>${not empty summary ? summary.pending : 0}</strong>
                        <small>${not empty summary ? summary.pendingHelper : 'Sin actividad'}</small>
                    </div>
                </article>
                <article class="summary-card">
                    <div class="summary-icon summary-icon--crimson">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <rect x="3" y="7" width="18" height="13" rx="2"/>
                            <path d="M8 7V4h8v3M3 12h18M10 12v2h4v-2"/>
                        </svg>
                    </div>
                    <div>
                        <p>Ponencias esta semana</p>
                        <strong>${not empty summary ? summary.weekPonencias : 0}</strong>
                        <small>${not empty summary ? summary.weekPonenciasHelper : ''}</small>
                    </div>
                </article>
                <article class="summary-card">
                    <div class="summary-icon summary-icon--green">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m5 12 4 4L19 6"/>
                        </svg>
                    </div>
                    <div>
                        <p>Resoluciones emitidas</p>
                        <strong>${not empty summary ? summary.resolutions : 0}</strong>
                        <small>${not empty summary ? summary.resolutionsHelper : ''}</small>
                    </div>
                </article>
            </section>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Tocas recientes</h2>
                        <p>Últimas asignaciones y actualizaciones</p>
                    </div>
                    <div class="table-tools">
                        <label class="search-box">
                            <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
                            </svg>
                            <input type="search" id="searchTocas" placeholder="Buscar por número…">
                        </label>
                        <select class="filter-button" id="statusFilterSelect">
                            <option value="">Todos los estados</option>
                            <option value="Nueva">Nueva</option>
                            <option value="En ponencia">En ponencia</option>
                            <option value="Resuelto">Resuelto</option>
                        </select>
                    </div>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Número de Toca</th>
                            <th>Materia</th>
                            <th>Fecha de recepción</th>
                            <th>Estado</th>
                            <th><span class="sr-only">Acciones</span></th>
                        </tr>
                        </thead>
                        <tbody id="tocasBody">
                        <c:choose>
                            <c:when test="${not empty tocas}">
                                <c:forEach var="toca" items="${tocas}">
                                    <tr class="toca-row"
                                        data-number="${toca.tocaNumber}"
                                        data-status="${toca.status}"
                                        data-search="${fn:toLowerCase(toca.tocaNumber)} ${fn:toLowerCase(toca.subject)}">
                                        <td>
                                            <strong class="case-number">
                                                <c:out value="${toca.tocaNumber}"/>
                                            </strong>
                                        </td>
                                        <td><c:out value="${toca.subject}"/></td>
                                        <td><c:out value="${toca.receptionDateLabel}"/></td>
                                        <td>
                                                <span class="status-badge status-badge--${toca.statusTone}">
                                                    <span></span>
                                                    <c:out value="${toca.status}"/>
                                                </span>
                                        </td>
                                        <td>
                                            <a class="consult-button"
                                               href="${contextPath}/mis-tocas/${toca.idToca}">
                                                Consultar
                                                <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                                    <path d="M5 12h14M14 7l5 5-5 5"/>
                                                </svg>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center;color:var(--color-muted);">
                                        No tienes tocas asignadas todavía.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">
                    <span id="tocasFooterCount">
                        Mostrando ${totalTocas} de ${totalTocas} tocas
                    </span>
                </div>
            </section>
        </main>
    </div>
</div>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-tocas.js"></script>
</body>
</html>