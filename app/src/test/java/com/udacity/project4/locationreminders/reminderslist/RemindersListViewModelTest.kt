package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        val application = ApplicationProvider.getApplicationContext() as Application
        fakeDataSource = FakeDataSource(
            mutableListOf(
                ReminderDTO(
                    "Test Title",
                    "Test Description",
                    "Test Location",
                    31.5,
                    31.5,
                ),
                ReminderDTO(
                    "Test Title",
                    "Test Description",
                    "Test Location",
                    31.5,
                    31.5,
                ),
                ReminderDTO(
                    "Test Title",
                    "Test Description",
                    "Test Location",
                    31.5,
                    31.5,
                ),
            )
        )

        remindersListViewModel = RemindersListViewModel(application, fakeDataSource)
    }


    @Test
    fun loadReminders_checkLoading() {
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_populateRemindersList() {
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().size,
            greaterThan(0)
        )
    }

    @Test
    fun loadRemindersFailure_showSnackBar() {
        fakeDataSource.setReturnError(true)

        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )


        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("Test exception")
        )

    }

}