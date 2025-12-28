package com.example.studentmanagementapp.ui.student

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.databinding.ActivityAddStudentBinding
import com.example.studentmanagementapp.viewmodel.StudentViewModel

class AddStudentActivity : AppCompatActivity() {

    private var isEditMode = false
    private var studentId: Long = -1

    private lateinit var binding: ActivityAddStudentBinding
    private val viewModel: StudentViewModel by viewModels()
    val registrationPattern = "^[A-Z]{4}\\d{9}$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isEditMode = intent.getStringExtra("MODE") == "EDIT"
        studentId = intent.getLongExtra("STUDENT_ID", -1)

        if (isEditMode) {
            binding.btnSaveStudent.text = "Update Student"
            binding.titleText.text = "Edit Student"
            loadStudentData()
        }
        binding.etRegistration.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val upper = text.uppercase()

                if (text != upper) {
                    binding.etRegistration.setText(upper)
                    binding.etRegistration.setSelection(upper.length)
                    return
                }

                binding.etRegistration.error =
                    if (upper.isNotEmpty() && !registrationPattern.matches(upper)) {
                        "Format must be AAAA000000000"
                    } else {
                        null
                    }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

//        binding.btnSaveStudent.setOnClickListener {
//            val name = binding.etName.text.toString()
//            val contact = binding.etContact.text.toString()
//            val registration = binding.etRegistration.text.toString()
//            val isRepeater = binding.switchRepeater.isChecked
//
////            if (name.isBlank() || contact.isBlank() || registration.isBlank()) {
////                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
////            } else {
////
////            }
//            if (!registration.matches(registrationPattern)) {
//                // Show error on the input field
//                binding.etRegistration.error = "Invalid Format! Use: AAAA000000000"
//            } else {
//            viewModel.addStudent(name, contact, registration, isRepeater){ result ->
//
//                runOnUiThread {
//                    when (result) {
//
//                        "VALID_DETAILS" -> {
//                            Toast.makeText(
//                                this,
//                                "Please enter valid student details",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                        "DUPLICATE_REGISTRATION" -> {
//                            Toast.makeText(
//                                this,
//                                "Student with this registration number already exists",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                        else -> {
//                            Toast.makeText(
//                                this,
//                                "Student Added successfully",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            finish()
//                        }
//                    }
//                }
//            }
//            }
//        }

//        binding.btnSaveStudent.setOnClickListener {
//            val name = binding.etName.text.toString().trim()
//            val contact = binding.etContact.text.toString().trim()
//            val registration = binding.etRegistration.text.toString().trim()
//            val isRepeater = binding.switchRepeater.isChecked // <- important
//
//            var isValid = true
//
//            // Validate Name
//            if (name.isEmpty()) {
//                binding.etName.error = "Enter student name"
//                isValid = false
//            } else {
//                binding.etName.error = null
//            }
//
//            // Validate Contact (10-11 digits starting with 03)
//            val contactPattern = "^03\\d{9}$".toRegex()
//            if (!contact.matches(contactPattern)) {
//                binding.etContact.error = "Invalid contact number"
//                isValid = false
//            } else {
//                binding.etContact.error = null
//            }
//
//            // Validate Registration
//            val registrationPattern = "^[A-Z]{4}\\d{9}$".toRegex()
//            if (!registration.matches(registrationPattern)) {
//                binding.etRegistration.error = "Invalid format: AAAA000000000"
//                isValid = false
//            } else {
//                binding.etRegistration.error = null
//            }
//
//            if (!isValid) return@setOnClickListener
//
//            // Add student via ViewModel
//            viewModel.addStudent(name, contact, registration, isRepeater) { result ->
//                runOnUiThread {
//                    when (result) {
//                        "VALID_DETAILS" -> Toast.makeText(this, "Enter valid student details", Toast.LENGTH_SHORT).show()
//                        "DUPLICATE_REGISTRATION" -> Toast.makeText(this, "Registration already exists", Toast.LENGTH_SHORT).show()
//                        else -> {
//                            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
//                            finish()
//                        }
//                    }
//                }
//            }
//        }
        binding.btnSaveStudent.setOnClickListener {

            val student = Student(
                studentId = if (isEditMode) studentId else 0,
                name = binding.etName.text.toString(),
                contactNumber = binding.etContact.text.toString(),
                registrationNumber = binding.etRegistration.text.toString(),
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                isRepeater = binding.switchRepeater.isChecked,
                isCR = binding.switchCR.isChecked
            )

            if (isEditMode) {
                viewModel.updateStudent(student) {
                    Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                viewModel.addStudent(
                    student.name,
                    student.contactNumber,
                    student.registrationNumber,
                    student.isRepeater
                ) {
                    Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }


    }

    private fun loadStudentData() {
        viewModel.getStudentById(studentId) { student ->
            student ?: return@getStudentById

            binding.etName.setText(student.name)
            binding.etContact.setText(student.contactNumber)
            binding.etRegistration.setText(student.registrationNumber)
            binding.etEmail.setText(student.email)
            binding.etPassword.setText(student.password)

            binding.switchRepeater.isChecked = student.isRepeater
            binding.switchCR.isChecked = student.isCR

            // IMPORTANT RULES
            binding.etRegistration.isEnabled = false   // 🚫 not editable
        }
    }

}
