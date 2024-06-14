package id.iot.trialpp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TimerPage : AppCompatActivity() {

    private lateinit var textViewClock: TextView
    private lateinit var timerContainer: LinearLayout

    private lateinit var database: DatabaseReference

    private val handler = Handler(Looper.getMainLooper())
    private val updateClockRunnable: Runnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1000) // Update every second
        }
    }
    private var timerCount = 0
    private val userId = "1" // Replace with actual user ID as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // First, set the content view to the loading page
        setContentView(R.layout.loading_page)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Delay for 1.2 seconds, then switch to the timer page
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.timer_page)
            initializeClock()

            // Set up the button click listener for adding new timers
            findViewById<Button>(R.id.buttonAddTimer).setOnClickListener {
                showAddTimerDialog()
            }

            val backButton: ImageView = findViewById(R.id.backButton)
            backButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Optionally call finish() if you don't want to keep the current activity in the back stack
            }

            // Sync with Firebase and display the schedule for the current day
            syncWithFirebase()

        }, 1200) // 1200 milliseconds = 1.2 seconds
    }

    private fun initializeClock() {
        textViewClock = findViewById(R.id.textViewClock)
        timerContainer = findViewById(R.id.timerContainer)
        // Start the clock update
        handler.post(updateClockRunnable)
    }

    private fun updateClock() {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(currentTime)
        textViewClock.text = formattedTime
    }

    private fun showAddTimerDialog() {
        val dialog = AddTimerDialog(this, userId)
        dialog.show()
    }

    private fun addNewTimer(timeSet: String, dayOfWeek: String) {
        timerCount++
        val inflater = LayoutInflater.from(this)
        val timerView = inflater.inflate(R.layout.timer_view, timerContainer, false)

        // Find views
        val textViewTimerNumber = timerView.findViewById<TextView>(R.id.textViewTimerNumber)
        val textViewTimeSet = timerView.findViewById<TextView>(R.id.textViewTimeSet)
        val textViewDay = timerView.findViewById<TextView>(R.id.textViewDay)
        val buttonDeleteTimer = timerView.findViewById<Button>(R.id.buttonDeleteTimer)

        // Set values
        textViewTimerNumber.text = "Timer $timerCount"
        textViewTimeSet.text = timeSet
        textViewDay.text = dayOfWeek

        // Set up the delete button
        buttonDeleteTimer.setOnClickListener {
            timerContainer.removeView(timerView)
            removeTimerFromFirebase(dayOfWeek, timeSet)
        }

        // Add the timer view to the container
        timerContainer.addView(timerView)
    }


    private fun removeTimerFromFirebase(dayOfWeek: String, timeSet: String) {
        val timerQuery = database.child("Timers").orderByChild("time").equalTo(timeSet)
        timerQuery.get().addOnSuccessListener { dataSnapshot ->
            for (timerSnapshot in dataSnapshot.children) {
                val day = timerSnapshot.child("day").getValue(String::class.java)
                if (day == dayOfWeek) {
                    timerSnapshot.ref.removeValue()
                }
            }
        }
    }

    private fun syncWithFirebase() {
        val currentDay = getCurrentDay()
        val userRef = database.child("user_data").child(userId).child("configuration").child("schedule").child(currentDay)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                timerContainer.removeAllViews() // Clear existing timers
                if (dataSnapshot.exists()) {
                    val timerString = dataSnapshot.getValue(String::class.java)
                    if (!timerString.isNullOrBlank()) {
                        val timerList = timerString.split(",")
                        timerList.forEach { timeSet ->
                            addNewTimer(timeSet, currentDay)
                        }
                    }
                } else {
                    // Use default times if no specific schedule is found
                    addNewTimer("08:00", currentDay)
                    addNewTimer("17:00", currentDay)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TimerPage", "syncWithFirebase:onCancelled", databaseError.toException())
            }
        })
    }


    private fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            Calendar.SUNDAY -> "sunday"
            else -> "monday"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the clock update when the activity is destroyed to prevent memory leaks
        handler.removeCallbacks(updateClockRunnable)
    }
}
