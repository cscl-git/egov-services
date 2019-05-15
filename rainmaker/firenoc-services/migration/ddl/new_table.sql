CREATE TABLE eg_fn_buildinguoms
(
    uuid character varying(64) NOT NULL,
    code character varying(128),
    activeuom boolean,
    active boolean,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    buildinguuid character varying(64) NOT NULL,
    CONSTRAINT pk_eg_fn_buildinguoms PRIMARY KEY (uuid),
    CONSTRAINT fk_eg_fn_buildinguoms FOREIGN KEY (buildinguuid)
        REFERENCES eg_fn_buidlings (uuid) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

ALTER TABLE eg_fn_firenoc
    ALTER COLUMN uuid TYPE character varying (64);
ALTER TABLE eg_fn_firenoc
    ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE eg_fn_firenoc
    ADD CONSTRAINT pk_eg_fn_firenoc PRIMARY KEY (uuid);

ALTER TABLE eg_fn_firenocdetail
        ALTER COLUMN firenocuuid TYPE character varying (128);
ALTER TABLE eg_fn_firenocdetail
        ALTER COLUMN firenocuuid SET NOT NULL;
ALTER TABLE eg_fn_firenocdetail
        ADD CONSTRAINT fk_eg_fn_firenocdetail_firenocuuid FOREIGN KEY (firenocuuid)
        REFERENCES eg_fn_firenoc (uuid) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE;

ALTER TABLE eg_fn_buidlings DROP COLUMN usagetype;

ALTER TABLE eg_fn_buidlings DROP COLUMN nooffloors;

ALTER TABLE eg_fn_buidlings DROP COLUMN noofbasements;

ALTER TABLE eg_fn_buidlings DROP COLUMN plotsize;

ALTER TABLE eg_fn_buidlings DROP COLUMN builtuparea;

ALTER TABLE eg_fn_buidlings DROP COLUMN additionalunitdetail;

ALTER TABLE eg_fn_buidlings DROP COLUMN heightofbuilding;

ALTER TABLE eg_fn_buidlings
            ALTER COLUMN firenocdetailsuuid TYPE character varying (64);
ALTER TABLE eg_fn_buidlings
            ALTER COLUMN firenocdetailsuuid SET NOT NULL;
ALTER TABLE eg_fn_buidlings
            ADD CONSTRAINT fk_eg_fn_buildings FOREIGN KEY (firenocdetailsuuid)
            REFERENCES eg_fn_buidlings (uuid) MATCH SIMPLE
            ON UPDATE CASCADE
            ON DELETE CASCADE;