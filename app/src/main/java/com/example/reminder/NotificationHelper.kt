package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.SoundPreference

object NotificationHelper {

    const val CHANNEL_ID_DEFAULT = "routine_reminders_default"
    const val CHANNEL_NAME = "Routine Reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your scheduled daily routines and activities"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRoutineReminder(
        context: Context,
        routineId: Long,
        title: String,
        categoryTitle: String,
        timeFormatted: String,
        leadMinutes: Int,
        soundPreference: String,
        vibrate: Boolean
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ROUTINE_ID", routineId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            routineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val message = if (leadMinutes > 0) {
            "আর $leadMinutes মিনিট পরেই শুরু হবে: $timeFormatted ($categoryTitle)"
        } else {
            "এখন শুরু করার সময়: $timeFormatted ($categoryTitle)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\nপ্রস্তুত হন এবং আপনার সারাদিনের লক্ষ্য পূরণ করুন!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(routineId.toInt(), notification)

        // Custom Sound & Vibration according to preference
        playToneAndVibrate(context, soundPreference, vibrate)
    }

    private fun playToneAndVibrate(context: Context, soundPreferenceId: String, vibrate: Boolean) {
        val soundPref = SoundPreference.fromId(soundPreferenceId)

        // Vibration
        if (vibrate) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 250, 150, 350),
                                -1
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 250, 150, 350), -1)
                    }
                }
            } catch (_: Exception) {}
        }

        // Sound tone
        if (soundPref != SoundPreference.SILENT) {
            try {
                Thread {
                    try {
                        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                        toneGen.startTone(soundPref.toneType, 700)
                        Thread.sleep(800)
                        toneGen.release()
                    } catch (_: Exception) {
                        // Fallback to system default ringtone
                        try {
                            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            val r = RingtoneManager.getRingtone(context, alert)
                            r.play()
                        } catch (_: Exception) {}
                    }
                }.start()
            } catch (_: Exception) {}
        }
    }
}
