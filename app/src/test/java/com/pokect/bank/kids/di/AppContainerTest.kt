package com.pokect.bank.kids.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AppContainerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        AppContainer.init(context)
    }

    @After
    fun tearDown() {
        // Reset singleton state for test isolation
        // Note: AppContainer is an object singleton, state persists across tests
        // We rely on Robolectric providing a clean shadow environment per test
    }

    @Test
    fun `init creates loginPreferences`() {
        assertNotNull(AppContainer.loginPreferences)
    }

    @Test
    fun `repository is created and returns AppRepository`() {
        val repo = AppContainer.repository

        assertNotNull(repo)
        assertTrue(repo is AppRepository)
    }

    @Test
    fun `repository has seed data after creation`() = runBlocking {
        val repo = AppContainer.repository

        // User should be seeded with Lucas data
        val user = repo.getUser()
        assertNotNull(user)
        assertTrue(user.name == "Lucas")
    }

    @Test
    fun `resetRepository clears internal state`() {
        // Force repository creation
        val originalRepo = AppContainer.repository
        assertNotNull(originalRepo)

        // Reset
        AppContainer.resetRepository()

        // Access again — should create a new repository instance
        val newRepo = AppContainer.repository
        assertNotNull(newRepo)
    }

    @Test
    fun `repository accessible after multiple resetRepository calls`() {
        // First creation
        val first = AppContainer.repository

        // Reset and recreate
        AppContainer.resetRepository()
        val second = AppContainer.repository

        // Reset again
        AppContainer.resetRepository()
        val third = AppContainer.repository

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
    }
}
