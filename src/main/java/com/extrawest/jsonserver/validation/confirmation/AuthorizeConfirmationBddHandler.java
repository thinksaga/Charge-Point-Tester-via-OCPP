package com.extrawest.jsonserver.validation.confirmation;

import java.util.Collections;
import java.util.Map;
import com.extrawest.jsonserver.validation.ConfirmationFactory;
import com.extrawest.jsonserver.validation.ValidationAndAssertionConfirmationFieldsFactory;
import eu.chargetime.ocpp.model.core.AuthorizeConfirmation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizeConfirmationBddHandler
        extends ValidationAndAssertionConfirmationFieldsFactory<AuthorizeConfirmation>
        implements ConfirmationFactory<AuthorizeConfirmation> {
    public static final String ID_TAG_INFO_REQUIRED = "idTagInfo";
    @Value("${authorize.confirmation.idTagInfo:{\"expiryDate\":\"2023-12-31T23:23:59.278930403Z\",\"parentIdTag\":\"idTag-chargePointId\",\"status\":\"Accepted\"}}")
    private String defaultIdTagInfo;
    @Setter private String receivedIdTag = null;

    @PostConstruct
    private void init() {
        String className = AuthorizeConfirmation.class.getName();

        this.defaultValues = Map.of(
                ID_TAG_INFO_REQUIRED, defaultIdTagInfo
        );

        this.requiredFieldsSetup = Map.of(
                ID_TAG_INFO_REQUIRED, (conf, idTagStr) -> conf.setIdTagInfo(
                        getValidatedIdTagInfo(idTagStr, defaultIdTagInfo, ID_TAG_INFO_REQUIRED, receivedIdTag))
        );

        this.optionalFieldsSetup = Collections.emptyMap();
    }

    @Override
    public void validateFields(Map<String, String> params) {
        super.validateConfirmationFields(params);
    }

    @Override
    public AuthorizeConfirmation createValidatedConfirmation(Map<String, String> params,
                                                             AuthorizeConfirmation response) {
        return super.createValidatedConfirmation(params, response);
    }

}
