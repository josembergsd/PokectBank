package com.pokect.bank.kids.ui.screens.rewards

import com.pokect.bank.kids.data.local.TestDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class RewardsViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state matches repository seed data`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertEquals(85, state.coins)
        assertEquals(4, state.rewards.size)
        assertFalse(state.showRedemptionModal)
        assertEquals(null, state.selectedReward)
        assertFalse(state.showConfetti)
        Unit
    }

    @Test
    fun `canAfford is true when coins sufficient`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val escudo = vm.uiState.value.rewards.find { it.reward.id == "r1" }

        assertEquals(true, escudo?.canAfford)
    }

    @Test
    fun `canAfford is false when coins insufficient`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val bau = vm.uiState.value.rewards.find { it.reward.id == "r4" }

        assertEquals(false, bau?.canAfford)
    }

    @Test
    fun `redeemReward sets selectedReward and showRedemptionModal`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)

        vm.redeemReward("r1")

        val state = vm.uiState.value
        assertEquals("r1", state.selectedReward?.id)
        assertTrue(state.showRedemptionModal)
        Unit
    }

    @Test
    fun `confirmRedemption deducts coins and shows confetti`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val initialCoins = vm.uiState.value.coins

        vm.redeemReward("r1")
        vm.confirmRedemption()

        delay(500)

        assertEquals(initialCoins - 50, vm.uiState.value.coins)
        assertTrue(vm.uiState.value.showConfetti)
        assertEquals("Prêmio resgatado!", vm.uiState.value.redemptionSuccessMessage)
    }

    @Test
    fun `confirmRedemption marks reward as redeemed`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)

        vm.redeemReward("r1")
        vm.confirmRedemption()

        delay(500)

        val redeemedReward = vm.uiState.value.rewards.find { it.reward.id == "r1" }
        assertTrue(redeemedReward?.reward?.redeemed == true)
        Unit
    }

    @Test
    fun `dismissRedemptionModal clears selection`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)

        vm.redeemReward("r1")
        vm.dismissRedemptionModal()

        val state = vm.uiState.value
        assertFalse(state.showRedemptionModal)
        assertEquals(null, state.selectedReward)
    }

    @Test
    fun `cannot redeem when coins insufficient`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val initialCoins = vm.uiState.value.coins

        vm.redeemReward("r4")
        vm.confirmRedemption()

        delay(500)

        assertEquals(initialCoins, vm.uiState.value.coins)
        assertFalse(vm.uiState.value.showConfetti)
        Unit
    }

    @Test
    fun `dismissConfetti clears confetti and message`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)

        vm.redeemReward("r1")
        vm.confirmRedemption()

        delay(500)

        vm.dismissConfetti()

        assertFalse(vm.uiState.value.showConfetti)
        assertEquals("", vm.uiState.value.redemptionSuccessMessage)
    }

    @Test
    fun `progressPercent is calculated correctly`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RewardsViewModel(repo)
        delay(500)
        val escudo = vm.uiState.value.rewards.find { it.reward.id == "r1" }

        assertTrue(escudo != null)
        assertEquals(1f, escudo.progressPercent, 0.01f)
    }
}
