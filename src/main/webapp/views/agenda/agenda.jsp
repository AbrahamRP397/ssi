<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi Agenda · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Mi Agenda"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Área jurisdiccional</p>
                    <h1>Mi Agenda de Ponencias</h1>
                    <p>Consulta tus sesiones programadas. Esta vista es solo de consulta.</p>
                </div>
                <div class="date-chip">
                    <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <rect x="3" y="5" width="18" height="16" rx="2"/>
                        <path d="M7 3v4M17 3v4M3 10h18"/>
                    </svg>
                    <span>Semana del <c:out value="${weekLabel}"/></span>
                </div>
            </div>

            <section class="data-card">
                <div class="card-toolbar">
                    <div>
                        <h2>Agenda semanal</h2>
                        <p>Solo se muestran ponencias de la Sala Tercera</p>
                    </div>
                    <button class="filter-button" type="button">
                        Vista semanal
                        <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m9 6 6 6-6 6"/>
                        </svg>
                    </button>
                </div>

                <div class="calendar-scroll">
                    <div class="agenda-calendar" aria-label="Calendario semanal de ponencias">
                        <div class="calendar-corner">Hora</div>
                        <c:forEach var="d" items="${days}">
                            <div class="calendar-day">
                                <strong><c:out value="${d['short']}"/></strong>
                                <span><c:out value="${d['dayNumber']}"/></span>
                            </div>
                        </c:forEach>

                        <div class="calendar-hours">
                            <c:forEach var="h" items="${hours}">
                                <span><c:out value="${h}"/></span>
                            </c:forEach>
                        </div>

                        <div class="calendar-grid" aria-hidden="true">
                            <c:forEach var="i" begin="1" end="55">
                                <span></span>
                            </c:forEach>
                        </div>

                        <div class="calendar-events">
                            <c:forEach var="item" items="${agendaItems}">
                                <article class="calendar-event"
                                         style="grid-column:${item['day'] + 1};
                                                 grid-row:${item['start'] + 1} / span ${item['duration']};">
                                    <strong><c:out value="${item['tocaNumber']}"/></strong>
                                    <span><c:out value="${item['originalCaseNumber']}"/></span>
                                    <small>
                                        <c:out value="${item['time']}"/>
                                        · <c:out value="${item['kind']}"/>
                                    </small>
                                </article>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <div class="table-footer">
                    <span>${not empty agendaItems ? fn:length(agendaItems) : 0} ponencias programadas · Solo consulta</span>
                    <span>Horario judicial: 08:00 a 20:00</span>
                </div>
            </section>
        </main>
    </div>
</div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
</body>
</html>