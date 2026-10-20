package com.yoga.portal.repository;

import com.yoga.portal.model.Booking;
import com.yoga.portal.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReference(String reference);

    boolean existsByReference(String reference);

    List<Booking> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMemberIdAndSlotIdAndStatusIn(Long memberId, Long slotId, Collection<BookingStatus> statuses);
}
