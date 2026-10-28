package com.guatequeriders.api.notifications.service;

import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.eventregistrations.repository.EventRegistrationRepository;
import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.notifications.model.EmailNotification;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderStatus;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository userRepository;
    private final RouteRegistrationRepository routeRegistrationRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final NotificationTemplateService templateService;
    private final ApplicationEventPublisher eventPublisher;
    private final String publicUrl;

    public NotificationService(UserRepository userRepository,
                               RouteRegistrationRepository routeRegistrationRepository,
                               EventRegistrationRepository eventRegistrationRepository,
                               NotificationTemplateService templateService,
                               ApplicationEventPublisher eventPublisher,
                               @Value("${app.mail.public-url:http://localhost:5173}") String publicUrl) {
        this.userRepository = userRepository;
        this.routeRegistrationRepository = routeRegistrationRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.templateService = templateService;
        this.eventPublisher = eventPublisher;
        this.publicUrl = stripTrailingSlash(publicUrl);
    }

    public void notifyMerchCampaignOpened(MerchCampaign campaign) {
        publish(
                allUserEmails(),
                "Nueva campaña Merch abierta",
                templateService.render(
                        "Merch",
                        "Nueva campaña Merch",
                        "Ya puedes hacer tu pedido en la nueva campaña de Guateque Riders.",
                        List.of("Campaña #" + campaign.getId()),
                        "Ver Merch",
                        publicUrl + "/merch"
                )
        );
    }

    public void notifyOrderPaid(Order order) {
        publish(
                List.of(order.getCustomerEmail()),
                "Pago confirmado · Pedido #" + order.getId(),
                templateService.render(
                        "Merch",
                        "Pago confirmado",
                        "Hemos recibido correctamente el pago de tu pedido.",
                        List.of(
                                "Pedido #" + order.getId(),
                                "Importe: " + order.getTotal() + " €",
                                "Estado: Confirmado"
                        ),
                        "Ver mis pedidos",
                        publicUrl + "/profile"
                )
        );
    }

    public void notifyOrderStatusChanged(Order order) {
        String status = switch (order.getStatus()) {
            case IN_PRODUCTION -> "En producción";
            case READY -> "Listo para entregar";
            case DELIVERED -> "Entregado";
            default -> null;
        };
        if (status == null) {
            return;
        }

        String message = switch (order.getStatus()) {
            case IN_PRODUCTION -> "Tu pedido ya está en producción.";
            case READY -> "Tu pedido está listo para entregar.";
            case DELIVERED -> "Tu pedido se ha marcado como entregado.";
            default -> "";
        };

        publish(
                List.of(order.getCustomerEmail()),
                status + " · Pedido #" + order.getId(),
                templateService.render(
                        "Merch",
                        status,
                        message,
                        List.of("Pedido #" + order.getId(), "Estado: " + status),
                        "Ver mis pedidos",
                        publicUrl + "/profile"
                )
        );
    }

    public void notifyRoutePublished(Route route) {
        publish(
                allUserEmails(),
                "Nueva ruta · " + route.getTitle(),
                templateService.render(
                        "Rutas",
                        route.getTitle(),
                        "Se ha publicado una nueva ruta de Guateque Riders.",
                        routeDetails(route),
                        "Ver rutas",
                        publicUrl + "/routes"
                )
        );
    }

    public void notifyRouteUpdated(Route route) {
        publish(
                routeParticipantEmails(route.getId()),
                "Ruta actualizada · " + route.getTitle(),
                templateService.render(
                        "Rutas",
                        "Cambios en " + route.getTitle(),
                        "Se han actualizado los datos de una ruta a la que estás apuntado.",
                        routeDetails(route),
                        "Ver rutas",
                        publicUrl + "/routes"
                )
        );
    }

    public void notifyRouteCancelled(Route route) {
        publish(
                routeParticipantEmails(route.getId()),
                "Ruta cancelada · " + route.getTitle(),
                templateService.render(
                        "Rutas",
                        "Ruta cancelada",
                        "La ruta a la que estabas apuntado ha sido cancelada.",
                        routeDetails(route),
                        "Ver rutas",
                        publicUrl + "/routes"
                )
        );
    }

    public void notifyRouteRegistration(Route route, User user) {
        publish(
                List.of(user.getEmail()),
                "Inscripción confirmada · " + route.getTitle(),
                templateService.render(
                        "Rutas",
                        "Ya estás apuntado",
                        "Tu inscripción a la ruta se ha confirmado correctamente.",
                        routeDetails(route),
                        "Ver rutas",
                        publicUrl + "/routes"
                )
        );
    }

    public void notifyEventPublished(Event event) {
        publish(
                allUserEmails(),
                "Nuevo evento · " + event.getTitle(),
                templateService.render(
                        "Eventos",
                        event.getTitle(),
                        "Se ha publicado un nuevo evento de Guateque Riders.",
                        eventDetails(event),
                        "Ver eventos",
                        publicUrl + "/events"
                )
        );
    }

    public void notifyEventUpdated(Event event) {
        publish(
                eventParticipantEmails(event.getId()),
                "Evento actualizado · " + event.getTitle(),
                templateService.render(
                        "Eventos",
                        "Cambios en " + event.getTitle(),
                        "Se han actualizado los datos de un evento al que estás apuntado.",
                        eventDetails(event),
                        "Ver eventos",
                        publicUrl + "/events"
                )
        );
    }

    public void notifyEventStatusChanged(Event event) {
        String status = switch (event.getStatus()) {
            case CLOSED -> "Inscripciones cerradas";
            case CANCELLED -> "Evento cancelado";
            default -> null;
        };
        if (status == null) {
            return;
        }

        publish(
                eventParticipantEmails(event.getId()),
                status + " · " + event.getTitle(),
                templateService.render(
                        "Eventos",
                        status,
                        event.getStatus().name().equals("CANCELLED")
                                ? "El evento al que estabas apuntado ha sido cancelado."
                                : "Las inscripciones de este evento se han cerrado.",
                        eventDetails(event),
                        "Ver eventos",
                        publicUrl + "/events"
                )
        );
    }

    public void notifyEventRegistration(Event event, User user) {
        publish(
                List.of(user.getEmail()),
                "Inscripción confirmada · " + event.getTitle(),
                templateService.render(
                        "Eventos",
                        "Inscripción confirmada",
                        "Ya estás apuntado al evento.",
                        eventDetails(event),
                        "Ver eventos",
                        publicUrl + "/events"
                )
        );
    }

    public void notifyCircuitPublished(CircuitActivity activity) {
        publish(
                allUserEmails(),
                "Nueva actividad de circuito · " + activity.getTitle(),
                templateService.render(
                        "Circuitos",
                        activity.getTitle(),
                        "Se ha publicado una nueva actividad de circuito.",
                        circuitDetails(activity),
                        "Ver circuitos",
                        publicUrl + "/circuits"
                )
        );
    }

    public void notifyCircuitUpdated(CircuitActivity activity) {
        publish(
                allUserEmails(),
                "Circuito actualizado · " + activity.getTitle(),
                templateService.render(
                        "Circuitos",
                        "Cambios en " + activity.getTitle(),
                        "Se han actualizado los datos de una actividad de circuito publicada.",
                        circuitDetails(activity),
                        "Ver circuitos",
                        publicUrl + "/circuits"
                )
        );
    }

    public void notifyCircuitCancelled(CircuitActivity activity) {
        publish(
                allUserEmails(),
                "Actividad de circuito cancelada · " + activity.getTitle(),
                templateService.render(
                        "Circuitos",
                        "Actividad cancelada",
                        "Una actividad de circuito publicada ha sido cancelada.",
                        circuitDetails(activity),
                        "Ver circuitos",
                        publicUrl + "/circuits"
                )
        );
    }

    private void publish(List<String> recipients, String subject, String html) {
        List<String> normalized = recipients.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new EmailNotification(normalized, subject, html));
    }

    private List<String> allUserEmails() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(User::getEmail)
                .toList();
    }

    private List<String> routeParticipantEmails(Long routeId) {
        return routeRegistrationRepository.findAllByRouteId(routeId).stream()
                .map(registration -> registration.getUser().getEmail())
                .toList();
    }

    private List<String> eventParticipantEmails(Long eventId) {
        return eventRegistrationRepository.findAllByEventId(eventId).stream()
                .map(registration -> registration.getUser().getEmail())
                .toList();
    }

    private List<String> routeDetails(Route route) {
        return List.of(
                "Fecha: " + DATE.format(route.getDate()),
                "Quedada: " + TIME.format(route.getMeetingTime()),
                "Salida: " + TIME.format(route.getDepartureTime()),
                "Origen: " + route.getOrigin(),
                "Destino: " + route.getDestination()
        );
    }

    private List<String> eventDetails(Event event) {
        return List.of(
                "Fecha: " + DATE.format(event.getDate()),
                "Hora: " + TIME.format(event.getStartTime()),
                "Lugar: " + event.getLocation()
        );
    }

    private List<String> circuitDetails(CircuitActivity activity) {
        return List.of(
                "Fecha: " + DATE.format(activity.getDate()),
                "Hora: " + TIME.format(activity.getStartTime()),
                "Circuito: " + activity.getCircuitName(),
                "Lugar: " + activity.getLocation()
        );
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5173";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
