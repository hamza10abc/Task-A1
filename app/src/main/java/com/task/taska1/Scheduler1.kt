package com.task.taska1

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Scheduler1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler1)

        // Button references
        val classButton: Button = findViewById(R.id.button_class)
        val workButton: Button = findViewById(R.id.button_work)
        val meetingButton: Button = findViewById(R.id.button_meeting)
        val otherButton: Button = findViewById(R.id.button_other)

        // Click listeners for the buttons
        classButton.setOnClickListener { showDescriptionDialog("Class") }
        workButton.setOnClickListener { showDescriptionDialog("Work") }
        meetingButton.setOnClickListener { showDescriptionDialog("Meeting") }
        otherButton.setOnClickListener { showOtherDialog() }

    }

    private fun showDescriptionDialog(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Description")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Done") { dialog: DialogInterface, _: Int ->
            val description = input.text.toString()
            navigateToScheduler2(title, description)
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showOtherDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Title")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Done") { dialog: DialogInterface, _: Int ->
            val title = input.text.toString()
            showDescriptionDialog(title)
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.cancel() }
        builder.show()
    }

    private fun navigateToScheduler2(title: String, description: String) {
        val intent = Intent(this, Scheduler2::class.java)
        intent.putExtra("Title", title)
        intent.putExtra("Description", description)
        startActivity(intent)
    }

}