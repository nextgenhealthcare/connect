/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.ncpdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

public class NCPDPReference {
    private static final String VERSION_D0 = "D0";

    private Map<String, String> NCPDPD0map = new HashMap<String, String>();
    private Map<String, String> segmentD0Map = new HashMap<String, String>();
    private ArrayList<String> repeatingFieldsD0 = new ArrayList<String>();

    private Map<String, String> NCPDP51map = new HashMap<String, String>();
    private Map<String, String> segment51Map = new HashMap<String, String>();
    private ArrayList<String> repeatingFields51 = new ArrayList<String>();

    private Map<String, String> transactionMap = new HashMap<String, String>();

    // singleton pattern
    private static NCPDPReference instance = null;

    private NCPDPReference() {
        populateNCPDPD0(NCPDPD0map);
        populateSegmentsD0(segmentD0Map);
        populateRepFieldsD0(repeatingFieldsD0);

        populateNCPDP51(NCPDP51map);
        populateSegments51(segment51Map);
        populateRepFields51(repeatingFields51);

        populateTransactionType(transactionMap);
    }

    public static NCPDPReference getInstance() {
        synchronized (NCPDPReference.class) {
            if (instance == null) {
                instance = new NCPDPReference();
            }

            return instance;
        }
    }

    public String getTransactionName(String key) {
        return MapUtils.getString(transactionMap, key, key);
    }

    public String getSegment(String key, String version) {
        if (StringUtils.equals(version, VERSION_D0)) {
            return MapUtils.getString(segmentD0Map, key, key);
        } else {
            return MapUtils.getString(segment51Map, key, key);
        }
    }

    public String getDescription(String key, String version) {
        if (StringUtils.equals(version, VERSION_D0)) {
            return MapUtils.getString(NCPDPD0map, key, StringUtils.EMPTY);
        } else {
            return MapUtils.getString(NCPDP51map, key, StringUtils.EMPTY);
        }
    }

    public String getCodeByName(String description, String version) {
        if (StringUtils.equals(version, VERSION_D0)) {
            for (String key : NCPDPD0map.keySet()) {
                if (NCPDPD0map.get(key).equals(description)) {
                    return key;
                }
            }
        } else {
            for (String key : NCPDP51map.keySet()) {
                if (NCPDP51map.get(key).equals(description)) {
                    return key;
                }
            }
        }

        return description;
    }

    public String getSegmentIdByName(String description, String version) {
        if (StringUtils.equals(version, VERSION_D0)) {
            for (String key : segmentD0Map.keySet()) {
                if (segmentD0Map.get(key).equals(description)) {
                    return key;
                }
            }
        } else {
            for (String key : segment51Map.keySet()) {
                if (segment51Map.get(key).equals(description)) {
                    return key;
                }
            }
        }

        return description;
    }

    public boolean isRepeatingField(String description, String version) {
        return ((StringUtils.equals(version, VERSION_D0) && repeatingFieldsD0.contains(description)) || repeatingFields51.contains(description));
    }

