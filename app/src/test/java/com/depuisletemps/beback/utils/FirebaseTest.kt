package com.depuisletemps.beback.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
class FirebaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testWrite() {
        val future: CompletableFuture<String> = CompletableFuture()
        val app = FirebaseApp.initializeApp(context)!!

        val firestore = FirebaseFirestore.getInstance(app)
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setHost("localhost:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()

        firestore.collection("loans").document("abc").set(mapOf("product" to "car", "" to ""))
            .addOnSuccessListener {
                throw Exception("success")
                //future.complete("success")
            }
            .addOnFailureListener {
                throw Exception("failure")
            }
            .addOnCanceledListener {
                throw Exception("canceled")
            }
        //future.get(1, TimeUnit.SECONDS)
        Thread.sleep(10000)

    }


}