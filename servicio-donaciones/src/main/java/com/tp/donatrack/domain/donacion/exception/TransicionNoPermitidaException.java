package com.tp.donatrack.domain.donacion.exception;

public class TransicionNoPermitidaException extends IllegalStateException {

    public TransicionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    public TransicionNoPermitidaException(String estadoActual, String accionIntentada) {
        super("No es posible realizar la acción '" + accionIntentada + "' en el estado actual: " + estadoActual);
    }
}
