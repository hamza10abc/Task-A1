package com.task.taska1

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
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

        val Next_btn = findViewById<Button>(R.id.Done_btn)
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


        val adv_txt = findViewById<TextView>(R.id.adv_opt)
        val adv_opt_layout = findViewById<RelativeLayout>(R.id.adv_opt_layout)
        val Done_btn = findViewById<Button>(R.id.Done_btn)
        val safeTimeBefore1 = findViewById<EditText>(R.id.Tv_beforeSafeTime)
        val safeTimeAfter1 = findViewById<EditText>(R.id.Tv_AfterSafeTime)

        adv_opt_layout.setVisibility(View.GONE)
        adv_txt.text = "Click to open Advance options"

        adv_txt.setOnClickListener {
            if ( adv_opt_layout.visibility == View.GONE){
                adv_opt_layout.setVisibility(View.VISIBLE)
                adv_txt.text = "Click to close Advance options"
            }
            else{
                adv_opt_layout.setVisibility(View.GONE)
                adv_txt.text = "Click to open Advance options"
            }
        }

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
                val newTask = Task(
                    title = title.toString(),
                    description = description.toString(),
                    fromDate = fromDate.toString(),
                    fromTime = fromTime.toString(),
                    toDate = toDate.toString(),
                    toTime = toTime.toString(),
                    safeTimeBefore = safeTimeBefore1.text.toString(),
                    safeTimeAfter = safeTimeAfter1.text.toString(),
                )

                val clashingTasks = checkForClashingTasks(newTask)

                if (clashingTasks.isNotEmpty()) {
                    // Show dialog with clashing tasks
                    showClashingTasksDialog(clashingTasks, newTask)
                } else {
                    // No clashing tasks, save the new task
                    saveTaskToJson(newTask)
                    Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun showClashingTasksDialog(clashingTasks: List<Task>, newTask: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Clashing Tasks Found")

        val message = StringBuilder()
        for (task in clashingTasks) {
            message.append("Title: ${task.title}\n")
            message.append("From: ${task.fromDate} ${task.fromTime}\n")
            message.append("To: ${task.toDate} ${task.toTime}\n\n")
        }

        builder.setMessage(message.toString())

        // Positive button to dismiss the dialog
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // Just dismiss the dialog
        }

        // Negative button for SYSTEM OVERRIDE to ignore clashing and save the task
        builder.setNegativeButton("SYSTEM OVERRIDE") { dialog, _ ->
            // Save the new task despite clashing
            saveTaskToJson(newTask)
            Toast.makeText(this, "Task saved successfully with SYSTEM OVERRIDE!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        }

        builder.show()
    }



    private fun checkForClashingTasks(newTask: Task): List<Task> {
        val existingTasksJson = readTasksFromJson()
        val clashingTasks = mutableListOf<Task>()

        for (i in 0 until existingTasksJson.length()) {
            val taskJson = existingTasksJson.getJSONObject(i)
            val existingTask = Task(
                title = taskJson.getString("title"),
                description = taskJson.getString("description"),
                fromDate = taskJson.getString("fromDate"),
                fromTime = taskJson.getString("fromTime"),
                toDate = taskJson.getString("toDate"),
                toTime = taskJson.getString("toTime"),
                safeTimeBefore = taskJson.getString("safeTimeBefore"),
                safeTimeAfter = taskJson.getString("safeTimeAfter")
            )

            // Check if newTask overlaps with existingTask
            if (isOverlapping(newTask, existingTask)) {
                clashingTasks.add(existingTask)
            }
        }

        return clashingTasks
    }

    private fun isOverlapping(taskA: Task, taskB: Task): Boolean {
        val taskAStartTime = getDateTime(taskA.fromDate, taskA.fromTime)
        val taskAEndTime = getDateTime(taskA.toDate, taskA.toTime)

        val taskBStartTime = getDateTime(taskB.fromDate, taskB.fromTime)
        val taskBEndTime = getDateTime(taskB.toDate, taskB.toTime)

        // Check for overlap
        return !(taskAEndTime.before(taskBStartTime) || taskAStartTime.after(taskBEndTime))
    }

    private fun getDateTime(date: String, time: String): Date {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.parse("$date $time")!!
    }

    private fun readTasksFromJson(): JSONArray {
        val dir = File(getExternalFilesDir(null), "Taskfiles")
        val taskFile = File(dir, "Task.json")

        return if (taskFile.exists()) {
            val content = taskFile.readText()
            if (content.isNotEmpty()) {
                JSONArray(content)
            } else {
                JSONArray()
            }
        } else {
            JSONArray()
        }
    }



    private fun saveTaskToJson(task: Task) {
        // Directory for Task.json
        val dir = File(getExternalFilesDir(null), "Taskfiles")
        if (!dir.exists()) {
            dir.mkdirs() // Create directory if not exists
        }

        // Task.json file
        val taskFile = File(dir, "Task.json")

        // Read the existing file if it exists, or create a new JSONArray if not
        val jsonArray: JSONArray = if (taskFile.exists()) {
            val content = taskFile.readText()
            if (content.isNotEmpty()) {
                JSONArray(content)
            } else {
                JSONArray()
            }
        } else {
            JSONArray()
        }

        // Create a new JSON object from the task data
        val taskJsonObject = JSONObject().apply {
            put("title", task.title)
            put("description", task.description)
            put("fromDate", task.fromDate)
            put("fromTime", task.fromTime)
            put("toDate", task.toDate)
            put("toTime", task.toTime)
            put("safeTimeBefore", task.safeTimeBefore)
            put("safeTimeAfter", task.safeTimeAfter)
        }

        // Append the new task to the JSON array
        jsonArray.put(taskJsonObject)

        // Write the updated array back to the file
        taskFile.writeText(jsonArray.toString())
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