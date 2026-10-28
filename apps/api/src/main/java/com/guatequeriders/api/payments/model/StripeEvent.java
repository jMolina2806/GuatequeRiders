package com.guatequeriders.api.payments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "stripe_events")
public class StripeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 255)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "object_id", length = 255)
    private String objectId;

    @Column(nullable = false)
    private boolean livemode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_attempt_id")
    private PaymentAttempt paymentAttempt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StripeEventStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = StripeEventStatus.RECEIVED;
        }
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStripeEventId() { return stripeEventId; }
    public void setStripeEventId(String stripeEventId) { this.stripeEventId = stripeEventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public boolean isLivemode() { return livemode; }
    public void setLivemode(boolean livemode) { this.livemode = livemode; }

    public PaymentAttempt getPaymentAttempt() { return paymentAttempt; }
    public void setPaymentAttempt(PaymentAttempt paymentAttempt) { this.paymentAttempt = paymentAttempt; }

    public StripeEventStatus getStatus() { return status; }
    public void setStatus(StripeEventStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
