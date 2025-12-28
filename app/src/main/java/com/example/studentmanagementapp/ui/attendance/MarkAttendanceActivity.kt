package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.AttendanceAdapter
import com.example.studentmanagementapp.adapter.AttendanceItem
import com.example.studentmanagementapp.adapter.CourseSelectAdapter
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.databinding.ActivityMarkAttendanceBinding
import com.example.studentmanagementapp.utils.PdfUtils
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAttendanceBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()
    private var selectedCourse: Course? = null
    private var adapter = AttendanceAdapter(emptyList())
    private var enrollmentsLiveData: LiveData<List<Enrollment>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Backgrounds
        binding.root.setBackgroundColor(getColor(android.R.color.white))

        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        courseViewModel.courses.observe(this) { courses ->
            if (courses.isNotEmpty() && selectedCourse == null) {
                showCourseSelectorBottomSheet(courses)
            }
        }

        studentViewModel.students.observe(this) {
            if (selectedCourse != null) loadEnrollments()
        }

        binding.btnSaveAttendance.setOnClickListener { saveAttendance() }
        binding.btnGeneratePdf.setOnClickListener { generatePdf() }
    }

    private fun showCourseSelectorBottomSheet(courses: List<Course>) {
        val dialog = BottomSheetDialog(this, R.style.FullScreenBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_select_course, null)
        view.setBackgroundColor(getColor(android.R.color.white))
        dialog.setContentView(view)

        val rvCourses = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(this)

        val adapter = CourseSelectAdapter(courses) { course ->
            selectedCourse = course
            binding.tvSelectedCourse.text = "${course.courseName} (${course.courseCode})"
            loadEnrollments()
            dialog.dismiss()
        }

        rvCourses.adapter = adapter
        dialog.show()
    }

    private fun loadEnrollments() {
        val courseId = selectedCourse?.courseId ?: return
        enrollmentsLiveData?.removeObservers(this)
        enrollmentsLiveData = attendanceViewModel.getEnrollments(courseId)
        enrollmentsLiveData?.observe(this) { enrollments ->
            attendanceViewModel.loadEnrolledStudents(enrollments) { students ->
                val items = enrollments.mapNotNull { enrollment ->
                    students.find { it.studentId == enrollment.studentOwnerId }
                }.map { AttendanceItem(it, false) } // start unselected

                adapter.updateData(items)

                if (items.isEmpty()) {
                    Toast.makeText(this, "No enrolled students for this course yet.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAttendance() {
        val courseId = selectedCourse?.courseId ?: return
        val timestamp = System.currentTimeMillis()
        lifecycleScope.launch {
            adapter.getAttendance().forEach { item ->
                attendanceViewModel.markAttendance(
                    studentId = item.student.studentId,
                    courseId = courseId,
                    isPresent = item.isPresent
                )
            }

            // Toast to show attendance is saved
            Toast.makeText(
                this@MarkAttendanceActivity,
                "Attendance saved successfully at $timestamp",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }


    private fun generatePdf() {
        val course = selectedCourse ?: return
        lifecycleScope.launch {
            val students = studentViewModel.students.value ?: emptyList()
            val attendanceRecords = attendanceViewModel.getAttendanceSnapshot(course.courseId)
            val file = PdfUtils.generateAttendancePdf(this@MarkAttendanceActivity, course, students, attendanceRecords)
            if (file != null) {
//                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
//                Toast.makeText(this, "Unable to create PDF right now.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
