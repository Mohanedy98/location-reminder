package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var fakeDataSource: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel() {
        stopKoin()
        val application = ApplicationProvider.getApplicationContext() as Application
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(application, fakeDataSource)
    }


    @Test
    fun saveNewReminder_setsShowLoading() {
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "Test Title",
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            )
        )

        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
           `is`(true)
        )
        mainCoroutineRule.resumeDispatcher()

        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
    }

    @Test
    fun setTitleToNull__returnsFalseAndShowsError() {

        val result = saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                null,
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            )
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
        assertThat(
            result,
           `is`(false)
        )
    }

    @Test
    fun setLocationToNull__returnsFalseAndShowsError() {

        val result = saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                "Title",
                "Test Description",
                null,
                31.5,
                31.5,
            )
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
        assertThat(
            result,
            `is`(false)
        )
    }

}