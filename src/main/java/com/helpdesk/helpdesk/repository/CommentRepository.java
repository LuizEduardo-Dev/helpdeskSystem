package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
