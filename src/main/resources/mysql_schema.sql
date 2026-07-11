CREATE DATABASE modeltracker;

GRANT ALL PRIVILEGES ON modeltracker.* TO 'modeltrackeruser'@'localhost' IDENTIFIED BY 'tech20na13';

CREATE TABLE modeltracker.bio_sample
(
   id                        BIGINT         NOT NULL,
   barcode                   VARCHAR(255),
   collection_date           VARCHAR(255),
   model_display_name        VARCHAR(255),
   primary_disease_site      VARCHAR(255),
   protocol_paticipantid     VARCHAR(255),
   sample_descriptor         VARCHAR(255),
   sample_type               VARCHAR(255),
   comment                   VARCHAR(255),
   intervention_description  VARCHAR(255),
   intervention_end_date     VARCHAR(255),
   intervention_start_date   VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;

CREATE TABLE modeltracker.model
(
   model_identifier           VARCHAR(200)   NOT NULL,
   study_short_name           VARCHAR(50)    NOT NULL,
   comment                    VARCHAR(255),
   model_display_name         VARCHAR(255),
   protocol_participantid     VARCHAR(255),
   parent_model_display_name  VARCHAR(255),
   PRIMARY KEY (model_identifier, study_short_name)
)
ENGINE=MyISAM;

CREATE TABLE modeltracker.stranalysis
(
   receiving_date        VARCHAR(50)    NOT NULL,
   sample_descriptor     VARCHAR(200)   NOT NULL,
   comment               VARCHAR(255),
   is_reference_str      BIT,
   match_count           INT,
   percentage_str_match  FLOAT,
   total_count           INT,
   sample_id             BIGINT,
   PRIMARY KEY (receiving_date, sample_descriptor)
)
ENGINE=MyISAM;

CREATE INDEX FKkyfy8ellugga3bldky03hpaiv
   ON stranalysis (sample_id ASC);

CREATE TABLE modeltracker.strdata
(
   id                 BIGINT         NOT NULL,
   allele1            VARCHAR(255),
   allele1n2          VARCHAR(255),
   allele2            VARCHAR(255),
   allele3            VARCHAR(255),
   allele4            VARCHAR(255),
   marker             VARCHAR(255),
   receiving_date     VARCHAR(255),
   sample_descriptor  VARCHAR(255),
   panel              VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;

CREATE TABLE modeltracker.study
(
   study_short_name        VARCHAR(255)   NOT NULL,
   principle_investigator  VARCHAR(255),
   study_long_name         VARCHAR(255),
   comment                 VARCHAR(255),
   PRIMARY KEY (study_short_name)
)
ENGINE=MyISAM;

CREATE TABLE modeltracker.data_field_option
(
   id                  BIGINT         NOT NULL,
   field_option_name   VARCHAR(255),
   field_option_value  VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;