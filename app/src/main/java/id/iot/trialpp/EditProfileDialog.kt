package id.iot.trialpp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class EditProfileDialog(context: Context, private val userId: String, private val listener: OnProfileUpdatedListener) : Dialog(context) {
    private var profileImageView: CircleImageView? = null
    private var editName: EditText? = null
    private var editEmail: EditText? = null
    private var editPhone: EditText? = null
    private var saveButton: Button? = null

    private lateinit var database: DatabaseReference

    interface OnProfileUpdatedListener {
        fun onProfileUpdated(name: String, email: String, phone: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_dialog)

        database = FirebaseDatabase.getInstance().reference.child("user_data").child("1")

        profileImageView = findViewById(R.id.profileImageView)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        saveButton = findViewById(R.id.saveButton)

        saveButton?.setOnClickListener {
            val name = editName?.text.toString()
            val email = editEmail?.text.toString()
            val phone = editPhone?.text.toString()

            // Save to Firebase
            val userUpdates = mapOf<String, Any>(
                "name" to name,
                "email" to email,
                "telp" to phone
            )

            database.updateChildren(userUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Invoke the callback
                    listener.onProfileUpdated(name, email, phone)
                    dismiss()
                } else {
                    // Handle update failure
                }
            }
        }
    }
}
