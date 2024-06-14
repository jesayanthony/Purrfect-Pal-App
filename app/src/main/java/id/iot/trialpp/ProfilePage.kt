package id.iot.trialpp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class ProfilePage : AppCompatActivity(), EditProfileDialog.OnProfileUpdatedListener {

    private lateinit var profileImageView: CircleImageView
    private lateinit var nameTxt: TextView
    private lateinit var telpTxt: TextView
    private lateinit var emailTxt: TextView
    private lateinit var constraintEdit: ConstraintLayout

    private val userId = "1" // Replace with actual user ID if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optionally call finish() if you don't want to keep the current activity in the back stack
        }

        val imageView: CircleImageView = findViewById(R.id.jisoo)
        Glide.with(this)
            .load(R.drawable.jisoo)
            .into(imageView)

        // Logout page
        val loginLayout: ConstraintLayout = findViewById(R.id.constraintLogout)
        loginLayout.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        profileImageView = findViewById(R.id.jisoo)
        nameTxt = findViewById(R.id.nameTxt)
        telpTxt = findViewById(R.id.telpTxt)
        emailTxt = findViewById(R.id.emailTxt)
        constraintEdit = findViewById(R.id.constraintEdit)

        constraintEdit.setOnClickListener {
            EditProfileDialog(this, userId, this).show()
        }

        // Load user data from Firebase
        val database = FirebaseDatabase.getInstance().reference.child("user_data").child(userId)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(MainActivity.UserData::class.java)
                if (userData != null) {
                    nameTxt.text = userData.name
                    emailTxt.text = userData.email
                    telpTxt.text = userData.telp
                } else {
                    Log.d("ProfilePage", "UserData is null")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ProfilePage", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    override fun onProfileUpdated(name: String, email: String, phone: String) {
        nameTxt.text = name
        emailTxt.text = email
        telpTxt.text = phone
    }
}
