package id.iot.trialpp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Button listeners to navigate to other activities
        findViewById<Button>(R.id.buttonTimer).setOnClickListener {
            startActivity(Intent(this, TimerPage::class.java))
        }
        findViewById<Button>(R.id.buttonPortion).setOnClickListener {
            startActivity(Intent(this, PortionPage::class.java))
        }
        findViewById<Button>(R.id.buttonTemp).setOnClickListener {
            startActivity(Intent(this, TempPage::class.java))
        }
        findViewById<ConstraintLayout>(R.id.articleLayout).setOnClickListener {
            startActivity(Intent(this, ArticlePage::class.java))
        }
        findViewById<ImageView>(R.id.account).setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }

        // Navigation drawer setup
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navIcon: Button = findViewById(R.id.nav_icon)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navView.setNavigationItemSelectedListener(this)

        // Firebase Database reference
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("user_data").child("1")

        // Inside the onCreate method, update the UI with schedule
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("MainActivity", "DataSnapshot: ${dataSnapshot.value}")
                val userData = dataSnapshot.getValue(UserData::class.java)
                if (userData != null) {
                    // Update UI with user name
                    Log.d("MainActivity", "User name: ${userData.name}")
                    val welcomeTextView = findViewById<TextView>(R.id.welcome)
                    welcomeTextView.text = "Welcome, ${userData.name}"

                    // Update UI with weight
                    userData.configuration?.weight?.let { weight ->
                        Log.d("MainActivity", "Weight: $weight")
                        val mediumTextView = findViewById<TextView>(R.id.mediumTxt)
                        mediumTextView.text = "${weight} gr"
                    }

                    // Update UI with schedule
                    userData.configuration?.schedule?.let { schedule ->
                        Log.d("MainActivity", "Schedule: $schedule")
                        val timeTextView = findViewById<TextView>(R.id.timeTxt)
                        val todayTextView = findViewById<TextView>(R.id.todayTxt)

                        // Get current day of the week
                        val calendar = Calendar.getInstance()
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val daySchedule = when (dayOfWeek) {
                            Calendar.MONDAY -> schedule.monday
                            Calendar.TUESDAY -> schedule.tuesday
                            Calendar.WEDNESDAY -> schedule.wednesday
                            Calendar.THURSDAY -> schedule.thursday
                            Calendar.FRIDAY -> schedule.friday
                            Calendar.SATURDAY -> schedule.saturday
                            Calendar.SUNDAY -> schedule.sunday
                            else -> "No schedule available"
                        }
                        timeTextView.text = "$daySchedule"
                        todayTextView.text = "$daySchedule today"
                    }

                    // Determine the food level based on proximity values
                    userData.data?.proximity?.let { proximity ->
                        val percentageFoodTextView = findViewById<TextView>(R.id.percentagefood)
                        val level = determineFoodLevel(proximity)
                        percentageFoodTextView.text = level
                    }

                    userData.data?.waterLevel?.let { waterLevel ->
                        val waterLevelPercentage = waterLevel.toIntOrNull()
                        val percentageDrinkTextView = findViewById<TextView>(R.id.percentagedrink)
                        percentageDrinkTextView.text = "$waterLevel%"
                        if (waterLevelPercentage != null) {
                            updateWaterConsumed(waterLevelPercentage)
                        }
                    }
                } else {
                    Log.d("MainActivity", "UserData is null")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    public fun updateWaterConsumed(waterLevelPercentage: Int) {
        val totalCapacity = 3500 // Total capacity in mL
        val waterConsumed = 3500 - (totalCapacity * waterLevelPercentage / 100)
        val waterConsumedTextView = findViewById<TextView>(R.id.watertodayTxt)
        waterConsumedTextView.text = "$waterConsumed mL of water in total"
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> true
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfilePage::class.java))
                true
            }
            else -> false
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Helper function to determine food level
    private fun determineFoodLevel(proximity: Proximity): String {
        return when {
            proximity.one == true && proximity.two == true && proximity.three == true -> "FULL"
            proximity.one == true && proximity.two == true -> "MEDIUM"
            proximity.one == true -> "LOW"
            else -> ""
        }
    }

    data class UserData(
        var configuration: Configuration? = null,
        var data: Data? = null,
        var email: String? = null,
        var name: String? = null,
        var telp: String? = null,
        var password: String? = null
    )

    data class Configuration(
        var schedule: Schedule? = null,
        var weight: String? = null
    )

    data class Schedule(
        var monday: String? = null,
        var tuesday: String? = null,
        var wednesday: String? = null,
        var thursday: String? = null,
        var friday: String? = null,
        var saturday: String? = null,
        var sunday: String? = null
    )

    data class Data(
        var proximity: Proximity? = null,
        var waterLevel: String? = null,
    )

    data class Proximity(
        var one: Boolean? = null,
        var two: Boolean? = null,
        var three: Boolean? = null
    )
}
