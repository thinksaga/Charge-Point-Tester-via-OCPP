package com.extrawest.jsonserver.validation.confirmation;

import java.util.Collections;
import java.util.Map;
import com.extrawest.jsonserver.validation.ConfirmationFactory;
import com.extrawest.jsonserver.validation.ValidationAndAssertionConfirmationFieldsFactory;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartTransactionConfirmationBddHandler
        extends ValidationAndAssertionConfirmationFieldsFactory<StartTransactionConfirmation>
        implements ConfirmationFactory<StartTransactionConfirmation> {
    public static final String ID_TAG_INFO_REQUIRED = "idTagInfo";
    public static final String TRANSACTION_ID_REQUIRED = "transactionId";
    @Value("${startTransaction.confirmation.idTagInfo:}")
    private String defaultIdTagInfo;
    @Value("${startTransaction.confirmation.transactionId:1111}")
    private String defaultTransactionId;
    @Setter private String receivedIdTag = null;

    @PostConstruct
    private void init() {
        this.defaultValues = Map.of(
                ID_TAG_INFO_REQUIRED, defaultIdTagInfo,
                TRANSACTION_ID_REQUIRED, defaultTransactionId
        );

        this.requiredFieldsSetup = Map.of(
                ID_TAG_INFO_REQUIRED, (conf, idTag) -> conf.setIdTagInfo(
                        getValidatedIdTagInfo(idTag, defaultIdTagInfo, ID_TAG_INFO_REQUIRED, receivedIdTag)),
                TRANSACTION_ID_REQUIRED, (conf, transactionId) -> conf.setTransactionId(
                        getValidatedIntegerOrThrow(transactionId, defaultTransactionId, TRANSACTION_ID_REQUIRED))
                );

        this.optionalFieldsSetup = Collections.emptyMap();
    }

    @Override
    public void validateFields(Map<String, String> params) {
        super.validateConfirmationFields(params);
    }

    @Override
    public StartTransactionConfirmation createValidatedConfirmation(Map<String, String> params,
                                                                StartTransactionConfirmation response) {
        return super.createValidatedConfirmation(params, response);
    }

}
