CREATE SEQUENCE DEBUG_USAGE_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE DEBUGGER_USAGE
    (ID NUMBER(38) DEFAULT DEBUG_USAGE_SEQUENCE.NEXTVAL NOT NULL PRIMARY KEY,
    SERVER_ID VARCHAR(50) NOT NULL,
    DUPP_COUNT NUMBER(38),
    ATTACH_BATCH_COUNT NUMBER(38),
    SOURCE_CONNECTOR_COUNT NUMBER(38),
    SOURCE_FILTER_TRANS_COUNT NUMBER(38),
    DESTINATION_FILTER_TRANS_COUNT NUMBER(38),
    DESTINATION_CONNECTOR_COUNT NUMBER(38),
    RESPONSE_COUNT NUMBER(38),
    INVOCATION_COUNT NUMBER(38));

ALTER TABLE PERSON 
    ADD ROLE VARCHAR(40),
    ADD COUNTRY VARCHAR(40),
    ADD STATETERRITORY VARCHAR(40),
    ADD USERCONSENT CHAR(1);
