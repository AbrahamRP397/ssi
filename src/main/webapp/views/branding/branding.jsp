<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="b" value="${requestScope.branding}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Identidad Visual · SSI</title>
    <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
    <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp"/>

    <div class="workspace">
        <jsp:include page="/components/topbar.jsp">
            <jsp:param name="pageName" value="Identidad visual"/>
        </jsp:include>

        <main class="main-content">
            <div class="page-heading">
                <div>
                    <p class="section-kicker">Configuración institucional</p>
                    <h1>Identidad Visual</h1>
                    <p>Personaliza el logotipo y los colores principales del sistema.</p>
                </div>
            </div>

            <div class="settings-layout">
                <form class="data-card settings-form" id="formBranding" method="post"
                      enctype="multipart/form-data"
                      action="${contextPath}/admin/identidad">
                    <input type="hidden" name="csrfToken"
                           value="<c:out value='${requestScope.csrfToken}'/>">

                    <div class="settings-section">
                        <div>
                            <h2>Logotipo institucional</h2>
                            <p>Usa una imagen PNG, JPG o SVG con fondo transparente.</p>
                        </div>
                        <label class="logo-uploader">
                            <span class="logo-preview">
                                <jsp:include page="/components/brand.jsp">
                                    <jsp:param name="variant" value="workspace"/>
                                </jsp:include>
                            </span>
                            <span>
                                <strong>Seleccionar nuevo logotipo</strong>
                                <small>La vista previa se actualiza antes de guardar.</small>
                            </span>
                            <input type="file" name="logo"
                                   accept="image/png,image/jpeg,image/svg+xml"
                                   id="logoInput">
                        </label>
                    </div>

                    <div class="settings-section">
                        <div>
                            <h2>Paleta de color</h2>
                            <p>Define el color institucional y el color de acciones.</p>
                        </div>
                        <div class="color-fields">
                            <label>
                                <span>Color institucional</span>
                                <input type="color" name="primaryColor"
                                       value="${b.primaryColor}" id="primaryInput">
                                <strong id="primaryLabel">${b.primaryColor}</strong>
                            </label>
                            <label>
                                <span>Color de acciones</span>
                                <input type="color" name="accentColor"
                                       value="${b.accentColor}" id="accentInput">
                                <strong id="accentLabel">${b.accentColor}</strong>
                            </label>
                        </div>
                    </div>

                    <div class="settings-actions">
                        <span id="savedMsg" style="display:none;">Cambios aplicados correctamente.</span>
                        <button class="primary-compact" type="submit">
                            <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
                                <path d="m5 12 4 4L19 6"/>
                            </svg>
                            Guardar identidad
                        </button>
                    </div>
                </form>

                <aside class="data-card brand-preview-card">
                    <p class="section-kicker">Vista previa</p>
                    <div class="brand-preview"
                         id="brandPreview"
                         style="--preview-primary:${b.primaryColor};--preview-accent:${b.accentColor};">
                        <jsp:include page="/components/brand.jsp">
                            <jsp:param name="variant" value="workspace"/>
                        </jsp:include>
                        <span>Portal institucional</span>
                        <button type="button">Acción principal</button>
                    </div>
                    <p>Los cambios se aplicarán a la navegación, botones y elementos de identidad del sistema.</p>
                </aside>
            </div>
        </main>
    </div>
</div>

<div id="toast" class="toast-message" role="status" aria-live="polite"></div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
<script src="${contextPath}/assets/js/ssi-branding.js"></script>
</body>
</html>