package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.adapter.AttendanceAdapter
import com.example.studentmanagementapp.adapter.AttendanceItem
import com.example.studentmanagementapp.databinding.ActivityAttendanceDetailBinding
import com.example.studentmanagementapp.utils.PdfUtils
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

class AttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceDetailBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private lateinit var adapter: AttendanceAdapter
    private var attendanceId: Long = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEdit.setOnClickListener {
            isEditMode = !isEditMode
            adapter.setEditable(isEditMode)

            binding.btnEdit.text =
                if (isEditMode) "Done Editing" else "Edit Attendance"
        }

        attendanceId = intent.getLongExtra("attendanceId", -1)
        if (attendanceId == -1L) {
            Toast.makeText(this, "Invalid attendance record", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = AttendanceAdapter(emptyList())
        binding.rvAttendanceDetail.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceDetail.adapter = adapter

        studentViewModel.students.observe(this) { studentsList ->
            lifecycleScope.launch {
                val records = attendanceViewModel.getAttendanceSnapshot(attendanceId)

                val items = records.mapNotNull { record ->
                    studentsList.find { it.studentId == record.studentOwnerId }
                        ?.let { AttendanceItem(it, record.isPresent) }
                }

                adapter.updateData(items)
            }
        }

        binding.btnGeneratePdf.setOnClickListener {
            generatePdf()
        }
    }

    private fun generatePdf() {
        lifecycleScope.launch {
            val records = attendanceViewModel.getAttendanceSnapshot(attendanceId)
            if (records.isEmpty()) {
                toast("No attendance records found")
                return@launch
            }

            val course = attendanceViewModel.getCourse(records.first().courseOwnerId)
            if (course == null) {
                toast("Course not found")
                return@launch
            }

            val students = studentViewModel.students.value
                ?.filter { it.studentId in records.map { r -> r.studentOwnerId } }
                ?: emptyList()

            val file = PdfUtils.generateAttendancePdf(
                this@AttendanceDetailActivity,
                course,
                students,
                records
            )

            toast(
                if (file != null) "PDF saved: ${file.absolutePath}"
                else "Failed to generate PDF"
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
