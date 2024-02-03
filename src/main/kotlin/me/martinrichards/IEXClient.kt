package me.martinrichards

import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.time.LocalDate

class Exchange() {
    lateinit var exchange: String
    lateinit var region: String
    lateinit var description: String
    lateinit var mic: String
    lateinit var suffix: String

    constructor(
        exchange: String,
        region: String,
        description: String,
        mic: String,
        suffix: String,
    ) : this() {
        this.exchange = exchange
        this.region = region
        this.description = description
        this.mic = mic
        this.suffix = suffix
    }
}

class Symbol() {
    lateinit var name: String
    lateinit var region: String
    lateinit var symbol: String
    lateinit var type: String
    lateinit var currency: String
    lateinit var date: LocalDate
    lateinit var exchange: String
    var cik: String? = null
    var figi: String? = null
    var iexId: String? = null
    var isEnabled: Boolean = false
    var lei: String? = null

    constructor(
        symbol: String,
        exchange: String,
        name: String,
        date: LocalDate,
        isEnabled: Boolean,
        type: String,
        region: String,
        currency: String,
        iexId: String,
        cik: String,
        figi: String,
        lei: String,
    ) : this() {
        this.symbol = symbol
        this.exchange = exchange
        this.name = name
        this.date = date
        this.isEnabled = isEnabled
        this.type = type
        this.region = region
        this.currency = currency
        this.iexId = iexId
        this.cik = cik
        this.figi = figi
        this.lei = lei
    }

    override fun toString(): String {
        return "name: $name, region: $region, symbol: $symbol, type: $type, currency: $currency, date: $date, " +
            "exchange: $exchange, cik: $cik, figi: $figi, iexId: $iexId, isEnabled: $isEnabled, lei: $lei"
    }
}

class Price() {
    lateinit var symbol: String
    var open: Double? = null
    var low: Double? = null
    var high: Double? = null
    var close: Double? = null
    var volume: Long? = null
    lateinit var date: LocalDate

    var fClose: Double? = null
    var fHigh: Double? = null
    var fLow: Double? = null
    var fOpen: Double? = null
    var fVolume: Long? = null

    var uClose: Double? = null
    var uHigh: Double? = null
    var uLow: Double? = null
    var uOpen: Double? = null
    var uVolume: Long? = null

    var priceDate: LocalDate? = null
    var updated: Long? = null

    constructor(
        symbol: String,
        date: LocalDate,
        open: Double,
        low: Double,
        high: Double,
        close: Double,
        volume: Long,
    ) : this() {
        this.symbol = symbol
        this.date = date
        this.open = open
        this.low = low
        this.high = high
        this.close = close
        this.volume = volume
    }
}

enum class RANGE(value: String) {
    YTD("ytd"),
    FIVE_YEARS("5y"),
    TWO_YEARS("2y"),
    ONE_YEAR("1y"),
    SIX_MONTHS("6m"),
    THREE_MONTHS("3m"),
    ONE_MONTH("1m"),
    FIVE_DAYS("5d"),
    DYNAMIC("dynamic"),
    MAX("max"),
    ZERO("zero"),
}

@RegisterRestClient(configKey = "iexcloud")
@ApplicationScoped
interface IEXClient {
    @Path("/ref-data/exchanges")
    @GET
    fun listExchanges(
        @QueryParam("token") token: String,
    ): Uni<List<Exchange>>

    @Path("/ref-data/exchange/{code}/symbols")
    @GET
    fun listSymbols(
        @PathParam("code") exchangeCode: String,
        @QueryParam("token") token: String,
    ): Uni<List<Symbol>>

    @Path("/stock/{code}/chart/{range}")
    @GET
    fun listRangePrices(
        @PathParam("code") symbolCode: String,
        @PathParam("range") range: String,
        @QueryParam("token") token: String,
    ): Uni<List<Price>>

    @Path("/stock/{code}/chart")
    @GET
    fun listLastPrices(
        @PathParam("code") symbolCode: String,
        @QueryParam("chartLast") last: Int,
        @QueryParam("token") token: String,
    ): Uni<List<Price>>
}
