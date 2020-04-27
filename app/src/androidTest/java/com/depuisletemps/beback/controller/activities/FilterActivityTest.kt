package com.depuisletemps.beback.controller.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.util.Checks
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.depuisletemps.beback.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterActivityTest {
    var mYellow: Int = 0
    var grey: Int = 0
    lateinit var appCompatEditTextProduct: ViewInteraction
    lateinit var appCompatEditTextRecipient: ViewInteraction
    lateinit var submitButton: ViewInteraction

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(FilterActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.READ_CONTACTS"
        )

    @Before
    fun setup() {
        mYellow = Color.parseColor("#FFA726")
        grey = Color.parseColor("#DDDDDD")

        appCompatEditTextProduct = onView(withId(R.id.filter_product))
        appCompatEditTextRecipient = onView(withId(R.id.filter_recipient))
        submitButton = onView(withId(R.id.mBtnSubmit))
    }

    @Test
    fun EnteringAProductShouldReturnYellowButtonTest() {
        appCompatEditTextProduct.perform(
            ViewActions.replaceText("Car"),
            ViewActions.closeSoftKeyboard()
        )

        submitButton.check(
            ViewAssertions.matches(buttonShouldHaveBackgroundColor(mYellow))
        )
    }

    @Test
    fun EnteringARecipientShouldReturnYellowButtonTest() {
        val appCompatEditText = onView(withId(R.id.filter_recipient))
        appCompatEditText.perform(
            ViewActions.replaceText("Bob"),
            ViewActions.closeSoftKeyboard()
        )

        submitButton.check(
            ViewAssertions.matches(buttonShouldHaveBackgroundColor(mYellow))
        )
    }

    @Test
    fun EnablingALoanTypeShouldReturnYellowButtonTest() {
        val appCompatToggleButton = onView(withId(R.id.toggle_borrowing))
        appCompatToggleButton.perform(click())

        submitButton.check(
            ViewAssertions.matches(buttonShouldHaveBackgroundColor(mYellow))
        )
    }

    @Test
    fun EnablingALoanTypeAndEnteringAProductShouldReturnYellowButtonTest() {
        val appCompatToggleButton = onView(withId(R.id.toggle_borrowing))
        appCompatToggleButton.perform(click())

        appCompatEditTextProduct.perform(
            ViewActions.replaceText("Car"),
            ViewActions.closeSoftKeyboard()
        )

        submitButton.check(
            ViewAssertions.matches(buttonShouldHaveBackgroundColor(mYellow))
        )
    }

    @Test
    fun NotEnteringAnythingShouldReturnGreyButtonTest() {
        val appCompatToggleButton = onView(withId(R.id.toggle_borrowing))
        appCompatToggleButton.perform(click())
        appCompatToggleButton.perform(click())

        appCompatEditTextProduct.perform(
            ViewActions.replaceText("Car"),
            ViewActions.closeSoftKeyboard()
        )
        appCompatEditTextProduct.perform(
            ViewActions.replaceText(""),
            ViewActions.closeSoftKeyboard()
        )

        appCompatEditTextRecipient.perform(
            ViewActions.replaceText("Bob"),
            ViewActions.closeSoftKeyboard()
        )
        appCompatEditTextRecipient.perform(
            ViewActions.replaceText(""),
            ViewActions.closeSoftKeyboard()
        )

        submitButton.check(
            ViewAssertions.matches(buttonShouldHaveBackgroundColor(grey))
        )
    }

    fun buttonShouldHaveBackgroundColor(color: Int): Matcher<View?>? {
        Checks.checkNotNull(color)
        return object : BoundedMatcher<View?, FloatingActionButton>(
            FloatingActionButton::class.java
        ) {
            override fun matchesSafely(btn: FloatingActionButton): Boolean {
                val colorStateList = btn.backgroundTintList
                val testList = ColorStateList.valueOf(color)
                return colorStateList === testList
            }

            override fun describeTo(description: Description) {
                description.appendText("Color did not match $color")
            }
        }
    }

}