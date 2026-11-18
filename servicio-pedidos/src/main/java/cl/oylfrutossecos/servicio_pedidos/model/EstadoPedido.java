package cl.oylfrutossecos.servicio_pedidos.model;

public enum EstadoPedido {
    PENDIENTE_VALIDACION("Pendiente"),
    CONFIRMADO("Confirmado"),
    PREPARADO("Preparado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
