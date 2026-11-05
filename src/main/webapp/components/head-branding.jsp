<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="branding" value="${requestScope.branding}" />
<style>
    :root {
    <c:if test="${not empty branding.primaryColor}">
        --color-primary: ${branding.primaryColor};
    </c:if>
    <c:if test="${not empty branding.accentColor}">
        --color-accent: ${branding.accentColor};
    </c:if>
    }
</style>