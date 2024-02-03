package me.martinrichards

import io.quarkus.vertx.SafeVertxContext
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.reactive.messaging.annotations.Merge
import jakarta.enterprise.context.ApplicationScoped
import me.martinrichards.repo.*
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import me.martinrichards.repo.Price as PriceEntity

@ApplicationScoped
class IEXService(
    @RestClient private val iexClient: IEXClient,
    @ConfigProperty(name = "iexcloud.token") private val token: String,
    private val exchangeRepo: IEXExchangeRepo,
    private val symbolRepo: IEXSymbolRepo,
    private val priceRepo: IEXPriceRepo,
) {
    @Outgoing("iex-exchange")
    fun updateExchanges(): Multi<Exchange> {
        return iexClient
            .listExchanges(token)
            .onItem()
            .transformToMulti { exchanges ->
                Multi.createFrom().iterable(exchanges)
            }
    }

    @Merge
    @SafeVertxContext
    @Incoming("iex-exchange")
    @Outgoing("exchange-code")
    fun saveExchange(exchange: Exchange): Uni<String> {
        return this.exchangeRepo.upsert(exchange).map { ex -> ex.code }
    }

    @Incoming("exchange-code")
    @Outgoing("iex-symbol")
    fun updateSymbols(exchangeCode: String): Multi<Symbol> {
        return iexClient.listSymbols(exchangeCode, token)
            .onItem()
            .transformToMulti { symbols ->
                Multi.createFrom().iterable(symbols)
            }
    }

    @Incoming("iex-symbol")
    @Outgoing("symbol")
    fun saveSymbol(symbol: Symbol): Uni<SymbolDTO?> {
        return this.exchangeRepo
            .findByCode(symbol.exchange)
            .flatMap {
                it?.let {
                    this.symbolRepo.persist(Symbol(it, symbol))
                }
            }.map { it?.toDTO() }
    }

    @Merge
    @Incoming("symbol")
    @Outgoing("iex-price")
    fun updatePrices(symbol: SymbolDTO): Multi<Price> {
        return this.priceRepo
            .lastPrice(symbol.code)
            .onItem()
            .transformToMulti { lastPrice ->
                val range = calculateRange(lastPrice)
                this.iexClient
                    .listRangePrices(symbol.code, range.name, this.token)
                    .onItem()
                    .transformToMulti { prices -> Multi.createFrom().iterable(prices) }
            }
    }

    @Incoming("iex-price")
    fun savePrice(price: Price): Uni<PriceDTO> {
        return this.symbolRepo
            .findByCode(price.symbol)
            .flatMap {
                it?.let {
                    this.priceRepo.persist(PriceEntity(it, price))
                }
            }.map { it.toDTO() }
    }

    fun updateMarketData(): Multi<String>? {
        return this.exchangeRepo.listExchanges()
            .onItem()
            .transformToMulti { exchanges ->
                Multi.createFrom().iterable(exchanges.map { ex -> ex.code })
            }
    }

    @Incoming("exchange-market-data-update")
    @Outgoing("symbol")
    fun updateMarketDataForExchange(exchange: String): Multi<String> {
        return this.symbolRepo
            .listByExchangeCode(exchange)
            .onItem()
            .transformToMulti { symbols ->
                Multi.createFrom().iterable(symbols.map { symbol -> symbol.code })
            }
    }

    private fun calculateRange(lastPrice: PriceEntity?): RANGE {
        val now = LocalDate.now()
        val range =
            lastPrice?.let {
                val range =
                    when (ChronoUnit.DAYS.between(now, it.date)) {
                        in (Long.MIN_VALUE..0) -> RANGE.ZERO
                        in (1..5) -> RANGE.FIVE_DAYS
                        in (6..30) -> RANGE.ONE_MONTH
                        in (31..90) -> RANGE.THREE_MONTHS
                        in (91..180) -> RANGE.THREE_MONTHS
                        in (181..365) -> RANGE.ONE_YEAR
                        in (366..730) -> RANGE.TWO_YEARS
                        in (731..1825) -> RANGE.FIVE_YEARS
                        else -> RANGE.MAX
                    }
                range
            } ?: RANGE.MAX
        return range
    }
}
