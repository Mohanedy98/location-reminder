package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .build()

    }
    @After
    fun closeDB() = database.close()

    @Test
    fun saveReminder_andGetById() = runTest {
        // GIVEN - Insert a Reminder.
        val reminder =  ReminderDTO(
            "Test Title",
            "Test Description",
            "Test Location",
            31.5,
            31.5,
        )

        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
    }

    @Test
    fun deleteAllReminders_clearDatabase() = runTest {
        val reminder =  ReminderDTO(
            "Test Title",
            "Test Description",
            "Test Location",
            31.5,
            31.5,
        )

        database.reminderDao().saveReminder(reminder)
       val reminders =  database.reminderDao().getReminders()
        assertThat(reminders.isNotEmpty(), `is`(true))

        database.reminderDao().deleteAllReminders()
        val remindersAfterDeletion =  database.reminderDao().getReminders()
        assertThat(remindersAfterDeletion.isEmpty(), `is`(true))

    }
}