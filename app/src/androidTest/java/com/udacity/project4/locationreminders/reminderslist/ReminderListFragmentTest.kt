package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.DataInteraction.*
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.KoinTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.inject


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val dataSource: FakeAndroidDataSource by inject()

    private val requiredModules = module {
        viewModel {
            RemindersListViewModel(
                get(),
                get() as ReminderDataSource
            )
        }
        single {
            FakeAndroidDataSource(
                mutableListOf(
                    ReminderDTO(
                        "Test Title",
                        "Test Description",
                        "Test Location",
                        31.5,
                        31.5,
                    ),
                    ReminderDTO(
                        "Test Title 2",
                        "Test Description",
                        "Test Location",
                        31.5,
                        31.5,
                    ),
                )
            )
        }
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        loadKoinModules(requiredModules)
    }

    @Test
    fun clickFAB_navigateToSaveReminderFragment() = runTest {
        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())


        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test(expected = PerformException::class)
    fun remindersList_showListOfReminders() = runTest {

        launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )

        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                // scrollTo will fail the test if no item matches.
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Title"))
                )
            )

    }


    @Test(expected = PerformException::class)
    fun onError_showErrorMessage() = runTest {
        dataSource.setReturnError(true)

        launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )

        onView(withText("Test exception"))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

    }

    @After
    fun after() {
        unloadKoinModules(requiredModules)
    }
}