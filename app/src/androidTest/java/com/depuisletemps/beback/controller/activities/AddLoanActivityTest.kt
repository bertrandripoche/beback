package com.depuisletemps.beback.controller.activities


import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.internal.util.Checks
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.depuisletemps.beback.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddLoanActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(AddLoanActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.READ_CONTACTS"
        )

    @Test
    fun GivenProductAndNameFilledShouldReturnYellowButtonTest() {

        val appCompatEditText = onView(
            allOf(
                withId(R.id.loan_product),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("Ball"), closeSoftKeyboard())

        val appCompatAutoCompleteTextView = onView(
            allOf(
                withId(R.id.loan_recipient),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        appCompatAutoCompleteTextView.perform(replaceText("Aude"), closeSoftKeyboard())

        val imageButton = onView(
            allOf(
                withId(R.id.mBtnSubmit),
                isDisplayed()
            )
        )

        val yellow = Color.parseColor("#FFA726")
        imageButton.check(ViewAssertions.matches(buttonShouldHaveBackgroundColor(yellow))
        )
    }

    @Test
    fun GivenProductAndNameNotFilledShouldReturnGreyButtonTest() {

        val appCompatEditText = onView(
            allOf(
                withId(R.id.loan_product),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("Ball"), closeSoftKeyboard())

        val imageButton = onView(
            allOf(
                withId(R.id.mBtnSubmit),
                isDisplayed()
            )
        )

        val grey = Color.parseColor("#DDDDDD")
        imageButton.check(ViewAssertions.matches(buttonShouldHaveBackgroundColor(grey))
        )
    }

    @Test
    fun GivenProductAndNameNotFilledAfterBeingFilledShouldReturnGreyButtonTest() {

        val appCompatEditText = onView(
            allOf(
                withId(R.id.loan_product),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("Ball"), closeSoftKeyboard())

        val appCompatAutoCompleteTextView = onView(
            allOf(
                withId(R.id.loan_recipient),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        appCompatAutoCompleteTextView.perform(replaceText("Aude"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.loan_product), withText("Ball"),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText(""))

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.loan_product),
                childAtPosition(
                    allOf(
                        withId(R.id.activity_add_loan_container),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(closeSoftKeyboard())

        val imageButton = onView(
            allOf(
                withId(R.id.mBtnSubmit),
                isDisplayed()
            )
        )

        val grey = Color.parseColor("#DDDDDD")
        imageButton.check(ViewAssertions.matches(buttonShouldHaveBackgroundColor(grey))
        )
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
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
