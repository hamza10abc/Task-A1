package com.task.taska1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import kotlinx.android.synthetic.main.scheduler3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Scheduler3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler3)


        // Retrieve data from previous intents (Scheduler1.kt and Scheduler2.kt)
        val title = intent.getStringExtra("Title").toString()
        val description = intent.getStringExtra("Description").toString()
        val fromDate = intent.getStringExtra("From_Date").toString()
        val fromTime = intent.getStringExtra("From_Time").toString()
        val toDate = intent.getStringExtra("To_Date").toString()
        val toTime = intent.getStringExtra("To_Time").toString()

        val Done_btn = findViewById<Button>(R.id.Done_btn)
        val safeTimeBefore1 = findViewById<EditText>(R.id.Tv_beforeSafeTime)
        val safeTimeAfter1 = findViewById<EditText>(R.id.Tv_AfterSafeTime)

        Done_btn.setOnClickListener {
            // Get the safe time values from the input fields
            val safeTimeBefore = safeTimeBefore1.text.toString()
            val safeTimeAfter = safeTimeAfter1.text.toString()

            // Validate the inputs
            if (safeTimeBefore != null && safeTimeAfter != null) {
                // Create the Task object
                val task = Task(
                    title = title,
                    description = description,
                    fromDate = fromDate,
                    fromTime = fromTime,
                    toDate = toDate,
                    toTime = toTime,
                    safeTimeBefore = safeTimeBefore,
                    safeTimeAfter = safeTimeAfter,
                )

                // Save the task to Task.json
                saveTaskToJson(task)

                // Optionally, show a message
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to the next activity or finish
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Please fill in both safe time values", Toast.LENGTH_SHORT).show()
            }
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
}