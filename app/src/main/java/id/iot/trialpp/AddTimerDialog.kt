package id.iot.trialpp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TimePicker
import com.google.firebase.database.FirebaseDatabase
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import com.google.firebase.database.GenericTypeIndicator

class AddTimerDialog(context: Context, private val userId: String) : Dialog(context) {

    private lateinit var timePicker: TimePicker
    private lateinit var daySpinner: Spinner
    private lateinit var addButton: Button
    private lateinit var selectedDay: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_timer)

        timePicker = findViewById(R.id.timePicker)
        daySpinner = findViewById(R.id.daySpinner)
        addButton = findViewById(R.id.addButton)

        // Initialize spinner options
        val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, daysOfWeek)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter

        // Listen to spinner selection
        daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDay = daysOfWeek[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        addButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val selectedTime = String.format("%02d:%02d", hour, minute)
            addTimerToFirebase(selectedTime, selectedDay)
            dismiss()
        }
    }

    private fun addTimerToFirebase(timeSet: String, dayOfWeek: String) {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("user_data").child(userId).child("configuration").child("schedule").child(dayOfWeek)
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val currentTimes = dataSnapshot.getValue(String::class.java)
            val updatedTimes = if (currentTimes.isNullOrBlank()) {
                timeSet
            } else {
                "$currentTimes,$timeSet"
            }
            userRef.setValue(updatedTimes)
        }
    }

}
