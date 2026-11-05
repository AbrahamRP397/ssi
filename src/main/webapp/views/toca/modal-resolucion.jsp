<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="toca" value="${requestScope.toca}" />

<div class="modal-backdrop" id="modalResolucion" style="display:none;" role="presentation">
    <!-- Agregamos la clase modal-xl -->
    <section class="resolution-modal modal-xl" role="dialog" aria-modal="true"
             aria-labelledby="resolucion-title">
        <header>
            <div class="modal-icon">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M6 2h9l4 4v16H6z"/><path d="M14 2v5h5M9 12h6M9 16h6"/>
                </svg>
            </div>
            <div>
                <p class="section-kicker">Toca <c:out value="${toca.tocaNumber}"/></p>
                <h2 id="resolucion-title">Emitir Resolución Final</h2>
            </div>
            <button class="modal-close" type="button" data-modal-close aria-label="Cerrar">
                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m6 6 12 12M18 6 6 18"/>
                </svg>
            </button>
        </header>

        <div id="resolucionFormWrap" class="modal-body-scroll">
            <form id="resolucionForm"
                  action="${contextPath}/mis-tocas/${toca.idToca}/resolucion"
                  method="post">
                <input type="hidden" name="csrfToken"
                       value="<c:out value='${requestScope.csrfToken}'/>">

                <label class="modal-field">
                    <span>Sentido de la Resolución</span>
                    <select name="outcome" required>
                        <option value="" disabled selected>Selecciona el sentido</option>
                        <option value="Confirma">Confirma</option>
                        <option value="Revoca">Revoca</option>
                        <option value="Modifica">Modifica</option>
                    </select>
                </label>

                <div class="modal-field">
                    <span>Texto de la Resolución</span>
                    <div class="editor-shell">
                        <div class="editor-toolbar" aria-label="Formato de texto">
                            <button type="button" data-cmd="bold" aria-label="Negritas"><strong>N</strong></button>
                            <button type="button" data-cmd="italic" aria-label="Cursivas"><em>K</em></button>
                            <button type="button" data-cmd="underline" aria-label="Subrayado"><u>S</u></button>
                            <span></span>
                            <button type="button" data-cmd="insertUnorderedList" aria-label="Lista">• Lista</button>
                        </div>
                        <!-- Le damos el ID correcto -->
                        <div class="resolution-editor"
                             id="resolucionEditor"
                             contenteditable="true"
                             role="textbox"
                             aria-multiline="true"
                             data-placeholder="Redacta el proyecto de resolución aquí..."></div>
                        <input type="hidden" name="body" id="resolucionBody">
                    </div>
                    <small class="field-help">
                        Al guardar, el sistema generará automáticamente el PDF oficial y lo integrará al expediente.
                    </small>
                    <small class="field-error" id="resolucionError" style="display:none;">
                        Redacta el texto de la resolución para continuar.
                    </small>
                </div>

                <label class="modal-field">
                    <span>Comentarios / Observaciones <small>Opcional</small></span>
                    <textarea name="observations" rows="3"
                              placeholder="Agrega notas relevantes sobre la resolución…"></textarea>
                </label>

                <div class="modal-actions">
                    <button class="cancel-button" type="button" data-modal-close>Cancelar</button>
                    <button class="primary-action" type="submit">
                        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="m5 12 4 4L19 6"/>
                        </svg>
                        Generar PDF y guardar
                    </button>
                </div>
            </form>
        </div>

        <div id="resolucionSuccess" style="display:none;" class="success-panel">
            <div>
                <svg class="ui-icon ui-icon--large" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m5 12 4 4L19 6"/>
                </svg>
            </div>
            <h3>Resolución guardada</h3>
            <p>El PDF oficial fue generado, integrado al expediente digital y registrado en la bitácora de auditoría.</p>
            <button class="primary-action" type="button" data-modal-close>Volver al expediente</button>
        </div>
    </section>
</div>