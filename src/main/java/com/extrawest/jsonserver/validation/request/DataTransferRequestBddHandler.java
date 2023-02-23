package com.extrawest.jsonserver.validation.request;

import java.util.Map;
import com.extrawest.jsonserver.validation.RequestFactory;
import com.extrawest.jsonserver.validation.ValidationAndAssertionFieldsFactory;
import eu.chargetime.ocpp.model.core.DataTransferRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataTransferRequestBddHandler
        extends ValidationAndAssertionFieldsFactory<DataTransferRequest>
        implements RequestFactory<DataTransferRequest> {
    public static final String VENDOR_ID_REQUIRED = "vendorId";
    public static final String MESSAGE_ID = "messageId";
    public static final String DATA = "data";

    @PostConstruct
    private void init() {
        this.requiredFieldsSetup = Map.of(
                VENDOR_ID_REQUIRED, (req, vendorId) -> {
                    if (nonEqual(wildCard, vendorId)) {
                        req.setVendorId(vendorId);
                    }
                }
        );

        this.optionalFieldsSetup = Map.of(
                MESSAGE_ID, (req, messageId) -> {
                    if (nonEqual(wildCard, messageId)) {
                        req.setMessageId(messageId);
                    }
                },
                DATA, (req, data) -> {
                    if (nonEqual(wildCard, data)) {
                        req.setData(data);
                    }
                }
        );

        this.assertionFactory = Map.of(
                VENDOR_ID_REQUIRED, (expectedParams, actual) -> compareStringsIncludeWildCard(
                        expectedParams, actual.getVendorId(), VENDOR_ID_REQUIRED),
                MESSAGE_ID, (expectedParams, actual) -> compareStringsIncludeWildCard(
                        expectedParams, actual.getMessageId(), MESSAGE_ID),
                DATA, (expectedParams, actual) -> compareStringsIncludeWildCard(
                        expectedParams, actual.getData(), DATA)
        );
    }

    @Override
    public boolean validateFields(Map<String, String> params, DataTransferRequest actualRequest) {
        return super.validateRequestFields(params, actualRequest);
    }

}
