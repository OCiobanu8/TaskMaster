package com.example.taskmaster

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.taskmaster.core.data.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreRulesIntegrationTest {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    @Before
    fun setUp() {
        val args = InstrumentationRegistry.getArguments()
        val shouldRun = args.getString("RUN_FIREBASE_EMULATOR_TESTS") == "true"
        assumeTrue(
            "Set instrumentation arg RUN_FIREBASE_EMULATOR_TESTS=true to run emulator-backed rules tests.",
            shouldRun
        )

        auth.useEmulator("10.0.2.2", 9099)
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = firestore.firestoreSettings
            .toBuilder()
            .setPersistenceEnabled(false)
            .build()
    }

    @Test
    fun memberCanWriteTask_nonMemberDeniedRead() = runBlocking {
        val owner = auth.signInAnonymously().await().user ?: error("owner sign in failed")
        val projectId = "proj_${System.currentTimeMillis()}"
        val taskId = "task_${System.currentTimeMillis()}"

        firestore.collection(FirestoreCollections.PROJECTS).document(projectId)
            .set(
                mapOf(
                    "name" to "Rules Test",
                    "ownerUserId" to owner.uid,
                    "memberIds" to listOf(owner.uid)
                )
            ).await()

        firestore.collection(FirestoreCollections.PROJECT_MEMBERS)
            .document("${projectId}_${owner.uid}")
            .set(
                mapOf(
                    "projectId" to projectId,
                    "userId" to owner.uid,
                    "role" to "owner"
                )
            ).await()

        firestore.collection(FirestoreCollections.TASK_INSTANCES).document(taskId)
            .set(
                mapOf(
                    "projectId" to projectId,
                    "title" to "Owner task",
                    "assigneeUserId" to owner.uid,
                    "status" to "TODO"
                )
            ).await()

        auth.signOut()
        auth.signInAnonymously().await().user ?: error("guest sign in failed")

        try {
            firestore.collection(FirestoreCollections.TASK_INSTANCES).document(taskId).get().await()
            throw AssertionError("Expected PERMISSION_DENIED for non-member access")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }
}
