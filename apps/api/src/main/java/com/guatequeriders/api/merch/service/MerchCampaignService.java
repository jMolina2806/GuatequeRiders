package com.guatequeriders.api.merch.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.merch.dto.CampaignCloseResponse;
import com.guatequeriders.api.merch.dto.CampaignDashboardResponse;
import com.guatequeriders.api.merch.dto.CampaignHistoryEntryResponse;
import com.guatequeriders.api.merch.dto.CampaignStatsResponse;
import com.guatequeriders.api.merch.dto.MerchCampaignResponse;
import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.repository.MerchCampaignRepository;
import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.dto.PreparationOrdersResponse;
import com.guatequeriders.api.orders.dto.SupplierOrderBatchResponse;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderItem;
import com.guatequeriders.api.orders.model.PaymentStatus;
import com.guatequeriders.api.orders.model.SupplierOrderBatch;
import com.guatequeriders.api.orders.model.SupplierOrderBatchItem;
import com.guatequeriders.api.orders.model.SupplierOrderBatchOrder;
import com.guatequeriders.api.orders.repository.OrderRepository;
import com.guatequeriders.api.orders.repository.SupplierOrderBatchRepository;
import com.guatequeriders.api.users.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchCampaignService {

    private final MerchCampaignRepository campaignRepository;
    private final OrderRepository orderRepository;
    private final SupplierOrderBatchRepository batchRepository;

    public MerchCampaignService(MerchCampaignRepository campaignRepository,
                                OrderRepository orderRepository,
                                SupplierOrderBatchRepository batchRepository) {
        this.campaignRepository = campaignRepository;
        this.orderRepository = orderRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public MerchCampaign getCurrent() {
        return campaignRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", 0L));
    }

    @Transactional(readOnly = true)
    public List<CampaignHistoryEntryResponse> listCampaigns() {
        return campaignRepository.findAllByOrderByIdDesc().stream()
                .map(this::historyEntry)
                .toList();
    }

    @Transactional
    public MerchCampaign openCampaign(User openedBy) {
        if (campaignRepository.findByStatusForUpdate(CampaignStatus.OPEN).isPresent()) {
            throw new ConflictException("There is already an open Merch campaign");
        }

        MerchCampaign campaign = new MerchCampaign();
        campaign.setStatus(CampaignStatus.OPEN);
        campaign.setOpenedAt(LocalDateTime.now());
        campaign.setOpenedByUser(openedBy);
        try {
            return campaignRepository.saveAndFlush(campaign);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("There is already an open Merch campaign");
        }
    }

    @Transactional
    public CampaignCloseResponse close(Long campaignId, User closedBy) {
        MerchCampaign campaign = lockCampaign(campaignId);
        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new ConflictException("Only open Merch campaigns can be closed");
        }
        if (batchRepository.existsByCampaignId(campaignId)) {
            throw new ConflictException("This campaign already has a supplier order");
        }

        List<Order> paidOrders = orderRepository
                .findAllByCampaignIdAndPaymentStatusOrderByCreatedAtDescIdDesc(campaignId, PaymentStatus.PAID);
        SupplierOrderBatch batch = createSnapshot(campaign, paidOrders);
        batchRepository.saveAndFlush(batch);

        campaign.setStatus(CampaignStatus.CLOSED);
        campaign.setClosedAt(LocalDateTime.now());
        campaign.setClosedByUser(closedBy);
        campaignRepository.save(campaign);

        return new CampaignCloseResponse(
                MerchCampaignResponse.from(campaign),
                SupplierOrderBatchResponse.from(batch)
        );
    }

    @Transactional
    public MerchCampaign markOrdered(Long campaignId, User orderedBy) {
        MerchCampaign campaign = lockCampaign(campaignId);
        if (campaign.getStatus() != CampaignStatus.CLOSED) {
            throw new ConflictException("Only closed Merch campaigns can be marked as ordered");
        }
        if (!batchRepository.existsByCampaignId(campaignId)) {
            throw new ConflictException("This campaign has no supplier order");
        }
        campaign.setStatus(CampaignStatus.ORDERED);
        campaign.setOrderedAt(LocalDateTime.now());
        campaign.setOrderedByUser(orderedBy);
        return campaign;
    }

    @Transactional(readOnly = true)
    public CampaignDashboardResponse getDashboard(Long campaignId) {
        MerchCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", campaignId));
        List<Order> allOrders = orderRepository.findAllByCampaignIdOrderByCreatedAtDescIdDesc(campaignId);
        List<Order> paidOrders = allOrders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .toList();
        List<OrderResponse> excludedOrders = allOrders.stream()
                .filter(order -> order.getPaymentStatus() != PaymentStatus.PAID)
                .map(OrderResponse::from)
                .toList();
        List<OrderResponse> latePaymentOrders = allOrders.stream()
                .filter(Order::isLatePayment)
                .map(OrderResponse::from)
                .toList();

        PreparationOrdersResponse provisional = null;
        SupplierOrderBatchResponse supplierOrder = null;
        int paidUnits;
        if (campaign.getStatus() == CampaignStatus.OPEN) {
            provisional = PreparationOrdersResponse.from(paidOrders);
            paidUnits = provisional.totalUnits();
        } else {
            SupplierOrderBatch batch = batchRepository.findByCampaignId(campaignId)
                    .orElseThrow(() -> new IllegalStateException("Closed Merch campaign has no supplier order"));
            supplierOrder = SupplierOrderBatchResponse.from(batch);
            paidUnits = supplierOrder.totalUnits();
        }

        CampaignStatsResponse stats = new CampaignStatsResponse(
                allOrders.size(),
                (int) allOrders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.PAID).count(),
                (int) allOrders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.PENDING).count(),
                (int) allOrders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.FAILED).count(),
                (int) allOrders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.CANCELLED).count(),
                paidUnits
        );

        return new CampaignDashboardResponse(
                MerchCampaignResponse.from(campaign),
                stats,
                provisional,
                supplierOrder,
                excludedOrders,
                latePaymentOrders
        );
    }

    @Transactional(readOnly = true)
    public SupplierOrderBatchResponse getSupplierOrder(Long campaignId) {
        MerchCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", campaignId));
        if (campaign.getStatus() == CampaignStatus.OPEN) {
            throw new ConflictException("The supplier order is only available after closing the campaign");
        }
        SupplierOrderBatch batch = batchRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier order", campaignId));
        return SupplierOrderBatchResponse.from(batch);
    }

    @Transactional
    public MerchCampaign lockOpenCampaign() {
        return campaignRepository.findByStatusForUpdate(CampaignStatus.OPEN)
                .orElseThrow(() -> new ConflictException("Merch store is currently closed"));
    }

    private MerchCampaign lockCampaign(Long campaignId) {
        return campaignRepository.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", campaignId));
    }

    private CampaignHistoryEntryResponse historyEntry(MerchCampaign campaign) {
        List<Order> orders = orderRepository.findAllByCampaignIdOrderByCreatedAtDescIdDesc(campaign.getId());
        int paidOrders;
        int totalUnits;
        if (campaign.getStatus() == CampaignStatus.OPEN) {
            paidOrders = (int) orders.stream()
                    .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                    .count();
            totalUnits = orders.stream()
                    .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                    .flatMap(order -> order.getItems().stream())
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
        } else {
            SupplierOrderBatch batch = batchRepository.findByCampaignId(campaign.getId()).orElse(null);
            paidOrders = batch == null ? 0 : batch.getOrders().size();
            totalUnits = batch == null ? 0 : batch.getTotalUnits();
        }
        int pendingOrders = (int) orders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PENDING)
                .count();
        return new CampaignHistoryEntryResponse(
                MerchCampaignResponse.from(campaign),
                orders.size(),
                paidOrders,
                pendingOrders,
                totalUnits
        );
    }

    private SupplierOrderBatch createSnapshot(MerchCampaign campaign, List<Order> paidOrders) {
        SupplierOrderBatch batch = new SupplierOrderBatch();
        batch.setCampaign(campaign);
        batch.setClosedAt(LocalDateTime.now());

        int totalUnits = 0;
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2);
        for (Order order : paidOrders) {
            SupplierOrderBatchOrder batchOrder = new SupplierOrderBatchOrder();
            batchOrder.setOriginalOrderId(order.getId());
            batchOrder.setCustomerName(order.getCustomerName());
            batchOrder.setOrderCreatedAt(order.getCreatedAt());
            batchOrder.setTotal(order.getTotal());
            for (OrderItem item : order.getItems()) {
                SupplierOrderBatchItem batchItem = new SupplierOrderBatchItem();
                batchItem.setProductCode(item.getProductCode());
                batchItem.setProductName(item.getProductName());
                batchItem.setSize(item.getSize());
                batchItem.setQuantity(item.getQuantity());
                batchItem.setUnitPrice(item.getUnitPrice());
                batchItem.setSubtotal(item.getSubtotal());
                batchItem.setCustomName(item.getCustomName());
                batchOrder.addItem(batchItem);
                totalUnits += item.getQuantity();
            }
            batch.addOrder(batchOrder);
            totalAmount = totalAmount.add(order.getTotal());
        }
        batch.setTotalUnits(totalUnits);
        batch.setTotalAmount(totalAmount);
        return batch;
    }
}
