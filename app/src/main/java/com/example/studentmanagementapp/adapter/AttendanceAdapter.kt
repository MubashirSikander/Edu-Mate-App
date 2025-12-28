package com.example.studentmanagementapp.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.data.entity.Student

data class AttendanceItem(val student: Student, var isPresent: Boolean)

class AttendanceAdapter(
    private var items: List<AttendanceItem>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private val selectedIds = mutableSetOf<Long>()
    private var isEditable = false


    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.item_student)
        val container: LinearLayout = itemView.findViewById(R.id.outer_linear_layout)
        val name: TextView = itemView.findViewById(R.id.tvStudentName)
        val registration: TextView = itemView.findViewById(R.id.tvRegistration)
        val imgRepeater: ImageView = itemView.findViewById(R.id.imgRepeater)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.student.name
        holder.registration.text = item.student.registrationNumber
        holder.imgRepeater.visibility = if (item.student.isRepeater) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = View.GONE

        fun updateBackground(
            container: LinearLayout,
            context: Context,
            isSelected: Boolean
        ) {
            val drawable = container.background?.mutate()
            if (drawable is GradientDrawable) {
                if (isSelected) {
                    drawable.setColor(
                        ContextCompat.getColor(context, R.color.primaryColor)
                    )
                    drawable.setStroke(
                        2,
                        ContextCompat.getColor(context, R.color.primaryColor)
                    )
                } else {
                    drawable.setColor(
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                    drawable.setStroke(
                        2,
                        ContextCompat.getColor(context, R.color.golden_dark_orange)
                    )
                }
            }
        }

        val isSelected = selectedIds.contains(item.student.studentId)
        updateBackground(holder.container, context, isSelected)


        holder.card.setOnClickListener {
            if (!isEditable) return@setOnClickListener

            val id = item.student.studentId
            val nowSelected = if (selectedIds.contains(id)) {
                selectedIds.remove(id)
                false
            } else {
                selectedIds.add(id)
                true
            }

            item.isPresent = nowSelected
            updateBackground(holder.container, context, nowSelected)

            Toast.makeText(
                context,
                "${item.student.name} marked ${if (nowSelected) "Present" else "Absent"}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    fun setEditable(editable: Boolean) {
        isEditable = editable
        notifyDataSetChanged()
    }

    fun getAttendance(): List<AttendanceItem> = items

    fun updateData(newItems: List<AttendanceItem>) {
        items = newItems
        selectedIds.clear()
        notifyDataSetChanged()
    }
}
