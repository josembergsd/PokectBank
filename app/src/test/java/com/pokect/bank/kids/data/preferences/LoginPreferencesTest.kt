package com.pokect.bank.kids.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class LoginPreferencesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + SupervisorJob())
    private lateinit var loginPreferences: LoginPreferences
    private lateinit var testFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testFile = File(context.filesDir, "test_${java.util.UUID.randomUUID()}.preferences_pb")

        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        loginPreferences = LoginPreferences(testDataStore)
    }

    @After
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun `initial state returns default LoginPrefsData`() = runTest(testDispatcher) {
        val prefs = loginPreferences.loginState.first()

        assertFalse(prefs.isLoggedIn)
        assertEquals(1, prefs.selectedAvatar)
    }

    @Test
    fun `setLoggedIn writes and reads back correct values`() = runTest(testDispatcher) {
        loginPreferences.setLoggedIn(avatar = 5)

        val prefs = loginPreferences.loginState.first()

        assertTrue(prefs.isLoggedIn)
        assertEquals(5, prefs.selectedAvatar)
    }

    @Test
    fun `setLoggedIn with different avatar values`() = runTest(testDispatcher) {
        loginPreferences.setLoggedIn(avatar = 3)
        val prefs1 = loginPreferences.loginState.first()
        assertEquals(3, prefs1.selectedAvatar)
        assertTrue(prefs1.isLoggedIn)

        // Overwrite with new avatar (simulates re-login with different avatar)
        loginPreferences.setLoggedIn(avatar = 7)
        val prefs2 = loginPreferences.loginState.first()
        assertEquals(7, prefs2.selectedAvatar)
        assertTrue(prefs2.isLoggedIn)
    }

    @Test
    fun `clearLogin resets to default values`() = runTest(testDispatcher) {
        // First set login
        loginPreferences.setLoggedIn(avatar = 4)
        val prefsAfterLogin = loginPreferences.loginState.first()
        assertTrue(prefsAfterLogin.isLoggedIn)

        // Then clear
        loginPreferences.clearLogin()

        val prefsAfterClear = loginPreferences.loginState.first()
        assertFalse(prefsAfterClear.isLoggedIn)
        assertEquals(1, prefsAfterClear.selectedAvatar) // default
    }

    @Test
    fun `Flow emits updated values after setLoggedIn`() = runTest(testDispatcher) {
        // Verify the Flow reacts to changes
        loginPreferences.setLoggedIn(avatar = 2)

        val emitted = loginPreferences.loginState.first()
        assertTrue(emitted.isLoggedIn)
        assertEquals(2, emitted.selectedAvatar)
    }

    @Test
    fun `Flow emits default values after clearLogin following setLoggedIn`() = runTest(testDispatcher) {
        loginPreferences.setLoggedIn(avatar = 6)

        loginPreferences.clearLogin()

        val emitted = loginPreferences.loginState.first()
        assertFalse(emitted.isLoggedIn)
        assertEquals(1, emitted.selectedAvatar)
    }
}
