package com.yoga.portal.repository;

import com.yoga.portal.model.YogaSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YogaSlotRepository extends JpaRepository<YogaSlot, Long> {

    List<YogaSlot> findAllByOrderByStartTimeAsc();
}
