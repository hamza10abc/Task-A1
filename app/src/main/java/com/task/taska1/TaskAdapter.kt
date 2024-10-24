package com.task.taska1

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private val taskList: ArrayList<Task>,
    private val onDeleteClickListener: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder to hold references to the views
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.Tv_Title)
        val description1: TextView = itemView.findViewById(R.id.Tv_Description1)
        val description2: TextView = itemView.findViewById(R.id.Tv_Description2)
        val timeRange: TextView = itemView.findViewById(R.id.Tv_Time_Range)
        val countDownTime: TextView = itemView.findViewById(R.id.Tv_Count_Down_Time)
        val descriptionLayout1: LinearLayout = itemView.findViewById(R.id.Tv_Description_layout1)
        val descriptionLayout2: LinearLayout = itemView.findViewById(R.id.Tv_Description_layout2)
        val deleteButton: Button = itemView.findViewById(R.id.delete_btn)
    }

    // OnCreateViewHolder - inflating the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    // OnBindViewHolder - binding data and handling click events
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Set initial data
        holder.title.text = task.title
        holder.description1.text = task.description
        holder.description2.text = task.description

        // Format the date from DD/MM/YYYY to 12 Oct 2024
        val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val formattedFromDate = outputDateFormat.format(inputDateFormat.parse(task.fromDate))
        val formattedToDate = outputDateFormat.format(inputDateFormat.parse(task.toDate))

        // Format the time from 24-hour to 12-hour format
        val inputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val formattedFromTime = outputTimeFormat.format(inputTimeFormat.parse(task.fromTime))
        val formattedToTime = outputTimeFormat.format(inputTimeFormat.parse(task.toTime))

//        holder.timeRange.text = "${task.fromDate} - ${task.toDate}  |  ${task.fromTime} - ${task.toTime}"
        holder.timeRange.text = "$formattedToDate - $formattedFromDate   |   $formattedFromTime - $formattedToTime"

        // Initially, DescriptionLayout1 is visible, DescriptionLayout2 is GONE
        holder.descriptionLayout1.visibility = View.VISIBLE
        holder.descriptionLayout2.visibility = View.GONE
        holder.deleteButton.visibility = View.GONE

        // Click listener to toggle visibility of the layouts
        holder.itemView.setOnClickListener {
            if (holder.descriptionLayout1.visibility == View.VISIBLE) {
                holder.descriptionLayout1.visibility = View.GONE
                holder.descriptionLayout2.visibility = View.VISIBLE
                holder.deleteButton.visibility = View.VISIBLE
            } else {
                holder.descriptionLayout1.visibility = View.VISIBLE
                holder.descriptionLayout2.visibility = View.GONE
                holder.deleteButton.visibility = View.GONE
            }
        }

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(task)  // Trigger the listener when delete is clicked
        }

        // Start the countdown timer
        startCountDown(task.fromDate, task.fromTime, task.toDate, task.toTime, holder.countDownTime, task)



    }

    // Get the number of items in the list
    override fun getItemCount(): Int {
        return taskList.size
    }

    private fun startCountDown(fromDate: String, fromTime: String, toDate: String, toTime: String, countDownTextView: TextView, task: Task) {
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        try {
            val taskStartDate = dateTimeFormat.parse("$fromDate $fromTime")
            val taskEndDate = dateTimeFormat.parse("$toDate $toTime")
            val handler = Handler(Looper.getMainLooper())

            handler.post(object : Runnable {
                override fun run() {
                    val currentTime = Date()
                    val startTimeDiff = taskStartDate.time - currentTime.time
                    val endTimeDiff = taskEndDate.time - currentTime.time

                    if (startTimeDiff > 0) {
                        // Countdown for task start time
                        val countDownText = formatTimeDiff(startTimeDiff, "Starting in")
                        countDownTextView.text = countDownText
                    } else if (endTimeDiff > 0) {
                        // Countdown for task end time
                        val countDownText = formatTimeDiff(endTimeDiff, "Ending in")
                        countDownTextView.text = countDownText
                    } else {
                        countDownTextView.text = "Task ended"
                        task.countDownTime = "Task ended" // Update the task status
                        return // Exit the countdown loop when the task has ended
                    }

                    // Update the Task instance with the current countdown text
                    task.countDownTime = countDownTextView.text.toString()

                    // Update every second
                    handler.postDelayed(this, 1000)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            countDownTextView.text = "Invalid time"
        }
    }

    private fun formatTimeDiff(diff: Long, prefix: String): String {
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return if (days > 0) {
            String.format("%s -> %02d:%02d:%02d:%02d", prefix, days, hours, minutes, seconds)
        } else {
            String.format("%s -> %02d:%02d:%02d", prefix, hours, minutes, seconds)
        }
    }




}
