package cl.oylfrutossecos.app.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificacionHelper {

    const val CANAL_ID = "canal_oyl_notificaciones"
    const val CANAL_NOMBRE = "Notificaciones OYL Frutos Secos"
    const val CANAL_DESCRIPCION = "Alertas de pedidos, compras y estado de inventario"

    fun inicializarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                CANAL_NOMBRE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CANAL_DESCRIPCION
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun enviarNotificacion(
        context: Context,
        titulo: String,
        mensaje: String,
        notificacionId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        // En Android 13+ (API 33) se debe verificar el permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) {
                // Si no hay permiso a nivel de SO, la notificación local ya quedó registrada en SQLite
                return
            }
        }

        try {
            val builder = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificacionId, builder.build())
        } catch (_: SecurityException) {
            // Ignorar de forma segura si falta el permiso en ejecución
        }
    }
}