function abrirModalCancelar(btn) {
  document.getElementById('modal-fecha').textContent = btn.dataset.fecha;
  document.getElementById('formCancelar').action     = '/tutor/cancelar/' + btn.dataset.id;

  document.getElementById('motivoSelect').value   = '';
  document.getElementById('motivoDetalle').value  = '';
  document.getElementById('checkConfirmar').checked = false;
  toggleBtnConfirmar();
}

function toggleBtnConfirmar() {
  const btn     = document.getElementById('btnConfirmarCancelacion');
  const checked = document.getElementById('checkConfirmar').checked;
  btn.disabled      = !checked;
  btn.style.opacity = checked ? '1' : '.5';
  btn.style.cursor  = checked ? 'pointer' : 'not-allowed';
}

function confirmarCancelacion() {
  const motivo = document.getElementById('motivoSelect').value;
  if (!motivo) {
    document.getElementById('motivoSelect').focus();
    return;
  }
  document.getElementById('formCancelar').submit();
}

// Muestra los campos del método de retiro elegido (Yape o cuenta bancaria)
// y limpia los del método que quedó oculto.
function cambiarMetodoRetiro() {
  const metodo = document.getElementById('retiro-metodo').value;

  document.getElementById('retiro-campos-yape').style.display   = metodo === 'Yape' ? 'block' : 'none';
  document.getElementById('retiro-campos-cuenta').style.display = metodo === 'CuentaBancaria' ? 'block' : 'none';

  if (metodo !== 'Yape') document.getElementById('retiro-celular-yape').value = '';
  if (metodo !== 'CuentaBancaria') {
    document.getElementById('retiro-banco').value  = '';
    document.getElementById('retiro-cuenta').value = '';
  }
}

/* ── Reapertura de modales tras errores del servidor (flags del HTML) ── */
document.addEventListener('DOMContentLoaded', function() {
  // El guardado del perfil tuvo errores: se reabre el modal para mostrarlos
  if (document.getElementById('flag-errores-perfil')) {
    new bootstrap.Modal(document.getElementById('modalEditarPerfil')).show();
  }

  // El retiro tuvo errores: se reabre el modal con lo que el tutor había llenado
  const datosRetiro = document.getElementById('datos-retiro');
  if (datosRetiro) {
    document.getElementById('retiro-monto').value  = datosRetiro.dataset.monto  || '';
    document.getElementById('retiro-metodo').value = datosRetiro.dataset.metodo || '';
    cambiarMetodoRetiro();
    document.getElementById('retiro-celular-yape').value = datosRetiro.dataset.celular || '';
    document.getElementById('retiro-banco').value        = datosRetiro.dataset.banco   || '';
    document.getElementById('retiro-cuenta').value       = datosRetiro.dataset.cuenta  || '';
    new bootstrap.Modal(document.getElementById('modalRetirar')).show();
  }
});
