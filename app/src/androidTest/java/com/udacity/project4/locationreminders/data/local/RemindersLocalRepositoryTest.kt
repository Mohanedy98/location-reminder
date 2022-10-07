package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepositoryTest: RemindersLocalRepository

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        remindersLocalRepositoryTest =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun saveReminder_andGetById_verifyCorrect() = runTest {
        // GIVEN - Insert a Reminder.
        val reminder = ReminderDTO(
            "Test Title",
            "Test Description",
            "Test Location",
            31.5,
            31.5,
        )

        remindersLocalRepositoryTest.saveReminder(reminder)

        val loaded = remindersLocalRepositoryTest.getReminder(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded, CoreMatchers.notNullValue())
        loaded as Result.Success
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun unknownId_returnsError() = runTest {
        val loaded = remindersLocalRepositoryTest.getReminder("sdafsdmvaksvm")

        assertThat(loaded, CoreMatchers.notNullValue())
        loaded as Result.Error
        assertThat(loaded.message, `is`("Reminder not found!"))
    }
}