package com.rbkmoney.porter.listener.handler.party

import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.listener.constant.HandleEventType
import com.rbkmoney.porter.listener.handler.ChangeHandler
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import org.springframework.stereotype.Component

@Component
class PartyCreateHandler(
    private val partyRepository: PartyRepository,
) : ChangeHandler<PartyChange, MachineEvent> {

    override fun handleChange(change: PartyChange, parent: MachineEvent) {
        val partyCreated = change.partyCreated
        val partyCreatedAt = TypeUtil.stringToLocalDateTime(partyCreated.createdAt)
        val partyEntity = PartyEntity().apply {
            partyId = partyCreated.id
            createdAt = partyCreatedAt
            email = partyCreated.contact_info.email
            status = PartyStatus.active
        }
        partyRepository.save(partyEntity)
    }

    override val changeType: HandleEventType = HandleEventType.PARTY_CREATED
}
