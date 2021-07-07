package com.rbkmoney.porter.listener.handler.party

import com.rbkmoney.damsel.payment_processing.PartyChange
import com.rbkmoney.machinegun.eventsink.MachineEvent
import com.rbkmoney.porter.listener.constant.HandleEventType
import com.rbkmoney.porter.listener.handler.ChangeHandler
import com.rbkmoney.porter.listener.handler.merge.PartyMerger
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import org.springframework.stereotype.Component

@Component
class PartyBlockingHandler(
    private val partyRepository: PartyRepository,
    private val partyMerger: PartyMerger,
) : ChangeHandler<PartyChange, MachineEvent> {

    override fun handleChange(change: PartyChange, event: MachineEvent) {
        val partyBlocking = change.partyBlocking
        val partyId = event.sourceId
        val partyEntity = partyRepository.findByPartyId(partyId) ?: PartyEntity()
        val updateParty = partyEntity.apply {
            this.partyId = partyId
            if (partyBlocking.isSetBlocked) {
                status = PartyStatus.blocked
            } else if (partyBlocking.isSetUnblocked) {
                status = PartyStatus.active
            }
        }
        partyMerger.mergeEvent(partyEntity, updateParty)
        partyRepository.save(updateParty)
    }

    override val changeType: HandleEventType = HandleEventType.PARTY_BLOCKING
}
