// Anticipación mínima para reservar: el tutor necesita tiempo para confirmar.
const HORAS_ANTICIPACION = 1;

// Momento más temprano que se puede agendar: de aquí en adelante todo es válido.
function limiteReserva() {
  return new Date(Date.now() + HORAS_ANTICIPACION * 60 * 60 * 1000);
}

// Pasa una fecha a YYYY-MM-DD en hora local (el formato que usa el input date).
function fechaISO(d) {
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}

// La hora mínima solo limita al primer día disponible; en los días siguientes
// el alumno puede elegir cualquier hora.
function ajustarHoraMin() {
  const inputFecha = document.getElementById('reserva-fecha');
  const inputHora  = document.getElementById('reserva-hora');
  if (!inputFecha || !inputHora) return;

  const limite = limiteReserva();
  if (inputFecha.value === fechaISO(limite)) {
    inputHora.min = String(limite.getHours()).padStart(2, '0') + ':' +
                    String(limite.getMinutes()).padStart(2, '0');
    if (inputHora.value && inputHora.value < inputHora.min) inputHora.value = '';
  } else {
    inputHora.removeAttribute('min');
  }
}

/* ── Formato de los datos de la tarjeta ── */

// Agrupa el número en bloques de 4: "1234 5678 9012 3456" (16 dígitos)
function formatearTarjeta(input) {
  const digitos = input.value.replace(/\D/g, '').slice(0, 16);
  input.value = digitos.replace(/(\d{4})(?=\d)/g, '$1 ');
}

// Inserta el "/" automáticamente: "0625" -> "06/25"
function formatearVencimiento(input) {
  let d = input.value.replace(/\D/g, '').slice(0, 4);
  input.value = d.length >= 3 ? d.slice(0, 2) + '/' + d.slice(2) : d;
}

function soloDigitos(input, max) {
  input.value = input.value.replace(/\D/g, '').slice(0, max);
}

// El vencimiento debe ser MM/AA, con mes válido y que aún no haya expirado
function vencimientoValido(valor) {
  const m = /^(\d{2})\/(\d{2})$/.exec(valor);
  if (!m) return false;
  const mes = parseInt(m[1], 10);
  if (mes < 1 || mes > 12) return false;
  // La tarjeta vence al final de ese mes
  const finDeMes = new Date(2000 + parseInt(m[2], 10), mes, 0, 23, 59, 59);
  return finDeMes >= new Date();
}

// Revisa los datos de la tarjeta antes de enviar; devuelve el mensaje de error o ''
function validarTarjeta() {
  const numero = document.getElementById('reserva-num-tarjeta').value.replace(/\s/g, '');
  const venc   = document.getElementById('reserva-venc-tarjeta').value;
  const cvv    = document.getElementById('reserva-cvv-tarjeta').value;

  if (numero.length !== 16) return 'El número de tarjeta debe tener 16 dígitos.';
  if (!vencimientoValido(venc)) return 'La fecha de vencimiento no es válida o ya expiró.';
  if (cvv.length !== 3) return 'El CVV debe tener 3 dígitos.';
  return '';
}

function seleccionarServicio(btn) {
  var select = document.getElementById('selectServicio');
  if (select) {
    select.value = btn.dataset.idservicio;
    actualizarTotal();
  }
}

function actualizarTotal() {
  const select = document.getElementById('selectServicio');
  const opt = select.options[select.selectedIndex];
  const precio = opt && opt.dataset.precio ? parseFloat(opt.dataset.precio) : 0;
  document.getElementById('reserva-total').textContent = 'S/. ' + precio.toFixed(2);
}

// Muestra los campos del método elegido y ajusta cuáles son obligatorios,
// para que el navegador no valide campos ocultos.
function cambiarMetodoPago() {
  const metodo = document.getElementById('reserva-metodo').value;

  const divYape    = document.getElementById('campos-yape');
  const divTarjeta = document.getElementById('campos-tarjeta');
  divYape.style.display    = metodo === 'Yape'    ? 'block' : 'none';
  divTarjeta.style.display = metodo === 'Tarjeta' ? 'block' : 'none';

  document.getElementById('reserva-celular-yape').required = metodo === 'Yape';
  document.getElementById('reserva-codigo-yape').required  = metodo === 'Yape';
  document.getElementById('reserva-num-tarjeta').required  = metodo === 'Tarjeta';
  document.getElementById('reserva-venc-tarjeta').required = metodo === 'Tarjeta';
  document.getElementById('reserva-cvv-tarjeta').required  = metodo === 'Tarjeta';

  if (metodo !== 'Yape') {
    document.getElementById('reserva-celular-yape').value = '';
    document.getElementById('reserva-codigo-yape').value  = '';
  }
  if (metodo !== 'Tarjeta') {
    document.getElementById('reserva-num-tarjeta').value  = '';
    document.getElementById('reserva-venc-tarjeta').value = '';
    document.getElementById('reserva-cvv-tarjeta').value  = '';
  }
}

document.addEventListener('DOMContentLoaded', function() {
  const select = document.getElementById('selectServicio');
  if (select) select.addEventListener('change', actualizarTotal);

  // Bloquea fechas y horas pasadas en el modal de reserva (igual que en la búsqueda)
  const inputFecha = document.getElementById('reserva-fecha');
  if (inputFecha) {
    // El primer día seleccionable es el del límite (hoy, o mañana si ya es muy tarde)
    inputFecha.min = fechaISO(limiteReserva());
    inputFecha.addEventListener('change', ajustarHoraMin);
  }

  // Formateo automático mientras el alumno escribe los datos de la tarjeta
  const inputNum  = document.getElementById('reserva-num-tarjeta');
  const inputVenc = document.getElementById('reserva-venc-tarjeta');
  const inputCvv  = document.getElementById('reserva-cvv-tarjeta');
  if (inputNum && inputVenc && inputCvv) {
    inputNum.addEventListener('input',  function() { formatearTarjeta(this); });
    inputVenc.addEventListener('input', function() { formatearVencimiento(this); });
    inputCvv.addEventListener('input',  function() { soloDigitos(this, 3); });

    // Antes de enviar, se revisan los datos de la tarjeta en el navegador
    document.getElementById('formReservar').addEventListener('submit', function(e) {
      const cajaError = document.getElementById('error-tarjeta');
      if (document.getElementById('reserva-metodo').value !== 'Tarjeta') {
        cajaError.textContent = '';
        return;
      }
      const error = validarTarjeta();
      cajaError.textContent = error;
      if (error) e.preventDefault();
    });
  }

  // Si la reserva tuvo errores, se reabre el modal con lo que el alumno había llenado
  const datos = document.getElementById('datos-reserva');
  if (datos) {
    if (datos.dataset.idservicio && select) select.value = datos.dataset.idservicio;
    document.getElementById('reserva-fecha').value  = datos.dataset.fecha  || '';
    document.getElementById('reserva-hora').value   = datos.dataset.hora   || '';
    document.getElementById('reserva-metodo').value = datos.dataset.metodo || '';
    cambiarMetodoPago();
    document.getElementById('reserva-celular-yape').value = datos.dataset.celular || '';
    document.getElementById('reserva-codigo-yape').value  = datos.dataset.codigo  || '';
    actualizarTotal();
    new bootstrap.Modal(document.getElementById('modalReservar')).show();
  }
});
