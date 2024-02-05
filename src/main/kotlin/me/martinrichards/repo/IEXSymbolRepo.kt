package me.martinrichards.repo

import io.quarkus.hibernate.reactive.panache.PanacheRepository
import io.quarkus.hibernate.reactive.panache.common.WithSessionOnDemand
import io.quarkus.kafka.client.serialization.JsonbDeserializer
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
import kotlin.collections.ArrayList
import me.martinrichards.Symbol as IEXSymbol

@Entity
@Table(name = "symbols")
class Symbol() {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator",
    )
    @Column(name = "id")
    lateinit var id: UUID

    @Column(name = "code")
    lateinit var code: String

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "exchange_id", nullable = false)
    lateinit var exchangeEntity: ExchangeEntity

    @Column(name = "name")
    lateinit var name: String

    @Column(name = "date")
    lateinit var date: LocalDate

    @Column(name = "is_enabled")
    var isEnabled: Boolean = false

    @Column(name = "type")
    lateinit var type: String

    @Column(name = "region")
    lateinit var region: String

    @Column(name = "currency")
    lateinit var currency: String

    @Column(name = "iexId")
    var iexId: String? = null

    constructor(exchangeEntity: ExchangeEntity, iexSymbol: IEXSymbol) : this() {
        this.code = iexSymbol.symbol.trim()
        this.exchangeEntity = exchangeEntity
        this.name = iexSymbol.name.trim()
        this.date = iexSymbol.date
        this.isEnabled = iexSymbol.isEnabled
        this.type = iexSymbol.type.trim()
        this.region = iexSymbol.region.trim()
        this.currency = iexSymbol.currency.trim()
        this.iexId = iexSymbol.iexId?.trim()
    }

    fun update(iexSymbol: IEXSymbol): Symbol {
        this.code = iexSymbol.symbol.trim()
        this.exchangeEntity = exchangeEntity
        this.name = iexSymbol.name.trim()
        this.date = iexSymbol.date
        this.isEnabled = iexSymbol.isEnabled
        this.type = iexSymbol.type.trim()
        this.region = iexSymbol.region.trim()
        this.currency = iexSymbol.currency.trim()
        this.iexId = iexSymbol.iexId?.trim()
        return this
    }

    fun toDTO(): SymbolDTO {
        return SymbolDTO(this)
    }
}

data class SymbolDTO(
    val id: UUID,
    val code: String,
    val exchange: String,
    val name: String,
    val date: LocalDate,
    val isEnabled: Boolean,
    val type: String,
    val region: String,
    val currency: String,
    val iexId: String?,
) {
    constructor(symbol: Symbol) : this(
        symbol.id, symbol.code, symbol.exchangeEntity.code, symbol.name, symbol.date, symbol.isEnabled, symbol.type,
        symbol.region, symbol.currency, symbol.iexId,
    )
}

class SymbolDeserializer() : JsonbDeserializer<SymbolDTO>(SymbolDTO::class.java)

class ListOfSymbolDeserializer() : JsonbDeserializer<SymbolDTO>(
    object :
        ArrayList<SymbolDTO>() {}.javaClass.genericSuperclass,
)

@ApplicationScoped
class IEXSymbolRepo : PanacheRepository<Symbol> {
    @WithSessionOnDemand
    fun findByCode(code: String): Uni<Symbol?> {
        return find("code", code).firstResult()
    }

    @WithSessionOnDemand
    fun listByExchangeCode(code: String): Uni<List<Symbol>> {
        return list("from symbols where symbols.exchange.code = ?1", code)
    }

    @WithSessionOnDemand
    fun upsert(
        exchangeEntity: ExchangeEntity,
        iexSymbol: IEXSymbol,
    ): Uni<Symbol> {
        return this.findByCode(iexSymbol.symbol).flatMap { entity ->
            val symbol =
                entity?.update(iexSymbol) ?: Symbol(exchangeEntity, iexSymbol)
            this.persist(symbol)
        }
    }
}
