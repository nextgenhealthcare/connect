package com.webreach.mirth.model.ncpdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 7, 2007
 * Time: 9:17:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPReference {

    private Map<String, String> NCPDPmap = new HashMap<String, String>();
    private Map<String, String> segmentMap = new HashMap<String, String>();
    private Map<String, String> transactionMap = new HashMap<String, String>();
    private ArrayList<String> repFields = new ArrayList<String>();
    private static NCPDPReference instance = null;
    private NCPDPReference()
    {
        populateNCPDP(NCPDPmap);
        populateSegments(segmentMap);
        populateTransactionType(transactionMap);
        populateRepFields(repFields);
    }

    public static NCPDPReference getInstance()
    {
        synchronized (NCPDPReference.class)
        {
            if (instance == null)
                instance = new NCPDPReference();
            return instance;
        }
    }
    public String getTransactionName(String key) {
        if(transactionMap.containsKey(key)){
            return transactionMap.get(key);
        }
        return key;
    }

    public String getSegment(String key) {
        if(segmentMap.containsKey(key)){
            return segmentMap.get(key);
        }
        return key;
    }
    public String getDescription(String key)
    {
        if (NCPDPmap.containsKey(key))
            return NCPDPmap.get(key);
        else
            return new String();
    }
    public String getCodeFromName(String desc) {
        Set keys = NCPDPmap.keySet();
        Iterator iter = keys.iterator();
        while(iter.hasNext()){
            Object key = iter.next();
            String value = NCPDPmap.get(key);
            if(value.equals(desc)){
                return (String) key;
            }
        }
        return desc;
    }
    public String getSegmentIdFromName(String desc) {
        Set keys = segmentMap.keySet();
        Iterator iter = keys.iterator();
        while(iter.hasNext()){
            Object key = iter.next();
            String value = segmentMap.get(key);
            if(value.equals(desc)){
                return (String) key;
            }
        }
        return desc;
    }
    public String getCodeFromCounter(String desc){
        if(desc.startsWith("DurPpsResponseCodeCounter")){
            // + desc.substring("DurPpsResponseCodeCounter".length())
            return "J6" ;
        }
        else if(desc.startsWith("ClinicalInformationCounter")) {
            //   + desc.substring("ClinicalInformationCounter".length())
            return "XE";
        }
        else if (desc.startsWith("DurPpsCodeCounter")){
            // + desc.substring("DurPpsCodeCounter".length())
            return "7E";
        }
        return "ERROR";
    }
    private void populateNCPDP(Map<String, String> map)
    {
        map.put("28","UnitOfMeasure");
        map.put("1C","SmokerNon-SmokerCode");
        map.put("1E","PrescriberLocationCode");
        map.put("2C","PregnancyIndicator");
        map.put("2E","PrimaryCareProviderIdQualifier");
        map.put("2F","NetworkReimbursementId");
        map.put("4C","CoordinationOfBenefitsOtherPaymentsCount");
        map.put("4E","PrimaryCareProviderLastName");
        map.put("4F","RejectFieldOccurrenceIndicator");
        map.put("5C","OtherPayerCoverageType");
        map.put("5E","OtherPayerRejectCount");
        map.put("5F","ApprovedMessageCodeCount");
        map.put("6C","OtherPayerIdQualifier");
        map.put("6E","OtherPayerRejectCode");
        map.put("6F","ApprovedMessageCode");
        map.put("7C","OtherPayerId");
        map.put("7E","DurPpsCodeCounter");
        map.put("7F","HelpDeskPhoneNumberQualifier");
        map.put("8C","FacilityId");
        map.put("8E","DurPpsLevelOfEffort");
        map.put("8F","HelpDeskPhoneNumber");
        map.put("9F","PreferredProductCount");
        map.put("A1","BinNumber");
        map.put("A2","VersionReleaseNumber");
        map.put("A3","TransactionCode");
        map.put("A4","ProcessorControlNumber");
        map.put("A9","TransactionCount");
        map.put("AK","SoftwareVendorCertificationId");
        map.put("AM","SegmentIdentification");
        map.put("AN","TransactionResponseStatus");
        map.put("AP","PreferredProductIdQualifier");
        map.put("AR","PreferredProductId");
        map.put("AS","PreferredProductIncentive");
        map.put("AT","PreferredProductCopayIncentive");
        map.put("AU","PreferredProductDescription");
        map.put("AV","TaxExemptIndicator");
        map.put("AW","FlatSalesTaxAmountPaid");
        map.put("AX","PercentageSalesTaxAmountPaid");
        map.put("AY","PercentageSalesTaxRatePaid");
        map.put("AZ","PercentageSalesTaxBasisPaid");
        map.put("B1","ServiceProviderId");
        map.put("B2","ServiceProviderIdQualifier");
        map.put("BE","ProfessionalServiceFeeSubmitted");
        map.put("C1","GroupId");
        map.put("C2","CardholderId");
        map.put("C3","PersonCode");
        map.put("C4","DateOfBirth");
        map.put("C5","PatientGenderCode");
        map.put("C6","PatientRelationshipCode");
        map.put("C7","PatientLocation");
        map.put("C8","OtherCoverageCode");
        map.put("C9","EligibilityClarificationCode");
        map.put("CA","PatientFirstName");
        map.put("CB","PatientLastName");
        map.put("CC","CardholderFirstName");
        map.put("CD","CardholderLastName");
        map.put("CE","HomePlan");
        map.put("CF","EmployerName");
        map.put("CG","EmployerStreetAddress");
        map.put("CH","EmployerCityAddress");
        map.put("CI","EmployerStateProvinceAddress");
        map.put("CJ","EmployerZipPostalZone");
        map.put("CK","EmployerPhoneNumber");
        map.put("CL","EmployerContactName");
        map.put("CM","PatientStreetAddress");
        map.put("CN","PatientCityAddress");
        map.put("CO","PatientStateProvinceAddress");
        map.put("CP","PatientZipPostalZone");
        map.put("CQ","PatientPhoneNumber");
        map.put("CR","CarrierId");
        map.put("CW","AlternateId");
        map.put("CX","PatientIdQualifier");
        map.put("CY","PatientId");
        map.put("CZ","EmployerId");
        map.put("D1","DateOfService");
        map.put("D2","PrescriptionServiceReferenceNumber");
        map.put("D3","FillNumber");
        map.put("D5","DaysSupply");
        map.put("D6","CompoundCode");
        map.put("D7","ProductServiceId");
        map.put("D8","DispenseAsWrittenProductSelectionCode");
        map.put("D9","IngredientCostSubmitted");
        map.put("DB","PrescriberId");
        map.put("DC","DispensingFeeSubmitted");
        map.put("DE","DatePrescriptionWritten");
        map.put("DF","NumberOfRefillsAuthorized");
        map.put("DI","LevelOfService");
        map.put("DJ","PrescriptionOriginCode");
        map.put("DK","SubmissionClarificationCode");
        map.put("DL","PrimaryCareProviderId");
        map.put("DN","BasisOfCostDetermination");
        map.put("DO","DiagnosisCode");
        map.put("DQ","UsualAndCustomaryCharge");
        map.put("DR","PrescriberLastName");
        map.put("DT","UnitDoseIndicator");
        map.put("DU","GrossAmountDue");
        map.put("DV","OtherPayerAmountPaid");
        map.put("DX","PatientPaidAmountSubmitted");
        map.put("DY","DateOfInjury");
        map.put("DZ","ClaimReferenceId");
        map.put("E1","ProductServiceIdQualifier");
        map.put("E3","IncentiveAmountSubmitted");
        map.put("E4","ReasonForServiceCode");
        map.put("E5","ProfessionalServiceCode");
        map.put("E6","ResultOfServiceCode");
        map.put("E7","QuantityDispensed");
        map.put("E8","OtherPayerDate");
        map.put("E9","ProviderId");
        map.put("EA","OriginallyPrescribedProductServiceCode");
        map.put("EB","OriginallyPrescribedQuantity");
        map.put("EC","CompoundIngredientComponentCount");
        map.put("ED","CompoundIngredientQuantity");
        map.put("EE","CompoundIngredientDrugCost");
        map.put("EF","CompoundDosageFormDescriptionCode");
        map.put("EG","CompoundDispensingUnitFormIndicator");
        map.put("EH","CompoundRouteOfAdministration");
        map.put("EJ","OrigPrescribedProductServiceIdQualifier");
        map.put("EK","ScheduledPrescriptionIdNumber");
        map.put("EM","PrescriptionServiceReferenceNumberQualifier");
        map.put("EN","AssociatedPrescriptionServiceReferenceNumber");
        map.put("EP","AssociatedPrescriptionServiceDate");
        map.put("ER","ProcedureModifierCode");
        map.put("ET","QuantityPrescribed");
        map.put("EU","PriorAuthorizationTypeCode");
        map.put("EV","PriorAuthorizationNumberSubmitted");
        map.put("EW","IntermediaryAuthorizationTypeId");
        map.put("EX","IntermediaryAuthorizationId");
        map.put("EY","ProviderIdQualifier");
        map.put("EZ","PrescriberIdQualifier");
        map.put("F1","HeaderResponseStatus");
        map.put("F3","AuthorizationNumber");
        map.put("F4","Message");
        map.put("F5","PatientPayAmount");
        map.put("F6","IngredientCostPaid");
        map.put("F7","DispensingFeePaid");
        map.put("F9","TotalAmountPaid");
        map.put("FA","RejectCount");
        map.put("FB","RejectCode");
        map.put("FC","AccumulatedDeductibleAmount");
        map.put("FD","RemainingDeductibleAmount");
        map.put("FE","RemainingBenefitAmount");
        map.put("FH","AmountAppliedToPeriodicDeductible");
        map.put("FI","AmountOfCopayCo-Insurance");
        map.put("FJ","AmountAttributedToProductSelection");
        map.put("FK","AmountExceedingPeriodicBenefitMaximum");
        map.put("FL","IncentiveAmountPaid");
        map.put("FM","BasisOfReimbursementDetermination");
        map.put("FN","AmountAttributedToSalesTax");
        map.put("FO","PlanId");
        map.put("FQ","AdditionalMessageInformation");
        map.put("FS","ClinicalSignificanceCode");
        map.put("FT","OtherPharmacyIndicator");
        map.put("FU","PreviousDateOfFill");
        map.put("FV","QuantityOfPreviousFill");
        map.put("FW","DatabaseIndicator");
        map.put("FX","OtherPrescriberIndicator");
        map.put("FY","DurFreeTextMessage");
        map.put("GE","PercentageSalesTaxAmountSubmitted");
        map.put("H1","MeasurementTime");
        map.put("H2","MeasurementDimension");
        map.put("H3","MeasurementUnit");
        map.put("H4","MeasurementValue");
        map.put("H5","PrimaryCareProviderLocationCode");
        map.put("H6","DurCo-AgentId");
        map.put("H7","OtherAmountClaimedSubmittedCount");
        map.put("H8","OtherAmountClaimedSubmittedQualifier");
        map.put("H9","OtherAmountClaimedSubmitted");
        map.put("HA","FlatSalesTaxAmountSubmitted");
        map.put("HB","OtherPayerAmountPaidCount");
        map.put("HC","OtherPayerAmountPaidQualifier");
        map.put("HD","DispensingStatus");
        map.put("HE","PercentageSalesTaxRateSubmitted");
        map.put("HF","QuantityIntendedToBeDispensed");
        map.put("HG","DaysSupplyIntendedToBeDispensed");
        map.put("HH","BasisOfCalculationDispensingFee");
        map.put("HJ","BasisOfCalculationCopay");
        map.put("HK","BasisOfCalculationFlatSalesTax");
        map.put("HM","BasisOfCalculationPercentageSalesTax");
        map.put("J1","ProfessionalServiceFeePaid");
        map.put("J2","OtherAmountPaidCount");
        map.put("J3","OtherAmountPaidQualifier");
        map.put("J4","OtherAmountPaid");
        map.put("J5","OtherPayerAmountRecognized");
        map.put("J6","DurPpsResponseCodeCounter");
        map.put("J7","PayerIdQualifier");
        map.put("J8","PayerId");
        map.put("J9","DurCo-AgentIdQualifier");
        map.put("JE","PercentageSalesTaxBasisSubmitted");
        map.put("KE","CouponType");
        map.put("ME","CouponNumber");
        map.put("NE","CouponValueAmount");
        map.put("PA","RequestType");
        map.put("PB","RequestPeriodDate-Begin");
        map.put("PC","RequestPeriodDate-End");
        map.put("PD","BasisOfRequest");
        map.put("PE","AuthorizedRepresentativeFirstName");
        map.put("PF","AuthorizedRepresentativeLastName");
        map.put("PG","AuthorizedRepresentativeStreetAddress");
        map.put("PH","AuthorizedRepresentativeCityAddress");
        map.put("PJ","AuthorizedRepresentativeStateProvinceAddress");
        map.put("PK","AuthorizedRepresentativeZipPostalZone");
        map.put("PM","PrescriberPhoneNumber");
        map.put("PP","PriorAuthorizationSupportingDocumentation");
        map.put("PR","PriorAuthorizationProcessedDate");
        map.put("PS","PriorAuthorizationEffectiveDate");
        map.put("PT","PriorAuthorizationExpirationDate");
        map.put("PW","PriorAuthorizationNumberOfRefillsAuthorized");
        map.put("PX","PriorAuthorizationQuantityAccumulated");
        map.put("PY","PriorAuthorizationNumber-Assigned");
        map.put("RA","PriorAuthorizationQuantity");
        map.put("RB","PriorAuthorizationDollarsAuthorized");
        map.put("RE","CompoundProductIdQualifier");
        map.put("SE","ProcedureModifierCodeCount");
        map.put("TE","CompoundProductId");
        map.put("UE","CompoundIngredientBasisOfCostDetermination");
        map.put("VE","DiagnosisCodeCount");
        map.put("WE","DiagnosisCodeQualifier");
        map.put("XE","ClinicalInformationCounter");
        map.put("ZE","MeasurementDate");
    }
    private void populateSegments(Map<String, String> map)
    {
        map.put("AM01","Patient");
        map.put("AM02","PharmacyProvider");
        map.put("AM03","Prescriber");
        map.put("AM04","Insurance");
        map.put("AM05","CoordinationOfBenefitsOtherPayments");
        map.put("AM06","WorkersCompensation");
        map.put("AM07","Claim");
        map.put("AM08","DURPPS");
        map.put("AM09","Coupon");
        map.put("AM10","Compound");
        map.put("AM11","Pricing");
        map.put("AM12","PriorAuthorization");
        map.put("AM13","Clinical");
        map.put("AM20","ResponseMessage");
        map.put("AM21","ResponseStatus");
        map.put("AM22","ResponseClaim");
        map.put("AM23","ResponsePricing");
        map.put("AM24","ResponseDURPPS");
        map.put("AM25","ResponseInsurance");
        map.put("AM26","ResponsePriorAuthorization");
    }

    private void populateTransactionType(Map<String, String> map)
    {
        map.put("E1","EligibilityVerification");
        map.put("B1","Billing");
        map.put("B2","Reversal");
        map.put("B3","Rebill");
        map.put("P1","PARequestBilling");
        map.put("P2","PAReversal");
        map.put("P3","PAInquiry");
        map.put("P4","PARequestOnly");
        map.put("N1","InformationReporting");
        map.put("N2","InformationReportingReversal");
        map.put("N3","InformationReportingRebill");
        map.put("C1","ControlledSubstanceReporting");
        map.put("C2","ControlledSubstanceReportingReversal");
        map.put("C3","ControlledSubstanceReportingRebill");
    }
    private void populateRepFields(ArrayList<String> arrayList){
        arrayList.add("ProcedureModifierCode");
        arrayList.add("OtherPayerCoverageType");
        arrayList.add("OtherPayerIdQualifier");
        arrayList.add("OtherPayerId");
        arrayList.add("OtherPayerDate");
        arrayList.add("OtherPayerAmountPaidQualifier");
        arrayList.add("OtherPayerAmountPaid");
        arrayList.add("OtherPayerRejectCode");
        arrayList.add("OtherAmountClaimedSubmittedQualifier");
        arrayList.add("OtherAmountClaimedSubmitted");
        arrayList.add("CompoundProductIdQualifier");
        arrayList.add("CompoundProductId");
        arrayList.add("CompoundIngredientQuantity");
        arrayList.add("CompoundIngredientDrugCost");
        arrayList.add("CompoundIngredientBasisOfCostDetermination");
        arrayList.add("DiagnosisCodeQualifier");
        arrayList.add("DiagnosisCode");
        arrayList.add("RejectCode");
        arrayList.add("RejectFieldOccurrenceIndicator");
        arrayList.add("ApprovedMessageCode");
        arrayList.add("PreferredProductIdQualifier");
        arrayList.add("PreferredProductId");
        arrayList.add("PreferredProductIncentive");
        arrayList.add("PreferredProductCopayIncentive");
        arrayList.add("PreferredProductDescription");
        arrayList.add("OtherAmountPaidQualifier");
        arrayList.add("OtherAmountPaid");
    }

    public boolean isRepeatingField(String fieldDesc) {
        if(repFields.contains(fieldDesc)){
            return true;
        }
        return false;
    }
}
