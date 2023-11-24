package com.example.spacelab

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

interface OnNoteSavedListener {
    fun onNoteSaved()
}

class TaskAdapter(val checkboxList: MutableList<TaskList>, private val onNoteSavedListener: OnNoteSavedListener, private val isViewNoteActivity: Boolean = false) :
    RecyclerView.Adapter<TaskAdapter.CheckboxViewHolder>() {

    // ViewHolder class to hold the views for each item in the RecyclerView
    class CheckboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Time: TextView = itemView.findViewById(R.id.TextView)
        val button: Button = itemView.findViewById(R.id.dateSwitch)
        val Description: EditText = itemView.findViewById(R.id.description)
    }

    // Create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckboxViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkbox, parent, false)
        return CheckboxViewHolder(itemView)
    }

    // Bind data to the views within the ViewHolder
    override fun onBindViewHolder(holder: CheckboxViewHolder, position: Int) {
        val currentItem = checkboxList[position]

        // Set description text
        holder.Description.setText(currentItem.label)

        // Set datetime text and handle visibility based on switch state
        holder.Time.text = currentItem.datetime
        holder.button.setOnClickListener {
            showDateTimePicker(position, holder.itemView.context)
        }

        // Hide the button if it's the ViewNoteActivity
        if (isViewNoteActivity) {
            holder.button.visibility = View.GONE
        }
    }

    // Return the number of items in the list
    override fun getItemCount() = checkboxList.size

    // Remove item from the list and notify the adapter
    fun removeItem(position: Int) {
        checkboxList.removeAt(position)
        notifyItemRemoved(position)
    }

    // Add item to the list and notify the adapter
    fun addItem(item: TaskList) {
        checkboxList.add(item)
        notifyItemInserted(checkboxList.size - 1)
    }

    // Show date picker dialog
    private fun showDateTimePicker(position: Int, context: Context) {
        val calendar = Calendar.getInstance()

        // Date Picker
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Handle the selected date
                calendar.set(year, month, dayOfMonth)
                showTimePicker(position, context, calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Show time picker dialog
    private fun showTimePicker(position: Int, context: Context, calendar: Calendar) {
        // Time Picker
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Handle the selected time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Check if the selected date and time are in the past
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    // Show an error message or handle the situation accordingly
                    checkboxList[position].isChecked = false
                    Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                } else {
                    // Do something with the selected date and time
                    val formattedDateTime =
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(calendar.time)
                    checkboxList[position].isChecked = true
                    // Update your UI or perform any other actions with the selected date and time
                    checkboxList[position].datetime = formattedDateTime
                    notifyItemChanged(position)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    // Function to enable swipe-to-delete
    fun enableSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                removeItem(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