    private void populateNCPDP51(Map<String, String> messages) {
        messages.put("28", "UnitOfMeasure");
        messages.put("1C", "SmokerNon-SmokerCode");
        messages.put("1E", "PrescriberLocationCode");
        messages.put("2C", "PregnancyIndicator");
        messages.put("2E", "PrimaryCareProviderIdQualifier");
        messages.put("2F", "NetworkReimbursementId");
        messages.put("4C", "CoordinationOfBenefitsOtherPaymentsCount");
        messages.put("4E", "PrimaryCareProviderLastName");
        messages.put("4F", "RejectFieldOccurrenceIndicator");
        messages.put("5C", "OtherPayerCoverageType");
        messages.put("5E", "OtherPayerRejectCount");
        messages.put("5F", "ApprovedMessageCodeCount");
        messages.put("6C", "OtherPayerIdQualifier");
        messages.put("6E", "OtherPayerRejectCode");
        messages.put("6F", "ApprovedMessageCode");
        messages.put("7C", "OtherPayerId");
        messages.put("7E", "DurPpsCodeCounter");
        messages.put("7F", "HelpDeskPhoneNumberQualifier");
        messages.put("8C", "FacilityId");
        messages.put("8E", "DurPpsLevelOfEffort");
        messages.put("8F", "HelpDeskPhoneNumber");
        messages.put("9F", "PreferredProductCount");
        messages.put("A1", "BinNumber");
        messages.put("A2", "VersionReleaseNumber");
        messages.put("A3", "TransactionCode");
        messages.put("A4", "ProcessorControlNumber");
        messages.put("A9", "TransactionCount");
        messages.put("AK", "SoftwareVendorCertificationId");
        messages.put("AM", "SegmentIdentification");
        messages.put("AN", "TransactionResponseStatus");
        messages.put("AP", "PreferredProductIdQualifier");
        messages.put("AR", "PreferredProductId");
        messages.put("AS", "PreferredProductIncentive");
        messages.put("AT", "PreferredProductCopayIncentive");
        messages.put("AU", "PreferredProductDescription");
        messages.put("AV", "TaxExemptIndicator");
        messages.put("AW", "FlatSalesTaxAmountPaid");
        messages.put("AX", "PercentageSalesTaxAmountPaid");
        messages.put("AY", "PercentageSalesTaxRatePaid");
        messages.put("AZ", "PercentageSalesTaxBasisPaid");
        messages.put("B1", "ServiceProviderId");
        messages.put("B2", "ServiceProviderIdQualifier");
        messages.put("BE", "ProfessionalServiceFeeSubmitted");
        messages.put("C1", "GroupId");
        messages.put("C2", "CardholderId");
        messages.put("C3", "PersonCode");
        messages.put("C4", "DateOfBirth");
        messages.put("C5", "PatientGenderCode");
        messages.put("C6", "PatientRelationshipCode");
        messages.put("C7", "PatientLocation");
        messages.put("C8", "OtherCoverageCode");
        messages.put("C9", "EligibilityClarificationCode");
        messages.put("CA", "PatientFirstName");
        messages.put("CB", "PatientLastName");
        messages.put("CC", "CardholderFirstName");
        messages.put("CD", "CardholderLastName");
        messages.put("CE", "HomePlan");
        messages.put("CF", "EmployerName");
        messages.put("CG", "EmployerStreetAddress");
        messages.put("CH", "EmployerCityAddress");
        messages.put("CI", "EmployerStateProvinceAddress");
        messages.put("CJ", "EmployerZipPostalZone");
        messages.put("CK", "EmployerPhoneNumber");
        messages.put("CL", "EmployerContactName");
        messages.put("CM", "PatientStreetAddress");
        messages.put("CN", "PatientCityAddress");
        messages.put("CO", "PatientStateProvinceAddress");
        messages.put("CP", "PatientZipPostalZone");
        messages.put("CQ", "PatientPhoneNumber");
        messages.put("CR", "CarrierId");
        messages.put("CW", "AlternateId");
        messages.put("CX", "PatientIdQualifier");
        messages.put("CY", "PatientId");
        messages.put("CZ", "EmployerId");
        messages.put("D1", "DateOfService");
        messages.put("D2", "PrescriptionServiceReferenceNumber");
        messages.put("D3", "FillNumber");
        messages.put("D5", "DaysSupply");
        messages.put("D6", "CompoundCode");
        messages.put("D7", "ProductServiceId");
        messages.put("D8", "DispenseAsWrittenProductSelectionCode");
        messages.put("D9", "IngredientCostSubmitted");
        messages.put("DB", "PrescriberId");
        messages.put("DC", "DispensingFeeSubmitted");
        messages.put("DE", "DatePrescriptionWritten");
        messages.put("DF", "NumberOfRefillsAuthorized");
        messages.put("DI", "LevelOfService");
        messages.put("DJ", "PrescriptionOriginCode");
        messages.put("DK", "SubmissionClarificationCode");
        messages.put("DL", "PrimaryCareProviderId");
        messages.put("DN", "BasisOfCostDetermination");
        messages.put("DO", "DiagnosisCode");
        messages.put("DQ", "UsualAndCustomaryCharge");
        messages.put("DR", "PrescriberLastName");
        messages.put("DT", "UnitDoseIndicator");
        messages.put("DU", "GrossAmountDue");
        messages.put("DV", "OtherPayerAmountPaid");
        messages.put("DX", "PatientPaidAmountSubmitted");
        messages.put("DY", "DateOfInjury");
        messages.put("DZ", "ClaimReferenceId");
        messages.put("E1", "ProductServiceIdQualifier");
        messages.put("E3", "IncentiveAmountSubmitted");
        messages.put("E4", "ReasonForServiceCode");
        messages.put("E5", "ProfessionalServiceCode");
        messages.put("E6", "ResultOfServiceCode");
        messages.put("E7", "QuantityDispensed");
        messages.put("E8", "OtherPayerDate");
        messages.put("E9", "ProviderId");
        messages.put("EA", "OriginallyPrescribedProductServiceCode");
        messages.put("EB", "OriginallyPrescribedQuantity");
        messages.put("EC", "CompoundIngredientComponentCount");
        messages.put("ED", "CompoundIngredientQuantity");
        messages.put("EE", "CompoundIngredientDrugCost");
        messages.put("EF", "CompoundDosageFormDescriptionCode");
        messages.put("EG", "CompoundDispensingUnitFormIndicator");
        messages.put("EH", "CompoundRouteOfAdministration");
        messages.put("EJ", "OrigPrescribedProductServiceIdQualifier");
        messages.put("EK", "ScheduledPrescriptionIdNumber");
        messages.put("EM", "PrescriptionServiceReferenceNumberQualifier");
        messages.put("EN", "AssociatedPrescriptionServiceReferenceNumber");
        messages.put("EP", "AssociatedPrescriptionServiceDate");
        messages.put("ER", "ProcedureModifierCode");
        messages.put("ET", "QuantityPrescribed");
        messages.put("EU", "PriorAuthorizationTypeCode");
        messages.put("EV", "PriorAuthorizationNumberSubmitted");
        messages.put("EW", "IntermediaryAuthorizationTypeId");
        messages.put("EX", "IntermediaryAuthorizationId");
        messages.put("EY", "ProviderIdQualifier");
        messages.put("EZ", "PrescriberIdQualifier");
        messages.put("F1", "HeaderResponseStatus");
        messages.put("F3", "AuthorizationNumber");
        messages.put("F4", "Message");
        messages.put("F5", "PatientPayAmount");
        messages.put("F6", "IngredientCostPaid");
        messages.put("F7", "DispensingFeePaid");
        messages.put("F9", "TotalAmountPaid");
        messages.put("FA", "RejectCount");
        messages.put("FB", "RejectCode");
        messages.put("FC", "AccumulatedDeductibleAmount");
        messages.put("FD", "RemainingDeductibleAmount");
        messages.put("FE", "RemainingBenefitAmount");
        messages.put("FH", "AmountAppliedToPeriodicDeductible");
        messages.put("FI", "AmountOfCopayCo-Insurance");
        messages.put("FJ", "AmountAttributedToProductSelection");
        messages.put("FK", "AmountExceedingPeriodicBenefitMaximum");
        messages.put("FL", "IncentiveAmountPaid");
        messages.put("FM", "BasisOfReimbursementDetermination");
        messages.put("FN", "AmountAttributedToSalesTax");
        messages.put("FO", "PlanId");
        messages.put("FQ", "AdditionalMessageInformation");
        messages.put("FS", "ClinicalSignificanceCode");
        messages.put("FT", "OtherPharmacyIndicator");
        messages.put("FU", "PreviousDateOfFill");
        messages.put("FV", "QuantityOfPreviousFill");
        messages.put("FW", "DatabaseIndicator");
        messages.put("FX", "OtherPrescriberIndicator");
        messages.put("FY", "DurFreeTextMessage");
        messages.put("GE", "PercentageSalesTaxAmountSubmitted");
        messages.put("H1", "MeasurementTime");
        messages.put("H2", "MeasurementDimension");
        messages.put("H3", "MeasurementUnit");
        messages.put("H4", "MeasurementValue");
        messages.put("H5", "PrimaryCareProviderLocationCode");
        messages.put("H6", "DurCo-AgentId");
        messages.put("H7", "OtherAmountClaimedSubmittedCount");
        messages.put("H8", "OtherAmountClaimedSubmittedQualifier");
        messages.put("H9", "OtherAmountClaimedSubmitted");
        messages.put("HA", "FlatSalesTaxAmountSubmitted");
        messages.put("HB", "OtherPayerAmountPaidCount");
        messages.put("HC", "OtherPayerAmountPaidQualifier");
        messages.put("HD", "DispensingStatus");
        messages.put("HE", "PercentageSalesTaxRateSubmitted");
        messages.put("HF", "QuantityIntendedToBeDispensed");
        messages.put("HG", "DaysSupplyIntendedToBeDispensed");
        messages.put("HH", "BasisOfCalculationDispensingFee");
        messages.put("HJ", "BasisOfCalculationCopay");
        messages.put("HK", "BasisOfCalculationFlatSalesTax");
        messages.put("HM", "BasisOfCalculationPercentageSalesTax");
        messages.put("J1", "ProfessionalServiceFeePaid");
        messages.put("J2", "OtherAmountPaidCount");
        messages.put("J3", "OtherAmountPaidQualifier");
        messages.put("J4", "OtherAmountPaid");
        messages.put("J5", "OtherPayerAmountRecognized");
        messages.put("J6", "DurPpsResponseCodeCounter");
        messages.put("J7", "PayerIdQualifier");
        messages.put("J8", "PayerId");
        messages.put("J9", "DurCo-AgentIdQualifier");
        messages.put("JE", "PercentageSalesTaxBasisSubmitted");
        messages.put("KE", "CouponType");
        messages.put("ME", "CouponNumber");
        messages.put("NE", "CouponValueAmount");
        messages.put("PA", "RequestType");
        messages.put("PB", "RequestPeriodDate-Begin");
        messages.put("PC", "RequestPeriodDate-End");
        messages.put("PD", "BasisOfRequest");
        messages.put("PE", "AuthorizedRepresentativeFirstName");
        messages.put("PF", "AuthorizedRepresentativeLastName");
        messages.put("PG", "AuthorizedRepresentativeStreetAddress");
        messages.put("PH", "AuthorizedRepresentativeCityAddress");
        messages.put("PJ", "AuthorizedRepresentativeStateProvinceAddress");
        messages.put("PK", "AuthorizedRepresentativeZipPostalZone");
        messages.put("PM", "PrescriberPhoneNumber");
        messages.put("PP", "PriorAuthorizationSupportingDocumentation");
        messages.put("PR", "PriorAuthorizationProcessedDate");
        messages.put("PS", "PriorAuthorizationEffectiveDate");
        messages.put("PT", "PriorAuthorizationExpirationDate");
        messages.put("PW", "PriorAuthorizationNumberOfRefillsAuthorized");
        messages.put("PX", "PriorAuthorizationQuantityAccumulated");
        messages.put("PY", "PriorAuthorizationNumber-Assigned");
        messages.put("RA", "PriorAuthorizationQuantity");
        messages.put("RB", "PriorAuthorizationDollarsAuthorized");
        messages.put("RE", "CompoundProductIdQualifier");
        messages.put("SE", "ProcedureModifierCodeCount");
        messages.put("TE", "CompoundProductId");
        messages.put("UE", "CompoundIngredientBasisOfCostDetermination");
        messages.put("VE", "DiagnosisCodeCount");
        messages.put("WE", "DiagnosisCodeQualifier");
        messages.put("XE", "ClinicalInformationCounter");
        messages.put("ZE", "MeasurementDate");
    }

