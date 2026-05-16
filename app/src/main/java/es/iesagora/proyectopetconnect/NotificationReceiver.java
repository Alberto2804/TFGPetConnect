package es.iesagora.proyectopetconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import es.iesagora.proyectopetconnect.R;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String titulo = intent.getStringExtra("titulo");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "agenda_channel")
                .setSmallIcon(R.drawable.petconnect) // Asegúrate de tener este icono
                .setContentTitle("Tu Agenda PetConnect")
                .setContentText(titulo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}