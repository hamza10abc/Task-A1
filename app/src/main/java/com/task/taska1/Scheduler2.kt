package com.task.taska1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
//import kotlinx.android.synthetic.main.scheduler2.*
import java.util.*

class Scheduler2 : AppCompatActivity() {

    private var fromDate: String? = null
    private var fromTime: String? = null
    private var toDate: String? = null
    private var toTime: String? = null
    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler2)


        // Get values from Scheduler1
        title = intent.getStringExtra("Title")
        description = intent.getStringExtra("Description")

//        val classButton: Button = findViewById(R.id.button_class)

        val From_Date_crd: CardView = findViewById(R.id.From_Date_crd)
        val From_Time_crd: CardView = findViewById(R.id.From_Time_crd)
        val To_Date_crd: CardView = findViewById(R.id.To_Date_crd)
        val To_Time_crd: CardView = findViewById(R.id.To_Time_crd)
        val Tv_fromDate: TextView = findViewById(R.id.Tv_fromDate)
        val Tv_fromTime: TextView = findViewById(R.id.Tv_fromTime)
        val Tv_ToDate: TextView = findViewById(R.id.Tv_ToDate)
        val Tv_toTime: TextView = findViewById(R.id.Tv_toTime)
        val Tv_Title: TextView = findViewById(R.id.Tv_Title)

        Tv_Title.text = title

        // Handle From Date selection
        From_Date_crd.setOnClickListener {
            showDatePickerDialog { date ->
                fromDate = date
                Tv_fromDate.text = fromDate

                toDate = date
                Tv_ToDate.text = toDate
            }
        }

        // Handle From Time selection
        From_Time_crd.setOnClickListener {
            showTimePickerDialog { time ->
                fromTime = time
                Tv_fromTime.text = fromTime
            }
        }

        // Handle To Date selection
        To_Date_crd.setOnClickListener {
            showDatePickerDialog { date ->
                toDate = date
                Tv_ToDate.text = toDate
            }
        }

        // Handle To Time selection
        To_Time_crd.setOnClickListener {
            showTimePickerDialog { time ->
                toTime = time
                Tv_toTime.text = toTime
            }
        }

        // Handle Next button click
        Next_btn.setOnClickListener {
            if (validateInputs()) {
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }


        }


    }

    // Function to show date picker dialog
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                onDateSelected(date)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // Function to show time picker dialog
    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(time)
            },
            hour, minute, false
        )
        timePickerDialog.show()
    }

    // Function to validate if all inputs are selected
    private fun validateInputs(): Boolean {
        return !fromDate.isNullOrEmpty() &&
                !fromTime.isNullOrEmpty() &&
                !toDate.isNullOrEmpty() &&
                !toTime.isNullOrEmpty()
    }

}