    private void populateNCPDPD0(Map<String, String> messages) {
        messages.put("28", "UnitOfMeasure");
        messages.put("1C", "SmokerNon-SmokerCode");
        messages.put("1E", "PrescriberLocationCode");
        messages.put("2A", "MedigapId"); // Added for D0
        messages.put("2B", "MedicaidIndicator"); // Added for D0
        messages.put("2C", "PregnancyIndicator");
        messages.put("2D", "ProviderAcceptAssignmentIndicator"); // Added for D0
        messages.put("2E", "PrimaryCareProviderIdQualifier");
        messages.put("2F", "NetworkReimbursementId");
        messages.put("2G", "CompoundIngredientModifierCodeCount");
        messages.put("2H", "CompoundIngredientModifierCode");
        messages.put("2J", "PrescriberFirstName"); // added D0
        messages.put("2K", "PrescriberStreetAddress"); // added D0
        messages.put("2M", "PrescriberCityAddress"); // added D0
        messages.put("2N", "PrescriberStateAddress"); // added D0
        messages.put("2P", "PrescriberZipAddress"); // added D0
        messages.put("2Q", "AdditionalDocumentationTypeId"); // added D0
        messages.put("2R", "LengthOfNeed"); // added D0
        messages.put("2S", "LengthOfNeedQualifier"); // added D0
        messages.put("2T", "PrescriberSupplierDateSigned"); // added D0
        messages.put("2U", "RequestStatus"); // added D0
        messages.put("2V", "RequestPeriodBeginDate"); // added D0
        messages.put("2W", "RequestPeriodRecertDate");// added D0
        messages.put("2X", "SupportingDocumentation");// added D0
        messages.put("2Y", "PlanSalesTaxAmount");// added D0
        messages.put("2Z", "QuestionNumberLetterCount"); // added D0
        messages.put("3Q", "FacilityName"); // added D0
        messages.put("3U", "FacilityStreetAddress"); // added D0
        messages.put("3V", "FacilityStateAddress"); // added D0
        messages.put("4B", "QuestionNumberLetter"); // added D0
        messages.put("4C", "CoordinationOfBenefitsOtherPaymentsCount");
        messages.put("4D", "QuestionPercentResponse"); // added D0
        messages.put("4E", "PrimaryCareProviderLastName");
        messages.put("4F", "RejectFieldOccurrenceIndicator");
        messages.put("4G", "QuestionDateResponse"); // added D0
        messages.put("4H", "QuestionDollarAmountResponse"); // added D0
        messages.put("4J", "QuestionNumericResponse"); // added D0
        messages.put("4K", "QuestionAlphaNumericResponse"); // added D0
        messages.put("4U", "AmountOfCoinsurance"); // added D0
        messages.put("4V", "BasisOfCalculationCoinsurance"); // added D0
        messages.put("4X", "PatientResidence"); // added D0
        messages.put("5C", "OtherPayerCoverageType");
        messages.put("5E", "OtherPayerRejectCount");
        messages.put("5F", "ApprovedMessageCodeCount");
        messages.put("5J", "FacilityCityAddress"); // added D0
        messages.put("6C", "OtherPayerIdQualifier");
        messages.put("6D", "FacilityZipAddress"); // added D0
        messages.put("6E", "OtherPayerRejectCode");
        messages.put("6F", "ApprovedMessageCode");
        messages.put("7C", "OtherPayerId");
        messages.put("7E", "DurPpsCodeCounter");
        messages.put("7F", "HelpDeskPhoneNumberQualifier");
        messages.put("8C", "FacilityId");
        messages.put("8E", "DurPpsLevelOfEffort");
        messages.put("8F", "HelpDeskPhoneNumber");
        messages.put("9F", "PreferredProductCount");
        messages.put("A1", "BinNumber");
        messages.put("A2", "VersionReleaseNumber");
        messages.put("A3", "TransactionCode");
        messages.put("A4", "ProcessorControlNumber");
        messages.put("A7", "InternalControlNumber"); // added D0
        messages.put("A9", "TransactionCount");
        messages.put("AK", "SoftwareVendorCertificationId");
        messages.put("AM", "SegmentIdentification");
        messages.put("AN", "TransactionResponseStatus");
        messages.put("AP", "PreferredProductIdQualifier");
        messages.put("AR", "PreferredProductId");
        messages.put("AS", "PreferredProductIncentive");
        messages.put("AT", "PreferredProductCopayIncentive");
        messages.put("AU", "PreferredProductDescription");
        messages.put("AV", "TaxExemptIndicator");
        messages.put("AW", "FlatSalesTaxAmountPaid");
        messages.put("AX", "PercentageSalesTaxAmountPaid");
        messages.put("AY", "PercentageSalesTaxRatePaid");
        messages.put("AZ", "PercentageSalesTaxBasisPaid");
        messages.put("B1", "ServiceProviderId");
        messages.put("B2", "ServiceProviderIdQualifier");
        messages.put("BE", "ProfessionalServiceFeeSubmitted");
        messages.put("BM", "NarrativeMessage"); // added D0
        messages.put("C1", "GroupId");
        messages.put("C2", "CardholderId");
        messages.put("C3", "PersonCode");
        messages.put("C4", "DateOfBirth");
        messages.put("C5", "PatientGenderCode");
        messages.put("C6", "PatientRelationshipCode");
        messages.put("C7", "PlaceOfService");
        messages.put("C8", "OtherCoverageCode");
        messages.put("C9", "EligibilityClarificationCode");
        messages.put("CA", "PatientFirstName");
        messages.put("CB", "PatientLastName");
        messages.put("CC", "CardholderFirstName");
        messages.put("CD", "CardholderLastName");
        messages.put("CE", "HomePlan");
        messages.put("CF", "EmployerName");
        messages.put("CG", "EmployerStreetAddress");
        messages.put("CH", "EmployerCityAddress");
        messages.put("CI", "EmployerStateProvinceAddress");
        messages.put("CJ", "EmployerZipPostalZone");
        messages.put("CK", "EmployerPhoneNumber");
        messages.put("CL", "EmployerContactName");
        messages.put("CM", "PatientStreetAddress");
        messages.put("CN", "PatientCityAddress");
        messages.put("CO", "PatientStateProvinceAddress");
        messages.put("CP", "PatientZipPostalZone");
        messages.put("CQ", "PatientPhoneNumber");
        messages.put("CR", "CarrierId");
        messages.put("CW", "AlternateId");
        messages.put("CX", "PatientIdQualifier");
        messages.put("CY", "PatientId");
        messages.put("CZ", "EmployerId");
        messages.put("D1", "DateOfService");
        messages.put("D2", "PrescriptionServiceReferenceNumber");
        messages.put("D3", "FillNumber");
        messages.put("D5", "DaysSupply");
        messages.put("D6", "CompoundCode");
        messages.put("D7", "ProductServiceId");
        messages.put("D8", "DispenseAsWrittenProductSelectionCode");
        messages.put("D9", "IngredientCostSubmitted");
        messages.put("DB", "PrescriberId");
        messages.put("DC", "DispensingFeeSubmitted");
        messages.put("DE", "DatePrescriptionWritten");
        messages.put("DF", "NumberOfRefillsAuthorized");
        messages.put("DI", "LevelOfService");
        messages.put("DJ", "PrescriptionOriginCode");
        messages.put("DK", "SubmissionClarificationCode");
        messages.put("DL", "PrimaryCareProviderId");
        messages.put("DN", "BasisOfCostDetermination");
        messages.put("DO", "DiagnosisCode");
        messages.put("DQ", "UsualAndCustomaryCharge");
        messages.put("DR", "PrescriberLastName");
        messages.put("DT", "SpecialPackagingIndicator"); // added D0
        messages.put("DU", "GrossAmountDue");
        messages.put("DV", "OtherPayerAmountPaid");
        messages.put("DX", "PatientPaidAmountSubmitted");
        messages.put("DY", "DateOfInjury");
        messages.put("DZ", "ClaimReferenceId");
        messages.put("E1", "ProductServiceIdQualifier");
        messages.put("E2", "RouteOfAdministration"); // added D0
        messages.put("E3", "IncentiveAmountSubmitted");
        messages.put("E4", "ReasonForServiceCode");
        messages.put("E5", "ProfessionalServiceCode");
        messages.put("E6", "ResultOfServiceCode");
        messages.put("E7", "QuantityDispensed");
        messages.put("E8", "OtherPayerDate");
        messages.put("E9", "ProviderId");
        messages.put("EA", "OriginallyPrescribedProductServiceCode");
        messages.put("EB", "OriginallyPrescribedQuantity");
        messages.put("EC", "CompoundIngredientComponentCount");
        messages.put("ED", "CompoundIngredientQuantity");
        messages.put("EE", "CompoundIngredientDrugCost");
        messages.put("EF", "CompoundDosageFormDescriptionCode");
        messages.put("EG", "CompoundDispensingUnitFormIndicator");
        messages.put("EH", "CompoundRouteOfAdministration");
        messages.put("EJ", "OrigPrescribedProductServiceIdQualifier");
        messages.put("EK", "ScheduledPrescriptionIdNumber");
        messages.put("EM", "PrescriptionServiceReferenceNumberQualifier");
        messages.put("EN", "AssociatedPrescriptionServiceReferenceNumber");
        messages.put("EP", "AssociatedPrescriptionServiceDate");
        messages.put("EQ", "PatientSalesTaxAmount");
        messages.put("ER", "ProcedureModifierCode");
        messages.put("ET", "QuantityPrescribed");
        messages.put("EU", "PriorAuthorizationTypeCode");
        messages.put("EV", "PriorAuthorizationNumberSubmitted");
        messages.put("EW", "IntermediaryAuthorizationTypeId");
        messages.put("EX", "IntermediaryAuthorizationId");
        messages.put("EY", "ProviderIdQualifier");
        messages.put("EZ", "PrescriberIdQualifier");
        messages.put("F1", "HeaderResponseStatus");
        messages.put("F3", "AuthorizationNumber");
        messages.put("F4", "Message");
        messages.put("F5", "PatientPayAmount");
        messages.put("F6", "IngredientCostPaid");
        messages.put("F7", "DispensingFeePaid");
        messages.put("F9", "TotalAmountPaid");
        messages.put("FA", "RejectCount");
        messages.put("FB", "RejectCode");
        messages.put("FC", "AccumulatedDeductibleAmount");
        messages.put("FD", "RemainingDeductibleAmount");
        messages.put("FE", "RemainingBenefitAmount");
        messages.put("FF", "FormularyId"); // added D0
        messages.put("FH", "AmountAppliedToPeriodicDeductible");
        messages.put("FI", "AmountOfCopayCo-Insurance");
        messages.put("FJ", "AmountAttributedToProductSelection");
        messages.put("FK", "AmountExceedingPeriodicBenefitMaximum");
        messages.put("FL", "IncentiveAmountPaid");
        messages.put("FM", "BasisOfReimbursementDetermination");
        messages.put("FN", "AmountAttributedToSalesTax");
        messages.put("FO", "PlanId");
        messages.put("FQ", "AdditionalMessageInformation");
        messages.put("FS", "ClinicalSignificanceCode");
        messages.put("FT", "OtherPharmacyIndicator");
        messages.put("FU", "PreviousDateOfFill");
        messages.put("FV", "QuantityOfPreviousFill");
        messages.put("FW", "DatabaseIndicator");
        messages.put("FX", "OtherPrescriberIndicator");
        messages.put("FY", "DurFreeTextMessage");
        messages.put("G1", "CompoundType"); // added D0
        messages.put("G2", "CMSPartDDefinedQualifiedFacility"); // added for D0
        messages.put("G3", "EstimatedGenericSavings"); // added D0
        messages.put("GE", "PercentageSalesTaxAmountSubmitted");
        messages.put("H1", "MeasurementTime");
        messages.put("H2", "MeasurementDimension");
        messages.put("H3", "MeasurementUnit");
        messages.put("H4", "MeasurementValue");
        messages.put("H5", "PrimaryCareProviderLocationCode");
        messages.put("H6", "DurCo-AgentId");
        messages.put("H7", "OtherAmountClaimedSubmittedCount");
        messages.put("H8", "OtherAmountClaimedSubmittedQualifier");
        messages.put("H9", "OtherAmountClaimedSubmitted");
        messages.put("HA", "FlatSalesTaxAmountSubmitted");
        messages.put("HB", "OtherPayerAmountPaidCount");
        messages.put("HC", "OtherPayerAmountPaidQualifier");
        messages.put("HD", "DispensingStatus");
        messages.put("HE", "PercentageSalesTaxRateSubmitted");
        messages.put("HF", "QuantityIntendedToBeDispensed");
        messages.put("HG", "DaysSupplyIntendedToBeDispensed");
        messages.put("HH", "BasisOfCalculationDispensingFee");
        messages.put("HJ", "BasisOfCalculationCopay");
        messages.put("HK", "BasisOfCalculationFlatSalesTax");
        messages.put("HM", "BasisOfCalculationPercentageSalesTax");
        messages.put("HN", "PatientEmailAddress"); // added D0
        messages.put("J1", "ProfessionalServiceFeePaid");
        messages.put("J2", "OtherAmountPaidCount");
        messages.put("J3", "OtherAmountPaidQualifier");
        messages.put("J4", "OtherAmountPaid");
        messages.put("J5", "OtherPayerAmountRecognized");
        messages.put("J6", "DurPpsResponseCodeCounter");
        messages.put("J7", "PayerIdQualifier");
        messages.put("J8", "PayerId");
        messages.put("J9", "DurCo-AgentIdQualifier");
        messages.put("JE", "PercentageSalesTaxBasisSubmitted");
        messages.put("K5", "TransactionReferenceNumber");
        messages.put("KE", "CouponType");
        messages.put("MA", "URL");
        messages.put("ME", "CouponNumber");
        messages.put("MG", "OtherPayerBinNumber"); // added D0
        messages.put("MH", "OtherPayerProcessorControlNumber"); // added D0
        messages.put("MJ", "OtherPayerGroupId"); // added D0
        messages.put("MQ", "AmountAttributedToProductSelectionQualifier"); // added D0
        messages.put("MT", "PatientAssignmentIndicator"); // added D0
        messages.put("MU", "BenefitStageCount"); // added D0
        messages.put("MV", "BenefitStageQualifier"); // added D0
        messages.put("MW", "BenefitStageAmount"); // added D0
        messages.put("N3", "MedicaidPaidAmount"); // added D0
        messages.put("N4", "MedicaidSubrogationInternalControlNumber"); // added D0
        messages.put("N5", "MedicaidIdNumber"); // added D0
        messages.put("N6", "MedicaidAgencyNumber"); // added D0
        messages.put("NE", "CouponValueAmount");
        messages.put("NP", "OtherPayerPatientRespAmountPaidQualifier"); // added D0
        messages.put("NQ", "OtherPayerPatientRespAmount"); // added D0
        messages.put("NR", "OtherPayerPatientRespAmountPaidCount"); // added D0
        messages.put("NT", "OtherPayerIdCount"); // added D0
        messages.put("NU", "OtherPayerCardholderId"); // added D0
        messages.put("NV", "DelayReasonCode"); // added D0
        messages.put("NX", "SubmissionClarificationCodeCount"); // added D0
        messages.put("PA", "RequestType");
        messages.put("PB", "RequestPeriodDate-Begin");
        messages.put("PC", "RequestPeriodDate-End");
        messages.put("PD", "BasisOfRequest");
        messages.put("PE", "AuthorizedRepresentativeFirstName");
        messages.put("PF", "AuthorizedRepresentativeLastName");
        messages.put("PG", "AuthorizedRepresentativeStreetAddress");
        messages.put("PH", "AuthorizedRepresentativeCityAddress");
        messages.put("PJ", "AuthorizedRepresentativeStateProvinceAddress");
        messages.put("PK", "AuthorizedRepresentativeZipPostalZone");
        messages.put("PM", "PrescriberPhoneNumber");
        messages.put("PP", "PriorAuthorizationSupportingDocumentation");
        messages.put("PR", "PriorAuthorizationProcessedDate");
        messages.put("PS", "PriorAuthorizationEffectiveDate");
        messages.put("PT", "PriorAuthorizationExpirationDate");
        messages.put("PW", "PriorAuthorizationNumberOfRefillsAuthorized");
        messages.put("PX", "PriorAuthorizationQuantityAccumulated");
        messages.put("PY", "PriorAuthorizationNumber-Assigned");
        messages.put("RA", "PriorAuthorizationQuantity");
        messages.put("RB", "PriorAuthorizationDollarsAuthorized");
        messages.put("RE", "CompoundProductIdQualifier");
        messages.put("SE", "ProcedureModifierCodeCount");
        messages.put("TE", "CompoundProductId");
        messages.put("TR", "BillingEntityTypeIndicator"); // added D0
        messages.put("TS", "PayToQualifier"); // added D0
        messages.put("TT", "PayToId"); // added D0
        messages.put("TU", "PayToName"); // added D0
        messages.put("TV", "PayToStreetAddress"); // added D0
        messages.put("TW", "PayToCityAddress"); // added D0
        messages.put("TX", "PayToStateAddress"); // added D0
        messages.put("TY", "PayToZipAddress"); // added D0
        messages.put("TZ", "GenericEquivalentProductIdQualifier"); // added D0
        messages.put("U1", "ContractNumber"); // added D0
        messages.put("U6", "BenefitId"); // added D0
        messages.put("U7", "PharmacyServiceType"); // added D0
        messages.put("U8", "IngredientCostContractedAmount"); // added D0
        messages.put("U9", "DispensingFeeContractedAmount"); // added D0
        messages.put("UA", "GenericEquivalentProductId"); // added D0
        messages.put("UB", "OtherPayerHelpDeskPhone"); // added D0
        messages.put("UC", "SpendingAccountAmountRemaining"); // added D0
        messages.put("UD", "HealthPlanFundedAssistanceAmount"); // added D0
        messages.put("UE", "CompoundIngredientBasisOfCostDetermination");
        messages.put("UF", "AdditionalMessageInformationCount"); // added D0
        messages.put("UG", "AdditionalMessageInformationContinuity"); // added D0
        messages.put("UH", "AdditionalMessageInformationQualifier"); // added D0
        messages.put("UJ", "AmountAttributedToProviderNetworkSelection");
        messages.put("UK", "AmountAttributedToProductSelectionBrandDrug");
        messages.put("UM", "AmountAttributedToProductSelectionNonPreferredSelection");
        messages.put("UN", "AmountAttributedToProductSelectionBrandNonPreferredSelection");
        messages.put("UP", "AmountAttributedToCoverageGap"); // added D0
        messages.put("UQ", "CmsLowIncomeCostSharingLevel"); // added D0
        messages.put("UR", "MedicarePartDCoverageCode");
        messages.put("US", "NextMedicarePartDEffecticeDate"); // added D0
        messages.put("UT", "NextMedicarePartDTerminationDate"); // added D0
        messages.put("UV", "OtherPayerPersonCode"); // added D0
        messages.put("UW", "OtherPayerPatientRelationshipCode"); // added D0
        messages.put("UX", "OtherPayerBenefitEffectiveDate"); // added D0
        messages.put("UY", "OtherPayerBenefitTerminationDate"); // added D0
        messages.put("VE", "DiagnosisCodeCount");
        messages.put("WE", "DiagnosisCodeQualifier");
        messages.put("XE", "ClinicalInformationCounter");
        messages.put("ZE", "MeasurementDate");
    }

