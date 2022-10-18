package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource :
    ReminderDataSource {
    private val reminders: MutableList<ReminderDTO> = mutableListOf()
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        if (shouldReturnError) {
            throw Error("Test exception")
        }
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return try {

            val reminder = reminders.first { reminderDTO -> reminderDTO.id == id }
            Result.Success(reminder)

        }catch (e: Exception){
            Result.Error(
                "Reminder not found"
            )
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}