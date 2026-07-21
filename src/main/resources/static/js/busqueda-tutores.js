// Fecha de hoy en formato YYYY-MM-DD (hora local), para el atributo min del input date.
function hoyISO() {
  const d = new Date();
  const off = d.getTimezoneOffset();
  return new Date(d.getTime() - off * 60000).toISOString().slice(0, 10);
}

// Si la fecha elegida es hoy, no deja elegir una hora ya pasada; otro día, sin límite.
function ajustarHoraMin() {
  const inputFecha = document.getElementById('reserva-fecha');
  const inputHora  = document.getElementById('reserva-hora');
  if (inputFecha.value === hoyISO()) {
    const ahora = new Date();
    inputHora.min = String(ahora.getHours()).padStart(2, '0') + ':' +
                    String(ahora.getMinutes()).padStart(2, '0');
    if (inputHora.value && inputHora.value < inputHora.min) inputHora.value = '';
  } else {
    inputHora.removeAttribute('min');
  }
}

function abrirReserva(idTutor, nombreTutor) {
  document.getElementById('reserva-tutor-nombre').textContent = nombreTutor;

  const select = document.getElementById('reserva-servicios');
  select.innerHTML = '';

  const servicios = serviciosPorTutor[idTutor] || [];
  servicios.forEach(function(s) {
    const opt = document.createElement('option');
    opt.value = s.id;
    opt.dataset.precio = s.precio;
    opt.textContent = (nombresCurso[s.idCurso] || 'Curso') + ' — S/' + s.precio;
    select.appendChild(opt);
  });

  if (servicios.length === 0) {
    const opt = document.createElement('option');
    opt.value = '';
    opt.textContent = 'Este tutor no tiene servicios disponibles';
    select.appendChild(opt);
  }

  const inputFecha = document.getElementById('reserva-fecha');
  inputFecha.value = '';
  inputFecha.min = hoyISO(); // impide elegir fechas pasadas
  document.getElementById('reserva-hora').value  = '';
  ajustarHoraMin(); // impide elegir una hora pasada si la fecha es hoy

  // Reinicia la sección de pago
  document.getElementById('reserva-metodo').value = '';
  cambiarMetodoPago();
  actualizarTotal();
}

function actualizarTotal() {
  const select = document.getElementById('reserva-servicios');
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
  document.getElementById('reserva-servicios').addEventListener('change', actualizarTotal);
  document.getElementById('reserva-fecha').addEventListener('change', ajustarHoraMin);

  // Si la reserva tuvo errores, se reabre el modal con lo que el alumno había llenado
  const datos = document.getElementById('datos-reserva');
  if (datos) {
    const idTutor = datos.dataset.idtutor;
    if (idTutor) {
      const btn = document.querySelector('button[data-id="' + idTutor + '"]');
      abrirReserva(idTutor, btn ? btn.dataset.nombre : '');
    }
    if (datos.dataset.idservicio) {
      document.getElementById('reserva-servicios').value = datos.dataset.idservicio;
    }
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
