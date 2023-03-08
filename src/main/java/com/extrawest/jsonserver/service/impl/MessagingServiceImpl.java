package com.extrawest.jsonserver.service.impl;

import com.extrawest.jsonserver.model.ChargePoint;
import com.extrawest.jsonserver.model.emun.ImplementedMessageType;
import com.extrawest.jsonserver.model.exception.BddTestingException;
import com.extrawest.jsonserver.repository.BddDataRepository;
import com.extrawest.jsonserver.repository.ServerSessionRepository;
import com.extrawest.jsonserver.service.MessagingService;
import com.extrawest.jsonserver.validation.incoming.confirmation.*;
import com.extrawest.jsonserver.validation.incoming.request.*;
import com.extrawest.jsonserver.validation.outcoming.confirmation.*;
import com.extrawest.jsonserver.validation.outcoming.request.*;
import com.extrawest.jsonserver.ws.JsonWsServer;
import com.extrawest.jsonserver.ws.handler.ServerCoreEventHandlerImpl;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.*;
import eu.chargetime.ocpp.model.firmware.DiagnosticsStatusNotificationRequest;
import eu.chargetime.ocpp.model.firmware.FirmwareStatusNotificationRequest;
import eu.chargetime.ocpp.model.firmware.UpdateFirmwareConfirmation;
import eu.chargetime.ocpp.model.localauthlist.SendLocalListConfirmation;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageConfirmation;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest;
import eu.chargetime.ocpp.model.reservation.CancelReservationConfirmation;
import eu.chargetime.ocpp.model.smartcharging.ClearChargingProfileConfirmation;
import eu.chargetime.ocpp.model.smartcharging.GetCompositeScheduleConfirmation;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileConfirmation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.extrawest.jsonserver.util.TimeUtil.waitHalfSecond;
import static com.extrawest.jsonserver.util.TimeUtil.waitOneSecond;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {
    private final ApplicationContext springBootContext;
    private final BddDataRepository bddDataRepository;
    private final JsonWsServer server;
    private final ServerSessionRepository sessionRepository;

    private final AuthorizeRequestBddHandler authorizeRequestBddHandler;
    private final AuthorizeConfirmationBddHandler authorizeConfirmationBddHandler;
    private final BootNotificationRequestBddHandler bootNotificationRequestBddHandler;
    private final BootNotificationConfirmationBddHandler bootNotificationConfirmationBddHandler;
    private final DataTransferIncomingRequestBddHandler dataTransferIncomingRequestBddHandler;
    private final DataTransferOutcomingConfirmationBddHandler dataTransferOutcomingConfirmationBddHandler;
    private final DiagnosticsStatusNotificationRequestBddHandler diagnosticsStatusNotificationRequestBddHandler;
    private final DiagnosticsStatusNotificationConfirmationBddHandler
            diagnosticsStatusNotificationConfirmationBddHandler;
    private final FirmwareStatusNotificationRequestBddHandler firmwareStatusNotificationRequestBddHandler;
    private final FirmwareStatusNotificationConfirmationBddHandler firmwareStatusNotificationConfirmationBddHandler;
    private final HeartbeatRequestBddHandler heartbeatRequestBddHandler;
    private final HeartbeatConfirmationBddHandler heartbeatConfirmationBddHandler;
    private final MeterValuesRequestBddHandler meterValuesRequestBddHandler;
    private final MeterValuesConfirmationBddHandler meterValuesConfirmationBddHandler;
    private final StartTransactionRequestBddHandler startTransactionRequestBddHandler;
    private final StartTransactionConfirmationBddHandler startTransactionConfirmationBddHandler;
    private final StatusNotificationRequestBddHandler statusNotificationRequestBddHandler;
    private final StatusNotificationConfirmationBddHandler statusNotificationConfirmationBddHandler;
    private final StopTransactionRequestBddHandler stopTransactionRequestBddHandler;
    private final StopTransactionConfirmationBddHandler stopTransactionConfirmationBddHandler;

    private final CancelReservationRequestBddHandler cancelReservationRequestBddHandler;
    private final CancelReservationConfirmationBddHandler cancelReservationConfirmationBddHandler;
    private final ChangeAvailabilityRequestBddHandler changeAvailabilityRequestBddHandler;
    private final ChangeAvailabilityConfirmationBddHandler changeAvailabilityConfirmationBddHandler;
    private final ChangeConfigurationRequestBddHandler changeConfigurationRequestBddHandler;
    private final ChangeConfigurationConfirmationBddHandler changeConfigurationConfirmationBddHandler;
    private final ClearCacheRequestBddHandler clearCacheRequestBddHandler;
    private final ClearCacheConfirmationBddHandler clearCacheConfirmationBddHandler;
    private final ClearChargingProfileRequestBddHandler clearChargingProfileRequestBddHandler;
    private final ClearChargingProfileConfirmationBddHandler clearChargingProfileConfirmationBddHandler;
    private final DataTransferOutcomingRequestBddHandler dataTransferOutComingRequestBddHandler;
    private final DataTransferIncomingConfirmationBddHandler dataTransferIncomingConfirmationBddHandler;
    private final GetCompositeScheduleRequestBddHandler getCompositeScheduleRequestBddHandler;
    private final GetCompositeScheduleConfirmationBddHandler getCompositeScheduleConfirmationBddHandler;
    private final GetConfigurationRequestBddHandler getConfigurationRequestBddHandler;
    private final GetConfigurationConfirmationBddHandler getConfigurationConfirmationBddHandler;
    private final ResetRequestBddHandler resetRequestBddHandler;
    private final ResetConfirmationBddHandler resetConfirmationBddHandler;
    private final SendLocalListRequestBddHandler sendLocalListRequestBddHandler;
    private final SendLocalListConfirmationBddHandler sendLocalListConfirmationBddHandler;
    private final SetChargingProfileRequestBddHandler setChargingProfileRequestBddHandler;
    private final SetChargingProfileConfirmationBddHandler setChargingProfileConfirmationBddHandler;
    private final TriggerMessageRequestBddHandler triggerMessageRequestBddHandler;
    private final TriggerMessageConfirmationBddHandler triggerMessageConfirmationBddHandler;
    private final UnlockConnectorRequestBddHandler unlockConnectorRequestBddHandler;
    private final UnlockConnectorConfirmationBddHandler unlockConnectorConfirmationBddHandler;
    private final UpdateFirmwareRequestBddFactory updateFirmwareRequestBddFactory;
    private final UpdateFirmwareConfirmationBddHandler updateFirmwareConfirmationBddHandler;

    @Override
    public ImplementedMessageType sendRequest(String chargePointId, ImplementedMessageType type,
                                              Map<String, String> params) {
        UUID sessionUUID = sessionRepository.getSessionByChargerId(chargePointId);
        Request request;
        ImplementedMessageType requestedMessageType = null;
        switch (type) {
            case CANCEL_RESERVATION -> request = cancelReservationRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case CHANGE_AVAILABILITY -> request = changeAvailabilityRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case CHANGE_CONFIGURATION -> request = changeConfigurationRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case CLEAR_CACHE -> request = clearCacheRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case CLEAR_CHARGING_PROFILE -> request = clearChargingProfileRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case DATA_TRANSFER -> request = dataTransferOutComingRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case GET_COMPOSITE_SCHEDULE -> request = getCompositeScheduleRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case GET_CONFIGURATION -> request = getConfigurationRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case RESET -> request = resetRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case SEND_LOCAL_LIST -> request = sendLocalListRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case SET_CHARGING_PROFILE -> request = setChargingProfileRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case TRIGGER_MESSAGE -> {
                TriggerMessageRequest message = triggerMessageRequestBddHandler
                        .createMessageWithValidatedParams(params);
                bddDataRepository.addRequestedMessageType(chargePointId, message.getRequestedMessage());
                requestedMessageType = ImplementedMessageType.fromValue(message.getRequestedMessage().name());
                request = message;
            }
            case UNLOCK_CONNECTOR -> request = unlockConnectorRequestBddHandler
                    .createMessageWithValidatedParams(params);
            case UPDATE_FIRMWARE -> request = updateFirmwareRequestBddFactory.createMessageWithValidatedParams(params);
            default -> throw new BddTestingException("Request message type is unavailable");
        }
        sendRequest(sessionUUID, request);
        return requestedMessageType;
    }

    private void sendRequest(UUID sessionUUID, Request request) {
        try {
            server.send(sessionUUID, request);
        } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CompletableFuture<Confirmation>> waitForSuccessfulResponse(UUID sessionIndex, int waitingTimeSec,
                                                                               Map<String, String> parameters) {
        Optional<CompletableFuture<Confirmation>> completed = bddDataRepository.getCompleted(sessionIndex);
        if (completed.isEmpty()) {
            log.info(String.format("Waiting for successful response, up to %s seconds...", waitingTimeSec));
            for (int i = 1; i <= waitingTimeSec; i++) {
                completed = bddDataRepository.getCompleted(sessionIndex);
                if (completed.isPresent()) {
                    return completed;
                }
                waitOneSecond();
            }
        }
        return completed;
    }

    @Override
    public Optional<Request> waitForRequestedMessage(ChargePoint chargePoint,
                                                     int waitingTimeSec,
                                                     ImplementedMessageType type) {
        String chargePointId = chargePoint.getChargePointId();
        Optional<List<ImplementedMessageType>> messageTypes = bddDataRepository.getRequestedMessageTypes(chargePointId);
        if (messageTypes.isEmpty() || !messageTypes.get().contains(type)) {
            throw new BddTestingException("There are no message requests awaiting with type: " + type);
        }
        Optional<List<Request>> requestedMessages = bddDataRepository.getRequestedMessage(chargePointId);
        Optional<Request> request = Optional.empty();
        if (requestedMessages.isPresent()) {
            request = getAndHandleIfListContainMessage(chargePointId, requestedMessages.get(), type);
            if (request.isPresent()) {
                return request;
            }
        }

        log.debug(String.format("Waiting for requested request, up to %s seconds...", waitingTimeSec));
        for (int i = 1; i <= waitingTimeSec; i++) {
            requestedMessages = bddDataRepository.getRequestedMessage(chargePointId);
            if (requestedMessages.isPresent()) {
                request = getAndHandleIfListContainMessage(chargePointId, requestedMessages.get(), type);
            }
            if (request.isPresent()) {
                break;
            }
            waitOneSecond();
        }

        return request;
    }

    private Optional<Request> getAndHandleIfListContainMessage(String chargePointId,
                                                               List<Request> requests,
                                                               ImplementedMessageType messageType) {
        if (Objects.isNull(requests) || requests.isEmpty()) {
            return Optional.empty();
        }
        for (Request request : requests) {
            switch (messageType) {
                case AUTHORIZE -> {
                    if (request instanceof AuthorizeRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case BOOT_NOTIFICATION -> {
                    if (request instanceof BootNotificationRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case DATA_TRANSFER -> {
                    if (request instanceof DataTransferRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case DIAGNOSTICS_STATUS_NOTIFICATION -> {
                    if (request instanceof DiagnosticsStatusNotificationRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case FIRMWARE_STATUS_NOTIFICATION -> {
                    if (request instanceof FirmwareStatusNotificationRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case HEARTBEAT -> {
                    if (request instanceof HeartbeatRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case METER_VALUES -> {
                    if (request instanceof MeterValuesRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case START_TRANSACTION -> {
                    if (request instanceof StartTransactionRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case STATUS_NOTIFICATION -> {
                    if (request instanceof StatusNotificationRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
                case STOP_TRANSACTION -> {
                    if (request instanceof StopTransactionRequest) {
                        bddDataRepository.removeRequestedMessage(chargePointId, request);
                        return Optional.of(request);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public ImplementedMessageType validateRequest(Map<String, String> parameters, Request request) {
        if (request instanceof BootNotificationRequest message) {
            bootNotificationRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.BOOT_NOTIFICATION;
        } else if (request instanceof AuthorizeRequest message) {
            authorizeConfirmationBddHandler.setReceivedIdTag(message.getIdTag());
            authorizeRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.AUTHORIZE;
        } else if (request instanceof DataTransferRequest message) {
            dataTransferIncomingRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.DATA_TRANSFER;
        } else if (request instanceof DiagnosticsStatusNotificationRequest message) {
            diagnosticsStatusNotificationRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.DIAGNOSTICS_STATUS_NOTIFICATION;
        } else if (request instanceof FirmwareStatusNotificationRequest message) {
            firmwareStatusNotificationRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.DIAGNOSTICS_STATUS_NOTIFICATION;
        } else if (request instanceof HeartbeatRequest message) {
            heartbeatRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.HEARTBEAT;
        } else if (request instanceof MeterValuesRequest message) {
            meterValuesRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.METER_VALUES;
        } else if (request instanceof StartTransactionRequest message) {
            startTransactionConfirmationBddHandler.setReceivedIdTag(message.getIdTag());
            startTransactionRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.START_TRANSACTION;
        } else if (request instanceof StatusNotificationRequest message) {
            statusNotificationRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.STATUS_NOTIFICATION;
        } else if (request instanceof StopTransactionRequest message) {
            stopTransactionConfirmationBddHandler.setReceivedIdTag(message.getIdTag());
            stopTransactionRequestBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            return ImplementedMessageType.STOP_TRANSACTION;
        } else {
             throw new BddTestingException("Type is not implemented. Request: " + request);
        }
    }

    @Override
    public Confirmation sendConfirmationResponse(Map<String, String> parameters,
                                                 ImplementedMessageType sendingMessageType) {
        ServerCoreEventHandlerImpl handler = springBootContext.getBean(ServerCoreEventHandlerImpl.class);
        while (Objects.nonNull(handler.getResponse())) {
            waitHalfSecond();
        }
        Confirmation response;
        switch (sendingMessageType) {
            case BOOT_NOTIFICATION -> response =
                    bootNotificationConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case AUTHORIZE ->  response =
                    authorizeConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case DATA_TRANSFER -> response =
                    dataTransferOutcomingConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case DIAGNOSTICS_STATUS_NOTIFICATION -> response =
                    diagnosticsStatusNotificationConfirmationBddHandler
                            .createMessageWithValidatedParams(parameters);
            case FIRMWARE_STATUS_NOTIFICATION -> response =
                    firmwareStatusNotificationConfirmationBddHandler
                            .createMessageWithValidatedParams(parameters);
            case HEARTBEAT -> response =
                    heartbeatConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case METER_VALUES -> response =
                    meterValuesConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case START_TRANSACTION -> response =
                    startTransactionConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case STATUS_NOTIFICATION -> response =
                    statusNotificationConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            case STOP_TRANSACTION -> response =
                    stopTransactionConfirmationBddHandler.createMessageWithValidatedParams(parameters);
            default ->
                    throw new BddTestingException("This type of confirmation message is not implemented. ");
        }

        handler.setResponse(response);
        while (Objects.nonNull(handler.getResponse())) {
            waitOneSecond();
        }

        return response;
    }

    @Override
    public void assertConfirmationMessage(Map<String, String> parameters,
                                          CompletableFuture<Confirmation> completableFuture) {
        try {
            Confirmation confirmation = completableFuture.get();
            if (confirmation instanceof CancelReservationConfirmation message) {
                cancelReservationConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof ChangeAvailabilityConfirmation message) {
                changeAvailabilityConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof ChangeConfigurationConfirmation message) {
                changeConfigurationConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof ClearCacheConfirmation message) {
                clearCacheConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof ClearChargingProfileConfirmation message) {
                clearChargingProfileConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof DataTransferConfirmation message) {
                dataTransferIncomingConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            }else if (confirmation instanceof GetCompositeScheduleConfirmation message) {
                getCompositeScheduleConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            }else if (confirmation instanceof GetConfigurationConfirmation message) {
                getConfigurationConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof ResetConfirmation message) {
                resetConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof SendLocalListConfirmation message) {
                sendLocalListConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof SetChargingProfileConfirmation message) {
                setChargingProfileConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof TriggerMessageConfirmation message) {
                triggerMessageConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof UpdateFirmwareConfirmation message) {
                updateFirmwareConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else if (confirmation instanceof UnlockConnectorConfirmation message) {
                unlockConnectorConfirmationBddHandler.validateAndAssertFieldsWithParams(parameters, message);
            } else {
                throw new BddTestingException("Type is not implemented. Confirmation: " + confirmation);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
