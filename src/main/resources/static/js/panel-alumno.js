/* ── Cancelar sesión (alumno) ── */
function abrirModalCancelarAlumno(btn) {
  document.getElementById('alumno-modal-fecha').textContent = btn.dataset.fecha;
  document.getElementById('formCancelarAlumno').action      = '/alumno/cancelar/' + btn.dataset.id;
  document.getElementById('alumnoMotivoSelect').value       = '';
  document.getElementById('alumnoMotivoDetalle').value      = '';
  document.getElementById('alumnoCheckConfirmar').checked   = false;
  toggleBtnAlumno();
}

function toggleBtnAlumno() {
  var btn     = document.getElementById('btnConfirmarAlumno');
  var checked = document.getElementById('alumnoCheckConfirmar').checked;
  btn.disabled      = !checked;
  btn.style.opacity = checked ? '1' : '.5';
  btn.style.cursor  = checked ? 'pointer' : 'not-allowed';
}

function confirmarCancelacionAlumno() {
  var motivo = document.getElementById('alumnoMotivoSelect').value;
  if (!motivo) { document.getElementById('alumnoMotivoSelect').focus(); return; }
  document.getElementById('formCancelarAlumno').submit();
}

/* ── Star rating ── */
function seleccionarEstrella(el) {
  var val = parseInt(el.dataset.val);
  document.getElementById('resena-calificacion').value = val;
  pintarEstrellas(val);
}

function hoverEstrella(el) {
  pintarEstrellas(parseInt(el.dataset.val));
}

function salirEstrella() {
  var val = parseInt(document.getElementById('resena-calificacion').value) || 0;
  pintarEstrellas(val);
}

function pintarEstrellas(upTo) {
  document.querySelectorAll('.estrella-btn').forEach(function(s) {
    s.classList.toggle('activa', parseInt(s.dataset.val) <= upTo);
  });
}

/* ── Populate resena modal on open ── */
var modalResenaEl = document.getElementById('modalResena');
if (modalResenaEl) {
  modalResenaEl.addEventListener('show.bs.modal', function(event) {
    var trigger = event.relatedTarget;
    if (!trigger) return;
    document.getElementById('resena-id-sesion').value          = trigger.dataset.idsesion || '';
    document.getElementById('resena-tutor-nombre').textContent = trigger.dataset.tutor    || '—';
    document.getElementById('resena-calificacion').value       = '';
    pintarEstrellas(0);
  });
}

/* ── Eventos de las estrellas (antes inline en el HTML) ── */
var starRating = document.getElementById('star-rating');
if (starRating) {
  starRating.addEventListener('click', function(e) {
    if (e.target.classList.contains('estrella-btn')) seleccionarEstrella(e.target);
  });
  starRating.addEventListener('mouseover', function(e) {
    if (e.target.classList.contains('estrella-btn')) hoverEstrella(e.target);
  });
  starRating.addEventListener('mouseleave', salirEstrella);
}

/* ── Si el guardado del perfil tuvo errores, se reabre el modal ── */
document.addEventListener('DOMContentLoaded', function() {
  if (document.getElementById('flag-errores-perfil')) {
    new bootstrap.Modal(document.getElementById('modalEditarAlumno')).show();
  }
});
