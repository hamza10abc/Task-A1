package com.task.taska1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.google.gson.Gson
import android.util.Log
import android.widget.Button
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    lateinit var taskList: ArrayList<Task>
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: TaskAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        val scheduleButton: Button = findViewById(R.id.Schedule_btn)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, Scheduler1::class.java)
            startActivity(intent)
        }

        readTasksFromFile()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load the JSON file from media directory
        taskList = readJsonFile() ?: ArrayList()

        // Initialize the adapter before sorting
        adapter = TaskAdapter(taskList) { task ->
            deleteTask(task)  // This will handle the delete functionality
        }
        recyclerView.adapter = adapter

        // Sort the task list by date and time after adapter is initialized
        sortTasksByFromDateAndTime()

        // Start the automatic deletion check
//        startAutoDeletionCheck()

        swipeRefreshLayout.setOnRefreshListener {
            refreshTaskList() // Call the method to refresh the task list
            Log.d("refresh", "Refresheddeded") // Log to confirm save
        }
    }
    // Method to refresh the task list
//    private fun refreshTaskList() {
//
//        checkForEndedTasks_new()
//
//        // Reload tasks from the JSON file
//        taskList.clear() // Clear the current task list
//        taskList.addAll(readJsonFile() ?: ArrayList()) // Re-populate with fresh data
//
//        // Notify the adapter of data changes
//        adapter.notifyDataSetChanged()
//
//
//        // Stop the refreshing animation
//        swipeRefreshLayout.isRefreshing = false
//
//
//    }

    private fun refreshTaskList() {
        Log.d("TaskListBeforeRefresh", "Current tasks: ${taskList.map { it.title to it.countDownTime }}")
        checkForEndedTasks_new()

        // Reload tasks from the JSON file
        taskList.clear() // Clear the current task list
        taskList.addAll(readJsonFile() ?: ArrayList()) // Re-populate with fresh data

        // Sort the newly loaded task list
        sortTasksByFromDateAndTime()

        // Notify the adapter of data changes
        adapter.notifyDataSetChanged()

        // Stop the refreshing animation
        swipeRefreshLayout.isRefreshing = false
    }



//    -----------------------AUTO DELETING FUNCTIONALITY BELOW------------------------------------------------

// -----------------------AUTO DELETING FUNCTIONALITY BELOW------------------------------------------------

    private fun startAutoDeletionCheck() {
        handler.post(object : Runnable {
            override fun run() {
//                checkForEndedTasks()  // Check for tasks that have ended
                checkForEndedTasks_new()
                handler.postDelayed(this, 60000) // Re-run every minute
            }
        })
    }

    // Method to check if any task has ended
    private fun checkForEndedTasks() {
        val iterator = taskList.iterator()

        while (iterator.hasNext()) {
            val task = iterator.next()

            // Assuming the TaskAdapter provides a way to get the current countdown status
            val taskCountDownText = task.countDownTime  // Retrieve the countdown text from your task model

            if (taskCountDownText == "Task ended") {
                Log.d("TaskAutoDelete", "Task has ended. Deleting task: ${task.title}")
                iterator.remove()  // Remove ended task from list
            }
        }

        // Notify the adapter of the changed data
        adapter.notifyDataSetChanged()
        saveTasksToFile()  // Save updated task list back to the file
    }

    private fun checkForEndedTasks_new() {
        val tasksToDelete = mutableListOf<Task>()

        for (task in taskList) {
            if (task.countDownTime == "Task ended") {
                Log.d("TaskAutoDelete", "Task has ended. Marking for deletion: ${task.title}")
                tasksToDelete.add(task)
            }
        }

        for (task in tasksToDelete) {
            deleteTask(task)
            Log.d("TaskAutoDelete", "Task deleted: ${task.title}")
        }
    }



// -----------------------AUTO DELETING FUNCTIONALITY ABOVE------------------------------------------------
//    -----------------------AUTO DELETING FUNCTIONALITY ABOVE------------------------------------------------



    // Sorting function to sort tasks by From_Date and From_Time
    private fun sortTasksByFromDateAndTime() {
        // This format matches the task's date and time structure
        val dateFormat = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault()) // Adjust for 'd/M/yyyy HH:mm' format

        taskList.sortWith { task1, task2 ->
            try {
                // Combine fromDate and fromTime fields for both tasks
                val task1StartDate = dateFormat.parse("${task1.fromDate} ${task1.fromTime}")
                val task2StartDate = dateFormat.parse("${task2.fromDate} ${task2.fromTime}")

                Log.d("TaskSorting", "Comparing Task 1: $task1StartDate with Task 2: $task2StartDate")

                // Compare the two dates and return the appropriate value for sorting
                when {
                    task1StartDate == null -> 1 // If task1 date is null, move task2 earlier
                    task2StartDate == null -> -1 // If task2 date is null, move task1 earlier
                    else -> task1StartDate.compareTo(task2StartDate) // Compare the parsed dates
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                0 // In case of error, consider the tasks equal
            }
        }

        // Notify the adapter that the task list has changed
        adapter.notifyDataSetChanged()
    }



    private fun getAppMediaDir(): File? {
        val mediaDir = File(getExternalFilesDir(null), "Taskfiles")
        if (!mediaDir.exists()) {
            mediaDir.mkdirs() // Create directory if it doesn't exist
        }
        return mediaDir
    }

    private fun readJsonFile(): ArrayList<Task>? {
        val mediaDir = getAppMediaDir()
        val file = File(mediaDir, "Task.json")
        if (!file.exists()) return null

        val jsonString = file.readText()
        val gson = Gson()
        val taskArray = gson.fromJson(jsonString, Array<Task>::class.java)
        return ArrayList(taskArray.toList())
    }

    fun readTasksFromFile() {
        val filePath = File(getExternalFilesDir("Media"), "Task.json")

        // Check if the file exists
        if (filePath.exists()) {
            Log.d("TaskFile", "File exists: ${filePath.absolutePath}")

            // Read the file contents
            try {
                val jsonContent = filePath.readText()
                Log.d("TaskFile", "File content: $jsonContent")
                // You can now parse the jsonContent to your data model
            } catch (e: Exception) {
                Log.e("TaskFile", "Error reading file: ${e.message}")
            }
        } else {
            Log.e("TaskFile", "File does not exist.")
        }
    }

    // Method to delete a task
    private fun deleteTask(task: Task) {
        taskList.remove(task)
        adapter.notifyDataSetChanged() // Notify adapter about changes
        saveTasksToFile() // Save the updated task list back to the file
    }

    // Method to save updated tasks to the JSON file
    private fun saveTasksToFile() {
        val mediaDir = getAppMediaDir()
        val file = File(mediaDir, "Task.json")
        val gson = Gson()
        val jsonString = gson.toJson(taskList)
        file.writeText(jsonString)
        Log.d("TaskFile1", "Tasks saved to file: $jsonString") // Log to confirm save
    }
}
