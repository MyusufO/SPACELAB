// RecyclerItemClickListener.kt

package com.example.spacelab

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Custom RecyclerView item click listener that implements RecyclerView.OnItemTouchListener
class RecyclerItemClickListener(
    context: Context,
    recyclerView: RecyclerView,
    private val mListener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    // Interface for defining item click callback
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // GestureDetector to detect gestures, in this case, single taps
    private val mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })

    // Initialization block: add this listener to the provided RecyclerView
    init {
        recyclerView.addOnItemTouchListener(this)
    }

    // Called when a touch event is intercepted in the RecyclerView
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // Find the child view at the touched coordinates
        val child = rv.findChildViewUnder(e.x, e.y)

        // Check if there's a listener, the gesture detector detects a single tap, and a child view is present
        if (child != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            // Notify the listener of the item click, providing the clicked view and its position
            mListener.onItemClick(child, rv.getChildAdapterPosition(child))
        }

        // Always return false to allow further handling of touch events by the RecyclerView
        return false
    }

    // Unused method, as we only need to intercept touch events
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    // Unused method, as we don't need to handle disallowing the interception of touch events
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
