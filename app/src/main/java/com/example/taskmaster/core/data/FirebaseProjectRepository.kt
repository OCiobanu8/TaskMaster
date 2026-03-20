package com.example.taskmaster.core.data

import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.model.Project
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class FirebaseProjectRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ProjectRepository {

    override suspend fun createProject(name: String): Result<Project> {
        val currentUser = authRepository.currentUser().first()
            ?: return Result.failure(IllegalStateException("Sign in required to create projects"))

        return runCatching {
            val projectRef = firestore.collection(FirestoreCollections.PROJECTS).document()
            val project = Project(
                id = projectRef.id,
                name = name,
                ownerUserId = currentUser.id,
                memberIds = listOf(currentUser.id)
            )
            val memberRef = firestore.collection(FirestoreCollections.PROJECT_MEMBERS)
                .document("${project.id}_${currentUser.id}")

            firestore.batch().apply {
                set(
                    projectRef,
                    mapOf(
                        "name" to project.name,
                        "ownerUserId" to project.ownerUserId,
                        "memberIds" to project.memberIds,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                set(
                    memberRef,
                    mapOf(
                        "projectId" to project.id,
                        "userId" to currentUser.id,
                        "role" to "owner",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.commit().await()

            project
        }
    }

    override fun observeProjects(userId: String): Flow<List<Project>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection(FirestoreCollections.PROJECTS)
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val projects = snapshot?.documents.orEmpty().mapNotNull { it.toProject() }
                trySend(projects)
            }

        awaitClose { registration.remove() }
    }
}
