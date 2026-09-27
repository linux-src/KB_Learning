package dev.kbwallet.app.portfolio.presentation

import app.cash.turbine.test
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.data.FakePortfolioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortfolioViewModelTest {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var portfolioRepository: FakePortfolioRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        portfolioRepository = FakePortfolioRepository()
        viewModel = PortfolioViewModel(
            portfolioRepository = portfolioRepository,
            coroutineDispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `State and portfolio coins are properly combined`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.coins.isEmpty())

            val portfolioCoin = FakePortfolioRepository.portfolioCoin
            portfolioRepository.savePortfolioCoin(portfolioCoin)

            awaitItem() // Ignore the first emission
            val updatedState = awaitItem()
            assertTrue(updatedState.coins.isNotEmpty())
            assertFalse(updatedState.isLoading)
            assertEquals(FakePortfolioRepository.portfolioCoin.coin.id, updatedState.coins.first().id)
        }
    }

    @Test
    fun `Total value updates when a coin is added`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(initialState.totalValue, formatFiat(10000.0))

            val portfolioCoin = FakePortfolioRepository.portfolioCoin.copy(
                ownedAmountInUnit = 50.0,
                ownedAmountInFiat = 1000.0
            )
            portfolioRepository.savePortfolioCoin(portfolioCoin)
            val updatedState = awaitItem()
            assertEquals(formatFiat(11000.0), updatedState.totalValue)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cash and holdings are reported separately, not folded into one figure`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            // Nothing bought yet: everything is cash, nothing is in assets.
            assertEquals(formatFiat(10000.0), initialState.cashBalance)
            assertEquals(formatFiat(0.0), initialState.holdingsValue)

            portfolioRepository.savePortfolioCoin(
                FakePortfolioRepository.portfolioCoin.copy(
                    ownedAmountInUnit = 50.0,
                    ownedAmountInFiat = 1000.0,
                )
            )
            val updatedState = awaitItem()

            assertEquals(formatFiat(10000.0), updatedState.cashBalance, "cash is untouched by the holding")
            assertEquals(formatFiat(1000.0), updatedState.holdingsValue)
            assertEquals(formatFiat(11000.0), updatedState.totalValue, "the total is the sum of the two")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `spending cash moves value from the cash balance into holdings`() = runTest {
        viewModel.state.test {
            awaitItem()

            // What a purchase does: cash down, holdings up, total unchanged.
            portfolioRepository.updateCashBalance(7500.0)
            portfolioRepository.savePortfolioCoin(
                FakePortfolioRepository.portfolioCoin.copy(
                    ownedAmountInUnit = 25.0,
                    ownedAmountInFiat = 2500.0,
                )
            )

            val state = viewModel.state.value
            assertEquals(formatFiat(7500.0), state.cashBalance)
            assertEquals(formatFiat(2500.0), state.holdingsValue)
            assertEquals(formatFiat(10000.0), state.totalValue)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Loading state and error message update on failure`() = runTest {
        portfolioRepository.simulateError()

        viewModel.state.test {
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(DataError.Remote.SERVER.toUiText(), errorState.error)
        }
    }
}