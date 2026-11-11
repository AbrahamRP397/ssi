<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="user" value="${requestScope.profileUser}" />
<c:set var="initials"
       value="${fn:toUpperCase(fn:substring(user.name, 0, 1))}${fn:toUpperCase(fn:substring(user.surname, 0, 1))}" />

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Mi Perfil · SSI</title>
  <link rel="stylesheet" href="${contextPath}/assets/css/ssi.css">
  <jsp:include page="/components/head-branding.jsp"/>
</head>
<body data-context-path="${contextPath}">

<div class="app-shell">
  <jsp:include page="/components/sidebar.jsp"/>

  <div class="workspace">
    <jsp:include page="/components/topbar.jsp">
      <jsp:param name="pageName" value="Mi Perfil"/>
    </jsp:include>

    <main class="main-content">
      <div class="page-heading">
        <div>
          <p class="section-kicker">Cuenta institucional</p>
          <h1>Mi Perfil</h1>
          <p>Consulta tus datos de adscripción y permisos dentro del sistema.</p>
        </div>
      </div>

      <section class="profile-layout">
        <article class="data-card profile-card">
          <div class="profile-avatar">${initials}</div>
          <h2>
            <c:out value="${user.name}"/>
            <c:out value="${user.surname}"/>
          </h2>
          <p>
            <c:choose>
              <c:when test="${user.role == 'Administrador'}">Administrador del sistema</c:when>
              <c:otherwise>Magistrado</c:otherwise>
            </c:choose>
          </p>
          <span class="status-badge status-badge--green">
                        <span></span>
                        Cuenta activa
                    </span>
        </article>

        <article class="data-card profile-details">
          <div>
            <span>Correo institucional</span>
            <strong><c:out value="${user.email}"/></strong>
          </div>
          <div>
            <span>Distrito judicial</span>
            <strong><c:out value="${districtLabel}"/></strong>
          </div>
          <div>
            <span>Rol y permisos</span>
            <strong>
              <c:choose>
                <c:when test="${user.role == 'Administrador'}">Administración completa</c:when>
                <c:otherwise>Consulta, ponencias y resoluciones</c:otherwise>
              </c:choose>
            </strong>
          </div>
          <div>
            <span>Último acceso</span>
            <strong><c:out value="${lastAccessLabel}"/></strong>
          </div>
        </article>
      </section>
    </main>
  </div>
</div>

<script src="${contextPath}/assets/js/ssi-api.js"></script>
<script src="${contextPath}/assets/js/ssi-workspace.js"></script>
</body>
</html>