package com.aayush.shoppingapp.common.helpers

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aayush.shoppingapp.R
import kotlin.math.max
import kotlin.math.min

class SwipeHelper(
    val context: Context,
    val onDeleteSwipe: (viewHolder: ViewHolder, direction: Int) -> Unit
) : Callback() {


    private var mContext: Context? = null
    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)
    private var intrinsicWidth = deleteIcon?.intrinsicWidth
    private var intrinsicHeight = deleteIcon?.intrinsicHeight
    private var background = ColorDrawable()
    private var mClearPaint: Paint? = null
    private var backgroundColor = ContextCompat.getColor(context, R.color.swipe_background_color)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    init {
        mContext = context
        background = ColorDrawable()
        backgroundColor = ContextCompat.getColor(context, R.color.swipe_background_color)
        mClearPaint = Paint()
        mClearPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        intrinsicWidth = deleteIcon?.intrinsicWidth
        intrinsicHeight = deleteIcon?.intrinsicHeight
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if(isSwipeEnabled()) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(
                    c,
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }

            // Draw the red delete background
            background.color = backgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // Calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - (intrinsicHeight ?: 0)) / 2
            val deleteIconMargin = (itemHeight - (intrinsicHeight ?: 0)) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - (intrinsicWidth ?: 0)
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + (intrinsicHeight ?: 0)

            // Draw the delete icon
            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
        return 0.7f;
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        onDeleteSwipe(viewHolder, direction)
    }

    private fun isSwipeEnabled(): Boolean {
        return true
    }
}