package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.DataInteraction.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var dataSource: ReminderDataSource

    private val dataBindingIdlingResource = DataBindingIdlingResource()


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()//stop the original app koin
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single { FakeAndroidDataSource() as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        dataSource = get()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * Checks that navigation occurs to the save reminder fragment
     */
    @Test
    fun clickFAB_navigateToSaveReminderFragment() = runTest {
        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())


        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
        scenario.close()
    }

    /**
     * Checks if the list is loaded and items is displayed
     */
    @Test
    fun remindersList_showListOfReminders() = runTest {
        dataSource.saveReminder(
            ReminderDTO(
                "Test Title",
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            ),
        )

        dataSource.saveReminder(
            ReminderDTO(
                "Test Title 2",
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            )
        )

        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                // scrollTo will fail the test if no item matches.
                RecyclerViewActions.scrollTo<ViewHolder>(
                    hasDescendant(withText("Test Title"))
                )
            )
        scenario.close()
    }

    /**
     * Checks if the list failed to load the data it will show error message in snack-bar
     */
    @Test
    fun onFailedToLoadData_showErrorMessage() = runTest {

        dataSource.saveReminder(
            ReminderDTO(
                "Test Title",
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            ),
        )

        dataSource.saveReminder(
            ReminderDTO(
                "Test Title 2",
                "Test Description",
                "Test Location",
                31.5,
                31.5,
            )
        )

        (dataSource as FakeAndroidDataSource).setReturnError(true)

       val scenario =  launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withText("Test exception"))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        scenario.close()

    }
}