    private void populateSegments51(Map<String, String> segments) {
        segments.put("AM01", "Patient");
        segments.put("AM02", "PharmacyProvider");
        segments.put("AM03", "Prescriber");
        segments.put("AM04", "Insurance");
        segments.put("AM05", "CoordinationOfBenefitsOtherPayments");
        segments.put("AM06", "WorkersCompensation");
        segments.put("AM07", "Claim");
        segments.put("AM08", "DURPPS");
        segments.put("AM09", "Coupon");
        segments.put("AM10", "Compound");
        segments.put("AM11", "Pricing");
        segments.put("AM12", "PriorAuthorization");
        segments.put("AM13", "Clinical");
        segments.put("AM20", "ResponseMessage");
        segments.put("AM21", "ResponseStatus");
        segments.put("AM22", "ResponseClaim");
        segments.put("AM23", "ResponsePricing");
        segments.put("AM24", "ResponseDURPPS");
        segments.put("AM25", "ResponseInsurance");
        segments.put("AM26", "ResponsePriorAuthorization");
    }

    private void populateSegmentsD0(Map<String, String> segments) {
        segments.put("AM01", "Patient");
        segments.put("AM02", "PharmacyProvider");
        segments.put("AM03", "Prescriber");
        segments.put("AM04", "Insurance");
        segments.put("AM05", "CoordinationOfBenefitsOtherPayments");
        segments.put("AM06", "WorkersCompensation");
        segments.put("AM07", "Claim");
        segments.put("AM08", "DURPPS");
        segments.put("AM09", "Coupon");
        segments.put("AM10", "Compound");
        segments.put("AM11", "Pricing");
        segments.put("AM12", "PriorAuthorization"); // removed for D0
        segments.put("AM13", "Clinical");
        segments.put("AM14", "AdditionalDocumentation"); // added for D0
        segments.put("AM15", "Facility"); // Added for D0 support
        segments.put("AM16", "Narrative");
        segments.put("AM20", "ResponseMessage");
        segments.put("AM21", "ResponseStatus");
        segments.put("AM22", "ResponseClaim");
        segments.put("AM23", "ResponsePricing");
        segments.put("AM24", "ResponseDURPPS");
        segments.put("AM25", "ResponseInsurance");
        segments.put("AM26", "ResponsePriorAuthorization");

    }

