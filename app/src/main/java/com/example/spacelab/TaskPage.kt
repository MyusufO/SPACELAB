package com.example.spacelab

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskPage : AppCompatActivity() {
    private val tasks = mutableListOf<Task>()

    private val ALARM_REQUEST_CODE = 1
    private val REQUEST_CODE_SET_ALARM = 2  // Replace with an appropriate value
    private val MY_PERMISSIONS_REQUEST_WAKE_LOCK = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_page)
        createNotificationChannel()

        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val btnAddReminder = findViewById<Button>(R.id.btnAddReminder)

        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        btnAddReminder.setOnClickListener {
            if (checkSelfPermission(
                    this,
                    Manifest.permission.SET_ALARM
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SET_ALARM),
                    REQUEST_CODE_SET_ALARM
                )
            } else {
                // Permission already granted, show the add reminder dialog
                showAddReminderDialog()
            }
        }
    }

    private fun showAddTaskDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerDeadline)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()

                // Retrieve the selected date from the DatePicker
                val year = datePicker.year
                val month = datePicker.month
                val day = datePicker.dayOfMonth

                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)

                // Format the date as needed
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val deadline = dateFormat.format(calendar.time)

                tasks.add(Task(title, description, deadline))
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun showAddReminderDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_reminder, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextReminderTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextReminderDescription)
        val linearLayout = dialogView.findViewById<LinearLayout>(R.id.linearLayout)

        // Create NumberPicker for hours
        val numberPickerHours = NumberPicker(this)
        numberPickerHours.minValue = 0
        numberPickerHours.maxValue = 23
        numberPickerHours.wrapSelectorWheel = true
        linearLayout.addView(numberPickerHours)

        // Create NumberPicker for minutes
        val numberPickerMinutes = NumberPicker(this)
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 59
        numberPickerMinutes.wrapSelectorWheel = true
        linearLayout.addView(numberPickerMinutes)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()

                val hours = numberPickerHours.value
                val minutes = numberPickerMinutes.value

                // Format the time as needed
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)

                // Set an alarm for the reminder
                setReminderAlarm(this, title, description, formattedTime)

                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun setReminderAlarm(context: Context, title: String, description: String, time: String) {
        // Parse the time string into hours and minutes
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = timeFormat.parse(time)
        val calendar = Calendar.getInstance()
        calendar.time = date!!

        // Check if the specified time is in the past
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(context, "Please select a future time for the reminder", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an Intent for the AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("description", description)

        // Create a PendingIntent to be triggered when the alarm goes off
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get the AlarmManager service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(context, "Reminder set for $time", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channelId", name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_SET_ALARM -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, show the add reminder dialog
                    showAddReminderDialog()
                } else {
                    // Permission denied, show a rationale if needed or handle accordingly
                    if (shouldShowRequestPermissionRationale(Manifest.permission.SET_ALARM)) {
                        // Explain to the user why the permission is needed and try again
                        Toast.makeText(this, "Permission denied. Please grant the permission to set alarms.", Toast.LENGTH_SHORT).show()
                    } else {
                        // User selected "Don't ask again" or similar, handle accordingly
                        Toast.makeText(this, "Permission denied. Please enable the permission in app settings.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
