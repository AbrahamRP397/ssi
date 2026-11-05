<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="toca" value="${requestScope.toca}" />

<div class="modal-backdrop" id="modalPonencia" style="display:none;" role="presentation">
  <section class="resolution-modal" role="dialog" aria-modal="true"
           aria-labelledby="ponencia-title">
    <header>
      <div class="modal-icon">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <rect x="3" y="7" width="18" height="13" rx="2"/>
          <path d="M8 7V4h8v3M3 12h18M10 12v2h4v-2"/>
        </svg>
      </div>
      <div>
        <p class="section-kicker">Toca <c:out value="${toca.tocaNumber}"/></p>
        <h2 id="ponencia-title">Registrar Ponencia</h2>
      </div>
      <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m6 6 12 12M18 6 6 18"/>
        </svg>
      </button>
    </header>

    <div id="ponenciaFormWrap">
      <form id="ponenciaForm"
            action="${contextPath}/mis-tocas/${toca.idToca}/ponencia"
            method="post">
        <input type="hidden" name="csrfToken"
               value="<c:out value='${requestScope.csrfToken}'/>">

        <label class="modal-field">
          <span>Fecha de ponencia</span>
          <input type="date" name="sessionDate" required>
        </label>
        <label class="modal-field">
          <span>Horario de la sesión</span>
          <div class="time-fields">
            <input type="time" name="startTime" required>
            <span>a</span>
            <input type="time" name="endTime" required>
          </div>
        </label>
        <p class="form-hint">
          Se validará una holgura mínima de una hora respecto a otras ponencias del magistrado.
        </p>

        <div class="modal-actions">
          <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
          <button class="primary-action" type="submit">
            <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path d="m5 12 4 4L19 6"/>
            </svg>
            Guardar Ponencia
          </button>
        </div>
      </form>
    </div>

    <div id="ponenciaSuccess" style="display:none;" class="success-panel">
      <div>
        <svg class="ui-icon ui-icon--large" viewBox="0 0 24 24" aria-hidden="true">
          <path d="m5 12 4 4L19 6"/>
        </svg>
      </div>
      <h3>Ponencia registrada</h3>
      <p>La sesión quedó registrada en el historial, la agenda de la Sala Tercera y la bitácora de auditoría.</p>
      <button class="primary-action" type="button" data-modal-close>Volver a la toca</button>
    </div>
  </section>
</div>