package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.Project
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProject(): Project? {
    return projectFromMap(id = id, data = data.orEmpty())
}

fun DocumentSnapshot.toTaskInstance(): TaskInstance? {
    return taskInstanceFromMap(id = id, data = data.orEmpty())
}

fun projectFromMap(id: String, data: Map<String, Any?>): Project? {
    val name = data["name"] as? String ?: return null
    val ownerUserId = data["ownerUserId"] as? String ?: return null
    @Suppress("UNCHECKED_CAST")
    val memberIds = (data["memberIds"] as? List<String>).orEmpty()
    return Project(
        id = id,
        name = name,
        ownerUserId = ownerUserId,
        memberIds = memberIds
    )
}

fun taskInstanceFromMap(id: String, data: Map<String, Any?>): TaskInstance? {
    val projectId = data["projectId"] as? String ?: return null
    val title = data["title"] as? String ?: return null
    val assigneeUserId = data["assigneeUserId"] as? String ?: return null
    val status = TaskStatus.fromStorage(data["status"] as? String ?: "")
    return TaskInstance(
        id = id,
        projectId = projectId,
        title = title,
        assigneeUserId = assigneeUserId,
        status = status
    )
}