    private void populateTransactionType(Map<String, String> types) {
        types.put("E1", "EligibilityVerification");
        types.put("B1", "Billing");
        types.put("B2", "Reversal");
        types.put("B3", "Rebill");
        types.put("P1", "PARequestBilling");
        types.put("P2", "PAReversal");
        types.put("P3", "PAInquiry");
        types.put("P4", "PARequestOnly");
        types.put("N1", "InformationReporting");
        types.put("N2", "InformationReportingReversal");
        types.put("N3", "InformationReportingRebill");
        types.put("C1", "ControlledSubstanceReporting");
        types.put("C2", "ControlledSubstanceReportingReversal");
        types.put("C3", "ControlledSubstanceReportingRebill");
    }

    private void populateRepFields51(List<String> fields) {
        fields.add("ProcedureModifierCode");
        fields.add("OtherPayerCoverageType");
        fields.add("OtherPayerIdQualifier");
        fields.add("OtherPayerId");
        fields.add("OtherPayerDate");
        fields.add("OtherPayerAmountPaidQualifier");
        fields.add("OtherPayerAmountPaid");
        fields.add("OtherPayerRejectCode");
        fields.add("OtherAmountClaimedSubmittedQualifier");
        fields.add("OtherAmountClaimedSubmitted");
        fields.add("CompoundProductIdQualifier");
        fields.add("CompoundProductId");
        fields.add("CompoundIngredientQuantity");
        fields.add("CompoundIngredientDrugCost");
        fields.add("CompoundIngredientBasisOfCostDetermination");
        fields.add("DiagnosisCodeQualifier");
        fields.add("DiagnosisCode");
        fields.add("RejectCode");
        fields.add("RejectFieldOccurrenceIndicator");
        fields.add("ApprovedMessageCode");
        fields.add("PreferredProductIdQualifier");
        fields.add("PreferredProductId");
        fields.add("PreferredProductIncentive");
        fields.add("PreferredProductCopayIncentive");
        fields.add("PreferredProductDescription");
        fields.add("OtherAmountPaidQualifier");
        fields.add("OtherAmountPaid");
    }

