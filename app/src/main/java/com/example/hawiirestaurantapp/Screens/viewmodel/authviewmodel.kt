package com.example.hawiirestaurantapp.Screens.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.hawiirestaurantapp.Data.uploadImage
import com.example.hawiirestaurantapp.Screens.Data.CartManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

data class UserProfile(
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val role: String = "user"
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    var loading by mutableStateOf(false)
        private set
    var uploading by mutableStateOf(false)
        private set
    var profileLoaded by mutableStateOf(false)
        private set
    var profile by mutableStateOf(UserProfile())
        private set
    var message by mutableStateOf("")

    fun signup(
        fullname: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (fullname.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            message = "Please fill in all fields"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            message = "Enter a valid email address"
            return
        }
        if (password.length < 6) {
            message = "Password must be at least 6 characters"
            return
        }
        if (password != confirmPassword) {
            message = "Passwords do not match"
            return
        }
        message = ""
        loading = true
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    loading = false
                    message = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
                        else -> task.exception?.localizedMessage ?: "Sign up failed. Try again"
                    }
                    return@addOnCompleteListener
                }
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    loading = false
                    message = "Something went wrong. Try logging in"
                    return@addOnCompleteListener
                }
                val user = hashMapOf<String, Any>(
                    "fullname" to fullname.trim(),
                    "email" to email.trim(),
                    "phone" to phone.trim(),
                    "address" to "",
                    "photoUrl" to "",
                    "role" to "user"
                )
                database.getReference("Users").child(uid).setValue(user)
                    .addOnCompleteListener { saveTask ->
                        loading = false
                        if (saveTask.isSuccessful) {
                            onSuccess()
                        } else {
                            message = "Account created, but saving your details failed: " + (saveTask.exception?.localizedMessage ?: "") + ". Try logging in."
                        }
                    }
            }
    }

    fun login(email: String, password: String, onSuccess: (Boolean) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            message = "Please fill in all fields"
            return
        }
        message = ""
        loading = true
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    loading = false
                    message = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> "Incorrect email or password"
                        else -> task.exception?.localizedMessage ?: "Login failed. Try again"
                    }
                    return@addOnCompleteListener
                }
                val uid = auth.currentUser?.uid ?: ""
                database.getReference("Users").child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        loading = false
                        onSuccess(snapshot.child("role").getValue(String::class.java) == "admin")
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        message = "Could not check your account type: " + (e.localizedMessage ?: "")
                    }
            }
    }

    fun signOut() {
        CartManager.clear()
        auth.signOut()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            message = "You are not logged in"
            profileLoaded = true
            return
        }
        database.getReference("Users").child(uid).get()
            .addOnSuccessListener { s ->
                profile = UserProfile(
                    fullname = s.child("fullname").getValue(String::class.java) ?: "",
                    email = s.child("email").getValue(String::class.java) ?: (auth.currentUser?.email ?: ""),
                    phone = s.child("phone").getValue(String::class.java) ?: "",
                    address = s.child("address").getValue(String::class.java) ?: "",
                    photoUrl = s.child("photoUrl").getValue(String::class.java) ?: "",
                    role = s.child("role").getValue(String::class.java) ?: "user"
                )
                profileLoaded = true
            }
            .addOnFailureListener { e ->
                message = "Could not load your profile: " + (e.localizedMessage ?: "")
                profile = profile.copy(email = auth.currentUser?.email ?: "")
                profileLoaded = true
            }
    }

    fun updateProfile(fullname: String, phone: String, address: String, onDone: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            message = "You are not logged in"
            onDone(false)
            return
        }
        if (fullname.isBlank() || phone.isBlank()) {
            message = "Name and phone number are required"
            onDone(false)
            return
        }
        message = ""
        loading = true
        val ref = database.getReference("Users").child(user.uid)
        ref.get().addOnCompleteListener { readTask ->
            val exists = readTask.isSuccessful && readTask.result?.exists() == true
            val updates = hashMapOf<String, Any>(
                "fullname" to fullname.trim(),
                "phone" to phone.trim(),
                "address" to address.trim(),
                "email" to (user.email ?: "")
            )
            if (!exists) {
                updates["role"] = "user"
            }
            ref.updateChildren(updates).addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    profile = profile.copy(
                        fullname = fullname.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        email = user.email ?: profile.email
                    )
                    message = "Profile updated"
                    onDone(true)
                } else {
                    message = "Could not save: " + (task.exception?.localizedMessage ?: "")
                    onDone(false)
                }
            }
        }
    }

    fun uploadProfilePhoto(context: Context, uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            message = "You are not logged in"
            return
        }
        uploading = true
        message = ""
        uploadImage(
            context,
            uri,
            { url ->
                database.getReference("Users").child(uid).child("photoUrl").setValue(url)
                    .addOnCompleteListener { task ->
                        uploading = false
                        if (task.isSuccessful) {
                            profile = profile.copy(photoUrl = url)
                            message = "Photo updated"
                        } else {
                            message = "Photo uploaded, but saving it failed: " + (task.exception?.localizedMessage ?: "")
                        }
                    }
            },
            { msg ->
                uploading = false
                message = "Photo upload failed: $msg"
            }
        )
    }
}