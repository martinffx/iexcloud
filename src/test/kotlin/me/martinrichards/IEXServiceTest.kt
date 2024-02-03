package me.martinrichards

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asFlow
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.inject.Inject
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.martinrichards.repo.IEXExchangeRepo
import me.martinrichards.repo.IEXSymbolRepo
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.time.LocalDate

@QuarkusTest
class IEXServiceTest {
    @Inject
    lateinit var iexService: IEXService

    @Inject
    lateinit var exchangeRepo: IEXExchangeRepo

    @Inject
    lateinit var symbolRepo: IEXSymbolRepo

    @InjectMock
    @RestClient
    lateinit var iexClient: IEXClient

    @BeforeEach
    fun setup() {
        val date = LocalDate.parse("2020-01-01")
        val exchanges =
            listOf(
                Exchange("AIXK", "KZ", "description", "mic", "suffix"),
                Exchange("APXL", "AU", "description", "mic", "suffix"),
                Exchange("ARCX", "US", "description", "mic", "suffix"),
                Exchange("ASEX", "GR", "description", "mic", "suffix"),
                Exchange("BATS", "US", "description", "mic", "suffix"),
                Exchange("BCSE", "BY", "description", "mic", "suffix"),
                Exchange("BSEX", "AZ", "description", "mic", "suffix"),
                Exchange("BVMF", "BR", "description", "mic", "suffix"),
                Exchange("BVMF", "BR", "description", "mic", "suffix"),
                Exchange("BVMF", "BR", "description", "mic", "suffix"),
                Exchange("BVMF", "BR", "description", "mic", "suffix"),
            )
        Mockito.`when`(iexClient.listExchanges(anyString())).thenReturn(Uni.createFrom().item(exchanges))

        val symbols =
            listOf(
                Symbol(
                    "AAA", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAB", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAC", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAD", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAE", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAF", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAG", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAH", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAI", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAJ", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
                Symbol(
                    "AAK", "BATS", "name", date,
                    true, "type", "US", "USD",
                    "iexId", "cik", "figi", "lei",
                ),
            )
        Mockito.`when`(iexClient.listSymbols(anyString(), anyString())).thenReturn(Uni.createFrom().item(symbols))
    }

    @Test
    fun shouldUpdateExchange() =
        runBlocking {
            val exchanges = iexService.updateExchanges().asFlow().toList()
            assertEquals(11, exchanges.size)
        }

    @Test
    fun shouldSaveExchange() =
        runBlocking {
            val exchange = Exchange("AIXK", "KZ", "description", "mic", "suffix")
            val code = iexService.saveExchange(exchange).awaitSuspending()
            assertEquals("AIXK", code)
        }

    @Test
    fun shouldUpdateSymbolsForExchange() =
        runBlocking {
            val exchange =
                exchangeRepo.upsert(
                    Exchange(
                        "BATS",
                        "US",
                        "description",
                        "mic",
                        "suffix",
                    ),
                ).awaitSuspending()
            val symbols =
                iexService.updateSymbols("BATS")
                    .asFlow().toList()
            assertEquals(11, symbols.size)
        }

    @Test
    fun shouldSaveSymbols() =
        runBlocking {
            exchangeRepo.upsert(
                Exchange(
                    "BATS",
                    "US",
                    "description",
                    "mic",
                    "suffix",
                ),
            ).awaitSuspending()
            val symbol =
                Symbol(
                    "ACWV",
                    "BATS",
                    "BlackRock Institutional Trust Company N.A. - iShares MSCI Global Min Vol Factor ETF",
                    LocalDate.parse("2022-09-03"),
                    false,
                    "et",
                    "US",
                    "USD",
                    "IEX_53424D5052542D52",
                    "0000913414",
                    "BBG0025X38X0",
                    "549300BPYHDEDI59G670",
                )

            val entity = iexService.saveSymbol(symbol).awaitSuspending()
            assertEquals("BATS", entity?.exchange)
            assertEquals("ACWV", entity?.code)
        }

    @Test
    fun shouldUpdatePrices() =
        runBlocking {
            val exchange =
                exchangeRepo.upsert(
                    Exchange(
                        "BATS",
                        "US",
                        "description",
                        "mic",
                        "suffix",
                    ),
                ).awaitSuspending()
            val symbol =
                symbolRepo.upsert(
                    exchange,
                    Symbol(
                        "ACWV",
                        "BATS",
                        "BlackRock Institutional Trust Company N.A. - iShares MSCI Global Min Vol Factor ETF",
                        LocalDate.parse("2022-09-03"),
                        false,
                        "et",
                        "US",
                        "USD",
                        "IEX_53424D5052542D52",
                        "0000913414",
                        "BBG0025X38X0",
                        "549300BPYHDEDI59G670",
                    ),
                ).awaitSuspending()

            val prices = iexService.updatePrices(symbol.toDTO()).asFlow().toList()
            assert(prices.size == 11)
            assert(prices.first().symbol == symbol.code)
        }

    @Test
    fun shouldSavePrices(): Unit =
        runBlocking {
            val price =
                Price(
                    "ACWV",
                    LocalDate.now(),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0,
                )
            iexService.savePrice(price)
        }

    @Test
    fun shouldUpdateMarketData(): Unit = run { TODO() }

    @Test
    fun shouldUpdateMarketDataForExchange(): Unit = run { TODO() }
}
