package com.example.tasksapp.data.repository

import com.example.tasksapp.data.local.TaskDao
import com.example.tasksapp.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun insert(task: Task): Long {
        return taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    suspend fun deleteById(taskId: Long) {
        taskDao.deleteById(taskId)
    }

    suspend fun toggleComplete(taskId: Long, isCompleted: Boolean) {
        taskDao.toggleComplete(taskId, isCompleted)
    }

    suspend fun deleteAll() {
        taskDao.deleteAll()
    }
}
