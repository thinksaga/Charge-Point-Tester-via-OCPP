package com.extrawest.jsonserver.validation.outcoming.confirmation;

import java.util.Collections;
import java.util.Map;
import com.extrawest.jsonserver.validation.outcoming.OutgoingMessageFactory;
import com.extrawest.jsonserver.validation.outcoming.OutcomingMessageFieldsValidationFactory;
import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatConfirmationBddHandler
        extends OutcomingMessageFieldsValidationFactory<HeartbeatConfirmation>
        implements OutgoingMessageFactory<HeartbeatConfirmation> {
    public static final String CURRENT_TIME_REQUIRED = "currentTime";
    @Value("${heartbeat.confirmation.currentTime:}")
    private String defaultCurrentTime;

    @PostConstruct
    private void init() {
        this.defaultValues = Map.of(
                CURRENT_TIME_REQUIRED, defaultCurrentTime
        );

        this.requiredFieldsSetup = Map.of(
                CURRENT_TIME_REQUIRED, (conf, timeStr) -> conf.setCurrentTime(
                        getValidatedZonedDateTimeOrCurrentTimeIfEmptyOrThrow(timeStr, defaultCurrentTime,
                                CURRENT_TIME_REQUIRED))
        );

        this.optionalFieldsSetup = Collections.emptyMap();
    }

    @Override
    public HeartbeatConfirmation createMessageWithValidatedParams(Map<String, String> params) {
        HeartbeatConfirmation request = super.createMessageWithValidatedParamsViaLibModel(params);
        log.debug(getParameterizeClassName() + ": " + request);
        return request;
    }

}
