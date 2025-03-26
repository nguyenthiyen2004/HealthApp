package com.example.healthapp.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthapp.R
import com.example.healthapp.fragments.DashboardFragment
import com.example.healthapp.fragments.StepsFragment
import com.example.healthapp.fragments.SleepFragment
import com.example.healthapp.fragments.HeartRateFragment
import com.example.healthapp.utils.NetworkUtils
import com.example.healthapp.utils.SecureStorage
import com.example.healthapp.utils.ThemeUtils
import com.example.healthapp.workers.ReminderWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kiểm tra trạng thái mạng
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
        }

        // Theo dõi trạng thái mạng theo thời gian thực
        registerNetworkCallback()

        // Áp dụng chế độ sáng/tối khi khởi động ứng dụng
        ThemeUtils.applyTheme(ThemeUtils.isDarkMode(this))

        // Tạo kênh thông báo
        createNotificationChannel()

        // Lên lịch công việc định kỳ
        scheduleReminder()

        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_steps -> loadFragment(StepsFragment())
                R.id.nav_sleep -> loadFragment(SleepFragment())
                R.id.nav_heart_rate -> loadFragment(HeartRateFragment())
            }
            true
        }

        // Load default fragment
        loadFragment(DashboardFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Tạo kênh thông báo
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "health_reminder_channel", // ID của kênh
                "Health Reminders", // Tên hiển thị của kênh
                NotificationManager.IMPORTANCE_HIGH // Mức độ ưu tiên của thông báo
            ).apply {
                description = "Channel for health reminders" // Mô tả kênh
            }

            // Đăng ký kênh với hệ thống
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Lên lịch công việc định kỳ
    private fun scheduleReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    // Hàm gửi thông báo nhắc nhở
    fun sendNotification(title: String, message: String) {
        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Notification permission not granted!", Toast.LENGTH_SHORT).show()
            return
        }

        // Intent để mở ứng dụng khi người dùng nhấn vào thông báo
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Xây dựng thông báo
        val builder = NotificationCompat.Builder(this, "health_reminder_channel")
            .setSmallIcon(R.drawable.ic_notification) // Đảm bảo biểu tượng tồn tại
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Hiển thị thông báo bằng NotificationManager
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, builder.build())
    }

    // Tạo menu trong thanh công cụ
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    //Theo dõi trạng thái mạng theo thời gian thực.
    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Internet connected!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onLost(network: Network) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Internet disconnected!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Xử lý sự kiện khi chọn các mục trong menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                // Lưu dữ liệu nhạy cảm
                SecureStorage.saveData(this, "user_token", "1234567890abcdef")
                Toast.makeText(this, "User token saved securely!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_retrieve -> {
                // Lấy dữ liệu nhạy cảm
                val userToken = SecureStorage.getData(this, "user_token")
                if (userToken != null) {
                    Toast.makeText(this, "Retrieved token: $userToken", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No token found!", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_toggle_theme -> {
                // Chuyển đổi chế độ sáng/tối
                val isDarkMode = !ThemeUtils.isDarkMode(this) // Đảo trạng thái hiện tại
                ThemeUtils.saveThemeMode(this, isDarkMode) // Lưu trạng thái mới
                ThemeUtils.applyTheme(isDarkMode) // Áp dụng chế độ mới
                true
            }
            R.id.menu_send_notification -> {
                // Gửi thông báo nhắc nhở
                sendNotification("Health Reminder", "Don't forget to drink water!")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}