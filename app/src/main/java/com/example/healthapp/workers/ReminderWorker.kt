package com.example.healthapp.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthapp.activities.MainActivity

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Gửi thông báo nhắc nhở
        val mainActivity = MainActivity()
        mainActivity.sendNotification("Health Reminder", "Time to take a walk!")
        return Result.success()
    }
}