package me.martinrichards

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
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
    @RunOnVertxContext
    fun shouldUpdateExchange(asserter: UniAsserter): Unit =
        run {
            asserter.assertEquals(
                {
                    iexService.updateExchanges().collect().asList()
                        .map { exchanges -> exchanges.size }
                },
                11,
            )
            return
        }

    @Test
    @RunOnVertxContext
    fun shouldSaveExchange(asserter: UniAsserter): Unit =
        run {
            val exchange = Exchange("AIXK", "KZ", "description", "mic", "suffix")
            asserter.assertEquals({
                iexService.saveExchange(exchange)
            }, "AIXK")
            return
        }

    @Test
    @RunOnVertxContext
    fun shouldUpdateSymbolsForExchange(asserter: UniAsserter): Unit =
        run {
            asserter.execute {
                exchangeRepo.upsert(
                    Exchange(
                        "BATS",
                        "US",
                        "description",
                        "mic",
                        "suffix",
                    ),
                )
            }.assertEquals({
                iexService.updateSymbols("BATS")
                    .collect().asList().map { symbols -> symbols.size }
            }, 11)
            return
        }

    @Test
    @RunOnVertxContext
    fun shouldSaveSymbols(asserter: UniAsserter): Unit =
        run {
            asserter.assertThat({
                exchangeRepo.upsert(
                    Exchange(
                        "BATS",
                        "US",
                        "description",
                        "mic",
                        "suffix",
                    ),
                ).flatMap { exchange ->
                    val symbol =
                        Symbol(
                            "ACWV",
                            exchange.code,
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

                    iexService.saveSymbol(symbol)
                }
            }, {
                assertEquals("BATS", it?.exchange)
                assertEquals("ACWV", it?.code)
            })
            return
        }

    @Test
    @RunOnVertxContext
    fun shouldUpdatePrices(asserter: UniAsserter): Unit =
        run {
            asserter.assertThat({
                exchangeRepo.upsert(
                    Exchange(
                        "BATS",
                        "US",
                        "description",
                        "mic",
                        "suffix",
                    ),
                ).flatMap { exchange ->
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
                    ).flatMap {
                        iexService.updatePrices(it.toDTO()).collect().asList()
                    }
                }
            }, {
                assertEquals(0, it.size)
                // assert(it.first().symbol == "ACWV")
            })
            return
        }

    @Test
    @RunOnVertxContext
    fun shouldSavePrices(asserter: UniAsserter): Unit =
        run {
            asserter.assertThat({
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
            }, {
                assertEquals(it.code, "ACWV")
            })
        }

    @Test
    fun shouldUpdateMarketData(): Unit = run { TODO() }

    @Test
    fun shouldUpdateMarketDataForExchange(): Unit = run { TODO() }
}
