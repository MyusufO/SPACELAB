package  com.example.spacelab
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spacelab.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("title")
        val description = intent?.getStringExtra("description")

        if (context != null && title != null && description != null) {
            // Check if the app has the necessary permission before showing the notification
            if (context.checkCallingOrSelfPermission(android.Manifest.permission.WAKE_LOCK) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // NotificationManagerCompat for compatibility with different Android versions
                val notificationManager = NotificationManagerCompat.from(context)

                // Build the notification
                val builder = NotificationCompat.Builder(context, "channelId")
                    .setContentTitle("Reminder: $title")
                    .setContentText(description)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Show the notification
                notificationManager.notify(1, builder.build())
            } else {

                requestWakeLockPermission(context)
            }
        }
    }
    private fun requestWakeLockPermission(context: Context) {
        // Request the WAKE_LOCK permission
        ActivityCompat.requestPermissions(
            context as Activity, // Make sure your context is an Activity
            arrayOf(android.Manifest.permission.WAKE_LOCK),
            MY_PERMISSIONS_REQUEST_WAKE_LOCK
        )
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WAKE_LOCK = 123
    }
}