    private void populateRepFieldsD0(List<String> fields) {
        fields.add("ProcedureModifierCode");
        fields.add("OtherPayerCoverageType");
        fields.add("OtherPayerIdQualifier");
        fields.add("OtherPayerId");
        fields.add("OtherPayerDate");
        fields.add("OtherPayerAmountPaidQualifier");
        fields.add("OtherPayerAmountPaid");
        fields.add("OtherPayerRejectCode");
        fields.add("OtherAmountClaimedSubmittedQualifier");
        fields.add("OtherAmountClaimedSubmitted");
        fields.add("CompoundProductIdQualifier");
        fields.add("CompoundProductId");
        fields.add("CompoundIngredientQuantity");
        fields.add("CompoundIngredientDrugCost");
        fields.add("CompoundIngredientBasisOfCostDetermination");
        fields.add("DiagnosisCodeQualifier");
        fields.add("DiagnosisCode");
        fields.add("RejectCode");
        fields.add("RejectFieldOccurrenceIndicator");
        fields.add("ApprovedMessageCode");
        fields.add("PreferredProductIdQualifier");
        fields.add("PreferredProductId");
        fields.add("PreferredProductIncentive");
        fields.add("PreferredProductCopayIncentive");
        fields.add("PreferredProductDescription");
        fields.add("OtherAmountPaidQualifier");
        fields.add("OtherAmountPaid");
        fields.add("SubmissionClarificationCode");
        fields.add("BenefitStageAmount");
        fields.add("BenefitStageQualifier");
        fields.add("OtherPayerPatientRespAmount");
        fields.add("OtherPayerPatientRespAmountPaidQualifier");
        fields.add("InternalControlNumber");
        fields.add("OtherPayerAmountPaidCount");
        fields.add("CompoundIngredientModifierCode");
        fields.add("QuestionNumberLetter");
        fields.add("QuestionPercentResponse");
        fields.add("QuestionDateResponse");
        fields.add("QuestionDollarAmountResponse");
        fields.add("QuestionNumericResponse");
        fields.add("QuestionAlphaNumericResponse");
        fields.add("AdditionalMessageInformation");
        fields.add("AdditionalMessageInformationContinuity");
        fields.add("AdditionalMessageInformationQualifier");
        fields.add("OtherPayerProcessorControlNumber");
        fields.add("OtherPayerCardholderId");
        fields.add("OtherPayerGroupId");
        fields.add("OtherPayerPersonCode");
        fields.add("OtherPayerHelpDeskPhone");
        fields.add("OtherPayerPatientRelationshipCode");
        fields.add("OtherPayerBenefitEffectiveDate");
        fields.add("OtherPayerBenefitTerminationDate");
    }
}
