package com.polezhaiev.analytics.repository;

import com.polezhaiev.analytics.model.SlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SlotBookingRepository extends JpaRepository<SlotBooking, Long>, JpaSpecificationExecutor<SlotBooking> {
}
