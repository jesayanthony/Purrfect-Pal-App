package id.iot.trialpp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optionally call finish() if you don't want to keep the current activity in the back stack
        }

        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.username)
            val passwordEditText = findViewById<EditText>(R.id.password)
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                checkCredentialsInFirebase(email, password)
            }
        }
    }

    private fun checkCredentialsInFirebase(email: String, password: String) {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("user_data")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var credentialsValid = false
                for (snapshot in dataSnapshot.children) {
                    val userData = snapshot.getValue(UserData::class.java)
                    if (userData?.email == email && userData.password == password) {
                        credentialsValid = true
                        break
                    }
                }
                if (credentialsValid) {
                    val intent = Intent(this@LoginPage, ProfilePage::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginPage, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LoginPage", "checkCredentialsInFirebase:onCancelled", databaseError.toException())
            }
        })
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
        var weight: String? = null
    )

    data class Proximity(
        var one: Boolean? = null,
        var two: Boolean? = null,
        var three: Boolean? = null
    )
}
