package id.iot.trialpp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PortionPage : AppCompatActivity() {

    private lateinit var selectedPortionView: ConstraintLayout
    private lateinit var selectedPetView: ConstraintLayout
    private lateinit var database: DatabaseReference
    private val userId = "user_id" // Replace with actual user ID logic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_page)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.portion_page)

            val backButton: ImageView = findViewById(R.id.backButton)
            backButton.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Optionally call finish() if you don't want to keep the current activity in the back stack
            }

            // Portion selection
            val smallContainer: ConstraintLayout = findViewById(R.id.smallContainer)
            val mediumContainer: ConstraintLayout = findViewById(R.id.mediumContainer)
            val largeContainer: ConstraintLayout = findViewById(R.id.largeContainer)
            val customContainer: ConstraintLayout = findViewById(R.id.customContainer)

            smallContainer.setOnClickListener { selectPortion(view = smallContainer, portionSize = getPortionSize(R.id.smallContainer)) }
            mediumContainer.setOnClickListener { selectPortion(view = mediumContainer, portionSize = getPortionSize(R.id.mediumContainer)) }
            largeContainer.setOnClickListener { selectPortion(view = largeContainer, portionSize = getPortionSize(R.id.largeContainer)) }
            customContainer.setOnClickListener { selectPortion(view = customContainer, portionSize = null) }

            // Pet selection
            val dogContainer: ConstraintLayout = findViewById(R.id.dogContainer)
            val catContainer: ConstraintLayout = findViewById(R.id.catContainer)

            dogContainer.setOnClickListener { selectPet(view = dogContainer, isDog = true) }
            catContainer.setOnClickListener { selectPet(view = catContainer, isDog = false) }

        }, 1000)
    }

    private fun getPortionSize(viewId: Int): Int {
        return when (viewId) {
            R.id.smallContainer -> {
                if (::selectedPetView.isInitialized && selectedPetView.id == R.id.dogContainer) 100 else 50
            }
            R.id.mediumContainer -> {
                if (::selectedPetView.isInitialized && selectedPetView.id == R.id.dogContainer) 200 else 100
            }
            R.id.largeContainer -> {
                if (::selectedPetView.isInitialized && selectedPetView.id == R.id.dogContainer) 300 else 150
            }
            else -> 0
        }
    }

    private fun selectPortion(view: ConstraintLayout, portionSize: Int?) {
        // Reset previous selection
        if (::selectedPortionView.isInitialized) {
            selectedPortionView.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.white)
        }

        // Highlight selected view
        view.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_light)
        selectedPortionView = view

        // Handle custom selection
        if (view.id == R.id.customContainer) {
            showCustomPortionDialog()
        } else {
            // Upload the portion size to Firebase
            portionSize?.let {
                uploadPortionSizeToFirebase(it)
            }
        }
    }

    private fun selectPet(view: ConstraintLayout, isDog: Boolean) {
        // Reset previous selection
        if (::selectedPetView.isInitialized) {
            selectedPetView.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.white)
        }

        // Highlight selected view
        view.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_light)
        selectedPetView = view

        // Update portion sizes based on selected pet
        val smallLabel: TextView = findViewById(R.id.smallLabel)
        val mediumLabel: TextView = findViewById(R.id.mediumLabel)
        val largeLabel: TextView = findViewById(R.id.largeLabel)

        if (isDog) {
            smallLabel.text = "Small (100g)"
            mediumLabel.text = "Medium (200g)"
            largeLabel.text = "Large (300g)"
        } else {
            smallLabel.text = "Small (50g)"
            mediumLabel.text = "Medium (100g)"
            largeLabel.text = "Large (150g)"
        }
    }

    private fun showCustomPortionDialog() {
        val editText = EditText(this)
        editText.hint = "Enter grams"

        MaterialAlertDialogBuilder(this)
            .setTitle("Custom Portion Size")
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                val input = editText.text.toString()
                if (input.isNotEmpty()) {
                    val customLabel: TextView = findViewById(R.id.customLabel)
                    customLabel.text = "Custom ($input g)"
                    // Upload the custom portion size to Firebase
                    uploadPortionSizeToFirebase(input.toInt())
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadPortionSizeToFirebase(portionSize: Int) {
        val userRef = database.child("user_data").child("1").child("configuration")
        val portionData = mapOf(
            "weight" to portionSize.toString()
        )
        userRef.updateChildren(portionData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("PortionPage", "Portion size updated successfully")
            } else {
                Log.e("PortionPage", "Failed to update portion size", task.exception)
            }
        }
    }
}
