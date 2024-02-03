package me.martinrichards.repo

import io.quarkus.hibernate.reactive.panache.PanacheRepository
import io.quarkus.hibernate.reactive.panache.common.WithSessionOnDemand
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import java.util.UUID
import me.martinrichards.Price as IEXPrice

@Entity
@Table(name = "prices")
class Price() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator",
    )
    @Column(name = "id")
    lateinit var id: UUID

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "symbol_id", nullable = false)
    lateinit var symbol: Symbol

    @Column(name = "open")
    var open: Double? = null

    @Column(name = "low")
    var low: Double? = null

    @Column(name = "high")
    var high: Double? = null

    @Column(name = "close")
    var close: Double? = null

    @Column(name = "volume")
    var volume: Long? = null

    @Column(name = "date")
    lateinit var date: LocalDate

    @Column(name = "fully_unadjusted_open")
    var fullyUnadjustedOpen: Double? = null

    @Column(name = "fully_unadjusted_low")
    var fullyUnadjustedLow: Double? = null

    @Column(name = "fully_unadjusted_high")
    var fullyUnadjustedHigh: Double? = null

    @Column(name = "fully_unadjusted_close")
    var fullyUnadjustedClose: Double? = null

    @Column(name = "fully_unadjusted_volume")
    var fullyUnadjustedVolume: Long? = null

    @Column(name = "unadjusted_open")
    var unadjustedOpen: Double? = null

    @Column(name = "unadjusted_low")
    var unadjustedLow: Double? = null

    @Column(name = "unadjusted_high")
    var unadjustedHigh: Double? = null

    @Column(name = "unadjusted_close")
    var unadjustedClose: Double? = null

    @Column(name = "unadjusted_volume")
    var unadjustedVolume: Long? = null

    @Column(name = "priceDate")
    var priceDate: LocalDate? = null

    @Column(name = "updated")
    var updated: Long? = null

    constructor(symbol: Symbol, price: IEXPrice) : this() {
        this.symbol = symbol
        this.date = price.date
        this.open = price.open
        this.low = price.low
        this.high = price.high
        this.close = price.close
        this.volume = price.volume
        this.fullyUnadjustedOpen = price.fOpen
        this.fullyUnadjustedLow = price.fLow
        this.fullyUnadjustedHigh = price.fHigh
        this.fullyUnadjustedClose = price.fClose
        this.fullyUnadjustedVolume = price.fVolume
        this.unadjustedOpen = price.uOpen
        this.unadjustedLow = price.uLow
        this.unadjustedHigh = price.uHigh
        this.unadjustedClose = price.uClose
        this.unadjustedVolume = price.uVolume
        this.priceDate = price.priceDate
        this.updated = price.updated
    }

    fun toDTO(): PriceDTO {
        return PriceDTO(this)
    }

    fun code(): String = this.symbol.code
}

data class PriceDTO(
    val code: String,
    val date: LocalDate,
    val open: Double?,
    val low: Double?,
    val high: Double?,
    val close: Double?,
    val volume: Long?,
    val fullyUnadjustedOpen: Double?,
    val fullyUnadjustedLow: Double?,
    val fullyUnadjustedHigh: Double?,
    val fullyUnadjustedClose: Double?,
    val fullyUnadjustedVolume: Long?,
    val unadjustedOpen: Double?,
    val unadjustedLow: Double?,
    val unadjustedHigh: Double?,
    val unadjustedClose: Double?,
    val unadjustedVolume: Long?,
    val priceDate: LocalDate?,
    val updated: Long?,
) {
    constructor(price: Price) : this(
        price.code(), price.date, price.open, price.low, price.high, price.close, price.volume,
        price.fullyUnadjustedOpen, price.fullyUnadjustedLow, price.fullyUnadjustedHigh, price.fullyUnadjustedClose,
        price.fullyUnadjustedVolume, price.unadjustedOpen, price.unadjustedLow, price.unadjustedHigh,
        price.unadjustedClose, price.unadjustedVolume, price.priceDate, price.updated,
    )
}

@ApplicationScoped
class IEXPriceRepo : PanacheRepository<Price> {
    @WithSessionOnDemand
    fun lastPrice(code: String): Uni<Price?> {
        return find(
            "from Price as p where p.symbol.code = ?1",
            Sort.by("date"),
            code,
        )
            .firstResult<Price>()
    }
}
