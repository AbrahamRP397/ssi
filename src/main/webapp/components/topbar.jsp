<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="isAdmin" value="${sessionScope.rol == 'Administrador'}" />
<c:set var="user" value="${sessionScope.usuario}" />
<c:set var="initials" value="${fn:toUpperCase(fn:substring(user.name, 0, 1))}${fn:toUpperCase(fn:substring(user.surname, 0, 1))}" />

<header class="topbar">
    <div class="breadcrumbs">
        <span>Inicio</span>
        <svg class="ui-icon ui-icon--small" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m9 6 6 6-6 6"/>
        </svg>
        <strong>${param.pageName != null ? param.pageName : requestScope.pageName}</strong>    </div>
    <div class="topbar-actions">
        <button class="icon-button" type="button" aria-label="Notificaciones">
            <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/>
            </svg>
            <span class="notification-dot"></span>
        </button>
        <span class="topbar-divider"></span>
        <div class="user-switcher">
            <span class="avatar">${initials}</span>
            <span class="user-copy">
                <strong>
                    <c:out value="${user.name}"/> <c:out value="${user.surname}"/>
                </strong>
                <small>
                    <c:choose>
                        <c:when test="${isAdmin}">Administrador del sistema</c:when>
                        <c:otherwise>Magistrado</c:otherwise>
                    </c:choose>
                </small>
            </span>
        </div>
    </div>
</header>