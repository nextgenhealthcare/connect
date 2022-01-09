
CREATE TABLE debugger_usage
(
    id integer NOT NULL,
    server_id character varying(50) NOT NULL,
    deploy_count integer,
    invocation_count integer,
    postprocessor_count integer,
    preprocessor_count integer,
    undeploy_count integer,
    last_sent timestamp without time zone,
    CONSTRAINT debugger_usage_pkey PRIMARY KEY (id)
)
