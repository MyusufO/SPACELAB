package com.example.spacelab

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView



interface NoteActionListener {
    fun onDeleteClicked(note: Note)
    fun onEditClicked(note: Note, view: View)

}

class NotesAdapter(
    private val notesList: List<Note>,
    private val listener: NoteActionListener
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.noteContentTextView)
        val Tagname:TextView=itemView.findViewById(R.id.TagText)
        val optionsButton: Button = itemView.findViewById(R.id.optionsButton)
        val notesContainer: LinearLayout = itemView.findViewById(R.id.notesContainer)

        init {
            optionsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = notesList[position]
                    showOptionsPopup(itemView, note)
                }
            }

            // Set a click listener for the entire note item
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = notesList[position]
                    openViewNoteActivity(itemView.context, note)
                }
            }
        }

        // Function to open the ViewNote activity
        private fun openViewNoteActivity(context: Context, note: Note) {
            val intent = Intent(context, ViewNoteActivity::class.java)
            intent.putExtra("title",note.title)
            context.startActivity(intent)
        }
    }


    private fun showOptionsPopup(view: View, note: Note) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.menu_delete -> {
                    listener.onDeleteClicked(note)
                    true
                }
                R.id.menu_edit -> {
                    listener.onEditClicked(note, view)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]

        // Map color names to color resources
        val colorResourceId = when (note.color) {
            "Red" -> R.color.noteRed
            "Green" -> R.color.noteGreen
            "Blue" -> R.color.noteBlue
            "Yellow" -> R.color.noteYellow
            "Purple" -> R.color.notePurple
            "Orange" -> R.color.noteOrange
            "Pink" -> R.color.notePink
            "Cyan" -> R.color.noteCyan
            "Brown" -> R.color.noteBrown
            "Gray" -> R.color.noteGray
            else -> R.color.noteWhite // You can define a default color
        }

        // Get the color from the resource
        val backgroundColor = ContextCompat.getColor(holder.itemView.context, colorResourceId)

        // Set background color for the LinearLayout
        holder.notesContainer.setBackgroundColor(backgroundColor)
        holder.Tagname.text=note.tag
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
    }

    override fun getItemCount(): Int {
        return notesList.size
    }
}
