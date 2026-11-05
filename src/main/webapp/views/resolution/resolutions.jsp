<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resoluciones Emitidas · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Resoluciones emitidas"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Área jurisdiccional</p>
                    <h1>Resoluciones Emitidas</h1>
                    <p>Consulta y descarga las resoluciones de segunda instancia registradas.</p>
                </div>
            </div>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Resoluciones de la Sala Tercera</h2>
                        <p>El documento PDF queda integrado al expediente</p>
                    </div>
                    <label class="search-box">
                        <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
                        </svg>
                        <input type="search" id="searchResolutions" placeholder="Buscar por toca…">
                    </label>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Toca</th>
                            <th>Sentido</th>
                            <th>Fecha</th>
                            <th>Estado</th>
                            <th>Documento</th>
                        </tr>
                        </thead>
                        <tbody id="resolutionsBody">
                        <c:choose>
                            <c:when test="${not empty resolutions}">
                                <c:forEach var="r" items="${resolutions}">
                                    <tr class="resolution-row"
                                        data-search="${fn:toLowerCase(r.tocaNumber)}">
                                        <td>
                                            <strong class="case-number">
                                                <c:out value="${r.tocaNumber}"/>
                                            </strong>
                                        </td>
                                        <td><c:out value="${r.outcome}"/></td>
                                        <td><c:out value="${r.emittedLabel}"/></td>
                                        <td>
                                                <span class="status-badge status-badge--green">
                                                    <span></span>
                                                    Emitida
                                                </span>
                                        </td>
                                        <td>
                                            <a class="icon-text-button"
                                               href="${contextPath}/resoluciones/${r.idResolution}/pdf"
                                               target="_blank" rel="noopener">
                                                <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                                    <path d="M6 2h9l4 4v16H6z"/><path d="M14 2v5h5M9 12h6M9 16h6"/>
                                                </svg>
                                                Descargar PDF
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center;color:var(--color-muted);">
                                        Aún no has emitido resoluciones.
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
        const search = document.getElementById("searchResolutions");
        const rows = Array.from(document.querySelectorAll(".resolution-row"));
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