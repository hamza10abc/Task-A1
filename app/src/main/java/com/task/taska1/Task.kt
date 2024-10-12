package com.task.taska1

// Task.kt
class Task(
    val title: String,
    val description: String,
    val fromDate: String,
    val toDate: String,
    val fromTime: String,
    val toTime: String,
    val safeTimeBefore: String,
    val safeTimeAfter: String,
    var countDownTime: String? = null // Add this property if not already present
)



