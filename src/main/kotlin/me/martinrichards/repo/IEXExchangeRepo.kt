package me.martinrichards.repo

import io.quarkus.hibernate.reactive.panache.PanacheRepository
import io.quarkus.hibernate.reactive.panache.common.WithSessionOnDemand
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID
import me.martinrichards.Exchange as IEXExchange

data class ExchangeDTO(
    val id: UUID,
    val code: String,
    val region: String,
    val description: String,
    val mic: String,
    val suffix: String,
) {
    constructor(exchangeEntity: ExchangeEntity) : this(
        exchangeEntity.id,
        exchangeEntity.code,
        exchangeEntity.region,
        exchangeEntity.description,
        exchangeEntity.mic,
        exchangeEntity.suffix,
    )
}

@Entity
@Table(name = "exchanges")
class ExchangeEntity() {
    @Id
    @Column(name = "id")
    lateinit var id: UUID

    @Column(name = "code")
    lateinit var code: String

    @Column(name = "region")
    lateinit var region: String

    @Column(name = "description")
    lateinit var description: String

    @Column(name = "mic")
    lateinit var mic: String

    @Column(name = "suffix")
    lateinit var suffix: String

    @OneToMany(targetEntity = Symbol::class)
    lateinit var symbols: List<Symbol>

    constructor(
        exchange: IEXExchange,
    ) : this() {
        this.id = UUID.randomUUID()
        this.code = exchange.exchange
        this.region = exchange.region
        this.description = exchange.description
        this.mic = exchange.mic
        this.suffix = exchange.suffix
    }

    fun update(exchange: IEXExchange): ExchangeEntity {
        this.code = exchange.exchange
        this.region = exchange.region
        this.description = exchange.description
        this.mic = exchange.mic
        this.suffix = exchange.suffix
        return this
    }

    override fun toString() = "Exchange: $code, $region, $id"

    fun toDTO(): ExchangeDTO {
        return ExchangeDTO(this)
    }
}

@ApplicationScoped
class IEXExchangeRepo : PanacheRepository<ExchangeEntity> {
    @WithSessionOnDemand
    fun findByCode(code: String): Uni<ExchangeEntity?> {
        return find("code", code).firstResult()
    }

    @WithSessionOnDemand
    fun listExchanges(): Uni<List<ExchangeEntity>> {
        return listAll().map { it.toList() }
    }

    @WithTransaction
    fun upsert(iexExchange: IEXExchange): Uni<ExchangeEntity> {
        return this.findByCode(iexExchange.exchange).flatMap { entity ->
            val exchangeEntity =
                entity?.update(iexExchange) ?: ExchangeEntity(iexExchange)
            this.persist(exchangeEntity)
        }
    }
}
