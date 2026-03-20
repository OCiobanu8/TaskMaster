package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirebaseTaskRepository(
    private val firestore: FirebaseFirestore
) : TaskRepository {
    private companion object {
        const val DELETE_TIMEOUT_MS = 10_000L
    }

    override suspend fun createTask(
        projectId: String,
        title: String,
        assigneeUserId: String,
        dueAt: Instant
    ): Result<TaskInstance> {
        return runCatching {
            val createdAt = Instant.now()
            val docRef = firestore.collection(FirestoreCollections.TASK_INSTANCES).document()
            val task = TaskInstance(
                id = docRef.id,
                projectId = projectId,
                title = title,
                assigneeUserId = assigneeUserId,
                status = TaskStatus.TODO,
                dueAt = dueAt,
                createdAt = createdAt
            )

            docRef.set(
                mapOf(
                    "projectId" to task.projectId,
                    "title" to task.title,
                    "assigneeUserId" to task.assigneeUserId,
                    "status" to task.status.name,
                    "dueAt" to Timestamp(dueAt.epochSecond, dueAt.nano),
                    "createdAt" to Timestamp(createdAt.epochSecond, createdAt.nano),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            task
        }
    }

    override fun observeTasks(projectId: String): Flow<List<TaskInstance>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection(FirestoreCollections.TASK_INSTANCES)
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents.orEmpty().mapNotNull { it.toTaskInstance() }
                trySend(tasks.sortedBy { it.title.lowercase() })
            }

        awaitClose { registration.remove() }
    }

    override fun observeTasksForUser(userId: String): Flow<List<TaskInstance>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection(FirestoreCollections.TASK_INSTANCES)
            .whereEqualTo("assigneeUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents.orEmpty().mapNotNull { it.toTaskInstance() }
                trySend(tasks)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return runCatching {
            withTimeout(DELETE_TIMEOUT_MS) {
                firestore.collection(FirestoreCollections.TASK_INSTANCES)
                    .document(taskId)
                    .delete()
                    .await()
            }
            Unit
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return runCatching {
            firestore.collection(FirestoreCollections.TASK_INSTANCES)
                .document(taskId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Unit
        }
    }
}
