package com.metallicfist.firebasedemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    public var storedVerificationId: String = ""
    public var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = FirebaseFirestore.getInstance()

        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("log", "onVerificationCompleted:$credential")

                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("log", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(
                verificationId: String?,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("log", "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // ...
            }
        }

        button.setOnClickListener {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                editText.text.toString(),      // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,             // Activity (for callback binding)
                callbacks) // OnVerificationStateChangedCallbacks
            editText.text.clear()
            //button.isEnabled = false


            //Cloud Firestore methods
            //createNewUser(db)
            //createNewUserExtended(db)
        }
        button2.setOnClickListener {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, editText2.text.toString())
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        var auth: FirebaseAuth? = null
        auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("log", "signInWithCredential:success")

                    val user = task.result?.user

                    Toast.makeText(this@MainActivity, "Sign-in successful!", Toast.LENGTH_LONG).show()
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("log", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }


    //Cloud Firestore methods
    private fun createNewUserExtended(db: FirebaseFirestore) {
        // Create a new user with a first, middle, and last name
        val user = HashMap<String, Any>()
        user["first"] = "Alan"
        user["middle"] = "Mathison"
        user["last"] = "Turing"
        user["born"] = 1912

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("success", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("failure", "Error adding document", e)
            }
    }

    //Cloud Firestore methods
    private fun createNewUser(db: FirebaseFirestore) {
        // Create a new user with a first and last name
        val user = HashMap<String, Any>()
        user["first"] = "Ada"
        user["last"] = "Lovelace"
        user["born"] = 1815

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("success", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("failure", "Error adding document", e)
            }
    }

}
