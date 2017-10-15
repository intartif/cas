package org.apereo.cas.ticket.registry;

import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.queue.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.DeleteTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.DeleteTicketsMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.UpdateTicketMessageQueueCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * This is {@link MessageQueueTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MessageQueueTicketRegistry extends DefaultTicketRegistry {
    /**
     * Queue destination name.
     */
    public static final String QUEUE_DESTINATION = "MessageQueueTicketRegistry";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueTicketRegistry.class);

    private final JmsTemplate jmsTemplate;
    private final StringBean id;

    public MessageQueueTicketRegistry(final JmsTemplate jmsTemplate, final StringBean id) {
        this.jmsTemplate = jmsTemplate;
        this.id = id;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        super.addTicket(ticket);
        publishMessageToQueue(new AddTicketMessageQueueCommand(id, ticket));
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final boolean result = super.deleteSingleTicket(ticketId);
        publishMessageToQueue(new DeleteTicketMessageQueueCommand(id, ticketId));
        return result;
    }

    @Override
    public long deleteAll() {
        final long result = super.deleteAll();
        publishMessageToQueue(new DeleteTicketsMessageQueueCommand(id));
        return result;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        final Ticket result = super.updateTicket(ticket);
        publishMessageToQueue(new UpdateTicketMessageQueueCommand(id, ticket));
        return result;
    }

    private void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        jmsTemplate.convertAndSend(QUEUE_DESTINATION, cmd,
                message -> {
                    LOGGER.debug("Sending message [{}] from ticket registry id [{}]", message, cmd.getId());
                    return message;
                });
    }
}
