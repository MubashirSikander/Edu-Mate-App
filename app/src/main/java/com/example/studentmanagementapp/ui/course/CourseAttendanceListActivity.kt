package com.example.studentmanagementapp.ui.attendance

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.adapter.AttendanceListAdapter
import com.example.studentmanagementapp.databinding.ActivityCourseAttendanceListBinding
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
class CourseAttendanceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseAttendanceListBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private var courseId: Long = -1
    private lateinit var adapter: AttendanceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseAttendanceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getLongExtra("courseId", -1)

        adapter = AttendanceListAdapter(emptyList()) { attendanceRecord ->
            val intent = Intent(this, AttendanceDetailActivity::class.java)
            intent.putExtra("attendanceId", attendanceRecord.attendanceId)
            startActivity(intent)
        }

        binding.rvAttendanceRecords.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceRecords.adapter = adapter

        lifecycleScope.launch {
            val attendanceRecords = attendanceViewModel.getAttendanceSnapshot(courseId)
            adapter.updateData(attendanceRecords)
        }
    }
}
