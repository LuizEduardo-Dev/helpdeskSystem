package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findAllByTicketIdAndOrganizationIdOrderByCreatedAtAsc(Long ticketId, Long organizationId);
}