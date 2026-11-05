<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="variant" value="${param.variant == 'workspace' ? 'workspace' : 'lockup'}" />
<c:set var="compact" value="${param.compact == 'true'}" />
<c:set var="logoUrl" value="${sessionScope.branding.logoUrl}" />

<c:choose>
  <c:when test="${variant == 'workspace'}">
    <div class="workspace-brand">
      <div class="workspace-seal" aria-hidden="true">
        <c:choose>
          <c:when test="${not empty logoUrl}">
            <img src="${logoUrl}" alt="">
          </c:when>
          <c:otherwise>
            <svg viewBox="0 0 64 64">
              <circle cx="32" cy="32" r="30" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M18 22h28M23 19h18M32 15v26M20 43h24M24 39h16"
                    fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round"/>
              <path d="m22 23-7 13h14l-7-13Zm20 0-7 13h14l-7-13Z"
                    fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
              <path d="M14 36c1.8 3 13.8 3 16 0M34 36c1.8 3 13.8 3 16 0"
                    fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </c:otherwise>
        </c:choose>
      </div>
      <div>
        <strong>Poder Judicial</strong>
        <span>Estado de Morelos</span>
      </div>
    </div>
  </c:when>

  <c:otherwise>
    <div class="brand-lockup">
      <div class="seal ${compact ? 'seal--compact' : ''}" aria-hidden="true">
        <c:choose>
          <c:when test="${not empty logoUrl}">
            <img src="${logoUrl}" alt="">
          </c:when>
          <c:otherwise>
            <svg viewBox="0 0 64 64">
              <circle cx="32" cy="32" r="30" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M18 22h28M23 19h18M32 15v26M20 43h24M24 39h16"
                    fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round"/>
              <path d="m22 23-7 13h14l-7-13Zm20 0-7 13h14l-7-13Z"
                    fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
              <path d="M14 36c1.8 3 13.8 3 16 0M34 36c1.8 3 13.8 3 16 0"
                    fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="brand-copy">
        <span>Poder Judicial</span>
        <small>Estado de Morelos</small>
      </div>
    </div>
  </c:otherwise>
</c:choose>