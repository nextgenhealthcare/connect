--
-- This command file reloads a database that was unloaded using "dbunload".
--
-- ( Version :  9.0.2.2451)
--

SET OPTION Statistics          = 3
go
SET OPTION Date_order          = 'YMD'
go
SET OPTION describe_java_format= 'binary'
go



-------------------------------------------------
--   Create tables
-------------------------------------------------

CREATE TABLE "mirth"."SCHEMA_INFO"
(
	"VERSION"       		varchar(40) NULL ,
	
)
go
CREATE TABLE "mirth"."SCRIPT"
(
	"ID"    			varchar(255) NOT NULL ,
	"SCRIPT"        		"text" NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."TEMPLATE"
(
	"ID"    			varchar(255) NOT NULL ,
	"TEMPLATE"      		"text" NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."ENCRYPTION_KEY"
(
	"DATA"  			"text" NOT NULL ,
	
)
go
CREATE TABLE "mirth"."ATTACHMENT"
(
	"ID"    			varchar(255) NOT NULL ,
	"MESSAGE_ID"    		varchar(255) NOT NULL ,
	"ATTACHMENT_DATA"       	"image" NULL ,
	"ATTACHMENT_SIZE"       	integer NULL ,
	"ATTACHMENT_TYPE"       	varchar(40) NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."CONFIGURATION"
(
	"ID"    			integer NOT NULL DEFAULT autoincrement ,
	"DATE_CREATED"  		"datetime" NULL DEFAULT GETDATE(*) ,
	"DATA"  			"text" NOT NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."EVENT"
(
	"ID"    			integer NOT NULL DEFAULT autoincrement ,
	"DATE_CREATED"  		"datetime" NULL DEFAULT GETDATE(*) ,
	"EVENT" 			"text" NOT NULL ,
	"EVENT_LEVEL"   		varchar(40) NOT NULL ,
	"DESCRIPTION"   		"text" NULL ,
	"ATTRIBUTES"    		"text" NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."PERSON"
(
	"ID"    			integer NOT NULL DEFAULT autoincrement ,
	"USERNAME"      		varchar(40) NOT NULL ,
	"PASSWORD"      		varchar(40) NOT NULL ,
	"SALT"  			varchar(40) NOT NULL ,
	"FULLNAME"      		varchar(255) NULL ,
	"EMAIL" 			varchar(255) NULL ,
	"PHONENUMBER"   		varchar(40) NULL ,
	"DESCRIPTION"   		varchar(255) NULL ,
	"LAST_LOGIN"    		"datetime" NULL DEFAULT GETDATE(*) ,
	"LOGGED_IN"     		smallint NOT NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."MESSAGE"
(
	"SEQUENCE_ID"   		integer NOT NULL DEFAULT autoincrement ,
	"ID"    			varchar(255) NOT NULL ,
	"SERVER_ID"     		varchar(255) NOT NULL ,
	"CHANNEL_ID"    		varchar(255) NOT NULL ,
	"SOURCE"        		varchar(255) NULL ,
	"TYPE"  			varchar(255) NULL ,
	"DATE_CREATED"  		"datetime" NOT NULL ,
	"VERSION"       		varchar(40) NULL ,
	"IS_ENCRYPTED"  		smallint NOT NULL ,
	"STATUS"        		varchar(40) NULL ,
	"RAW_DATA"      		"text" NULL ,
	"RAW_DATA_PROTOCOL"     	varchar(40) NULL ,
	"TRANSFORMED_DATA"      	"text" NULL ,
	"TRANSFORMED_DATA_PROTOCOL"     varchar(40) NULL ,
	"ENCODED_DATA"  		"text" NULL ,
	"ENCODED_DATA_PROTOCOL" 	varchar(40) NULL ,
	"CONNECTOR_MAP" 		"text" NULL ,
	"CHANNEL_MAP"   		"text" NULL ,
	"RESPONSE_MAP"  		"text" NULL ,
	"CONNECTOR_NAME"        	varchar(255) NULL ,
	"ERRORS"        		"text" NULL ,
	"CORRELATION_ID"        	varchar(255) NULL ,
	"ATTACHMENT"    		smallint NULL , 
	 PRIMARY KEY ("SEQUENCE_ID"),
	
)
go

ALTER TABLE "mirth"."MESSAGE" ADD  UNIQUE 
(
	"ID"
)
go
CREATE TABLE "mirth"."CHANNEL_STATISTICS"
(
	"SERVER_ID"     		varchar(255) NOT NULL ,
	"CHANNEL_ID"    		varchar(255) NOT NULL ,
	"RECEIVED"      		integer NULL ,
	"FILTERED"      		integer NULL ,
	"SENT"  			integer NULL ,
	"ERROR" 			integer NULL ,
	"QUEUED"        		integer NULL , 
	 PRIMARY KEY ("SERVER_ID", "CHANNEL_ID"),
	
)
go
CREATE TABLE "mirth"."CHANNEL"
(
	"ID"    			varchar(255) NOT NULL ,
	"NAME"  			varchar(40) NOT NULL ,
	"DESCRIPTION"   		"text" NULL ,
	"IS_ENABLED"    		smallint NULL ,
	"VERSION"       		varchar(40) NULL ,
	"REVISION"      		integer NULL ,
	"LAST_MODIFIED" 		"datetime" NULL DEFAULT GETDATE(*) ,
	"SOURCE_CONNECTOR"      	"text" NULL ,
	"DESTINATION_CONNECTORS"        "text" NULL ,
	"PROPERTIES"    		"text" NULL ,
	"PREPROCESSING_SCRIPT"  	"text" NULL ,
	"POSTPROCESSING_SCRIPT" 	"text" NULL ,
	"DEPLOY_SCRIPT" 		"text" NULL ,
	"SHUTDOWN_SCRIPT"       	"text" NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
CREATE TABLE "mirth"."ALERT_EMAIL"
(
	"ALERT_ID"      		varchar(255) NOT NULL ,
	"EMAIL" 			varchar(255) NOT NULL ,
	
)
go
CREATE TABLE "mirth"."CHANNEL_ALERT"
(
	"CHANNEL_ID"    		varchar(255) NOT NULL ,
	"ALERT_ID"      		varchar(255) NOT NULL ,
	
)
go
CREATE TABLE "mirth"."ALERT"
(
	"ID"    			varchar(255) NOT NULL ,
	"NAME"  			varchar(40) NOT NULL ,
	"IS_ENABLED"    		smallint NOT NULL ,
	"EXPRESSION"    		"text" NULL ,
	"TEMPLATE"      		"text" NULL , 
	 PRIMARY KEY ("ID"),
	
)
go
commit work
go


-------------------------------------------------
--   Reload data
--	edited to skip all the empty tables
-- 	edited to translate load sentences to INSERT's
-------------------------------------------------

INSERT INTO "mirth"."SCHEMA_INFO" ("VERSION") VALUES ('2');
go

INSERT INTO "mirth"."PERSON" ("USERNAME","PASSWORD","SALT","LOGGED_IN") 
VALUES ('admin','NdgB6ojoGb/uFa5amMEyBNG16mE=','Np+FZYzu4M0=',0); -- REVISAR esto!!
go


commit work
go


-------------------------------------------------
--   Add foreign key definitions
-------------------------------------------------


ALTER TABLE "mirth"."MESSAGE"
	ADD FOREIGN KEY "CHANNEL_ID_FK" ("CHANNEL_ID") 
	REFERENCES "mirth"."CHANNEL" ("ID") on delete cascade
go

CREATE INDEX "MESSAGE_INDEX1" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC
)
go

CREATE INDEX "MESSAGE_INDEX2" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC,
	"CONNECTOR_NAME" ASC
)
go

CREATE INDEX "MESSAGE_INDEX3" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC,
	"RAW_DATA_PROTOCOL" ASC
)
go

CREATE INDEX "MESSAGE_INDEX4" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC,
	"SOURCE" ASC
)
go

CREATE INDEX "MESSAGE_INDEX5" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC,
	"STATUS" ASC
)
go

CREATE INDEX "MESSAGE_INDEX6" ON "mirth"."MESSAGE"
(
	"CHANNEL_ID" ASC,
	"DATE_CREATED" ASC,
	"TYPE" ASC
)
go

ALTER TABLE "mirth"."CHANNEL_STATISTICS"
	ADD FOREIGN KEY "CHANNEL_STATS_ID_FK" ("CHANNEL_ID") 
	REFERENCES "mirth"."CHANNEL" ("ID") on delete cascade
go

ALTER TABLE "mirth"."ALERT_EMAIL"
	ADD FOREIGN KEY "ALERT_ID_AE_FK" ("ALERT_ID") 
	REFERENCES "mirth"."ALERT" ("ID") on delete cascade
go

ALTER TABLE "mirth"."CHANNEL_ALERT"
	ADD FOREIGN KEY "ALERT_ID_CA_FK" ("ALERT_ID") 
	REFERENCES "mirth"."ALERT" ("ID") on delete cascade
go
commit work
go


-------------------------------------------------
--   Create triggers
-------------------------------------------------

commit work
go

