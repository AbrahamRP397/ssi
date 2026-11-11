<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="toca" value="${requestScope.toca}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Toca ${toca.tocaNumber} · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}" data-toca-id="${toca.idToca}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Detalle de Toca"/>
        </jsp:include>

        <main class="main-content">
            <a class="back-button" href="${contextPath}/mis-tocas" id="backToTocas">
                <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
                Volver a Mis Tocas
            </a>

            <div class="case-heading">
                <div>
                    <div class="case-title-line">
                        <h1>Toca <span><c:out value="${toca.tocaNumber}"/></span></h1>
                        <span class="status-badge status-badge--${toca.statusTone}">
                            <span></span>
                            <c:out value="${toca.status}"/>
                        </span>
                    </div>
                    <p>
                        Expediente original: <c:out value="${toca.originalCaseNumber}"/>
                        · <c:out value="${toca.subject}"/>
                        · <c:out value="${toca.roomName}"/>
                    </p>
                </div>
                <span class="updated-label">
                    <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/>
                    </svg>
                    Actualizado <c:out value="${toca.updatedLabel}"/>
                </span>
            </div>

            <div class="tabs" role="tablist" data-tab-group>
                <button class="active" type="button" role="tab" data-tab="datos">Datos de la Toca</button>
                <button type="button" role="tab" data-tab="expediente">Expediente Original</button>
                <button type="button" role="tab" data-tab="historial">Historial</button>
            </div>

            <div class="detail-layout">
                <section class="document-card">

                    <%-- TAB: Expediente --%>
                    <div data-tab-panel="expediente">
                        <div class="document-heading">
                            <div>
                                <h2>Documentos del expediente</h2>
                                <p>
                                    ${not empty toca.documents ? fn:length(toca.documents) : 0} documentos
                                    · <c:out value="${toca.documentsTotalMb}"/> MB en total
                                </p>
                            </div>
                            <button type="button">
                                <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                    <circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>
                                </svg>
                                Buscar en documentos
                            </button>
                        </div>
                        <div class="document-list">
                            <c:forEach var="doc" items="${toca.documents}" varStatus="st">
                                <article class="document-row">
                                    <div class="pdf-icon"><span>PDF</span></div>
                                    <div class="document-info">
                                        <strong><c:out value="${doc.title}"/></strong>
                                        <p>
                                            <c:out value="${doc.type}"/>
                                            · <c:out value="${doc.pages}"/> páginas
                                            · <c:out value="${doc.sizeLabel}"/>
                                        </p>
                                    </div>
                                    <time><c:out value="${doc.uploadedLabel}"/></time>
                                    <button type="button" aria-label="Abrir ${doc.title}">
                                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                                            <path d="M5 12h14M14 7l5 5-5 5"/>
                                        </svg>
                                    </button>
                                    <c:if test="${not st.last}">
                                        <span class="document-connector"></span>
                                    </c:if>
                                </article>
                            </c:forEach>
                            <c:if test="${empty toca.documents}">
                                <div class="tab-placeholder">
                                    <h2>Sin documentos</h2>
                                    <p>Esta toca aún no tiene documentos cargados.</p>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <%-- TAB: Datos --%>
                    <div data-tab-panel="datos" style="display:none;">
                        <div class="tab-placeholder">
                            <div class="summary-icon summary-icon--crimson">
                                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                                    <path d="M3 6h7l2 2h9v11H3z"/><path d="M3 9h18"/>
                                </svg>
                            </div>
                            <h2>Datos generales de la Toca</h2>
                            <p style="line-height:2;">
                                Expediente original: <c:out value="${toca.originalCaseNumber}"/><br>
                                Apelante: <c:out value="${toca.appellant}"/><br>
                                Sala asignada por Oficialía: <c:out value="${toca.roomName}"/><br>
                                Fecha de recepción: <c:out value="${toca.receptionDateLabel}"/>
                            </p>
                        </div>
                    </div>

                    <%-- TAB: Historial --%>
                    <div data-tab-panel="historial" style="display:none;">
                        <div class="tab-placeholder">
                            <div class="summary-icon summary-icon--gold">
                                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                                    <circle cx="12" cy="12" r="9"/><path d="M12 7v6l4 2"/>
                                </svg>
                            </div>
                            <h2>Historial inalterable de la Toca</h2>
                            <p style="line-height:2;">
                                <c:forEach var="event" items="${toca.history}">
                                    <c:out value="${event.occurredLabel}"/>
                                    · <c:out value="${event.description}"/><br>
                                </c:forEach>
                                <c:if test="${empty toca.history}">
                                    Sin movimientos registrados.
                                </c:if>
                            </p>
                        </div>
                    </div>
                </section>

                <aside class="action-panel">
                    <p class="section-kicker">Acciones del expediente</p>
                    <h2>Continuar con la Toca</h2>
                    <p>Registra sesiones de ponencia o emite la resolución final con su documento PDF.</p>
                    <button class="secondary-action" type="button"
                            data-modal-open="modalPonencia">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m4 20 4.5-1 11-11a2.1 2.1 0 0 0-3-3l-11 11zM14.5 7l3 3"/>
                        </svg>
                        Registrar Ponencia
                    </button>
                    <button class="primary-action" type="button"
                            data-modal-open="modalResolucion">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M6 2h9l4 4v16H6z"/><path d="M14 2v5h5M9 12h6M9 16h6"/>
                        </svg>
                        Emitir Resolución
                    </button>
                    <div class="action-note">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M6 2h9l4 4v16H6z"/><path d="M14 2v5h5M9 12h6M9 16h6"/>
                        </svg>
                        <p>
                            <strong>Última actividad</strong>
                            <span><c:out value="${toca.lastActivityLabel}"/></span>
                        </p>
                    </div>
                </aside>
            </div>
        </main>
    </div>
</div>

<%-- Modales --%>
<jsp:include page="/views/toca/modal-ponencia.jsp"/>
<jsp:include page="/views/toca/modal-resolucion.jsp"/>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-toca-detail.js"></script>
</body>
</html>