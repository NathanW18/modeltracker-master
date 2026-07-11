# script to seed the modeltracker database biosample table and strdata table
# using the excel file provided by living biobank 
USE modeltracker_unittest;

# step 1: create a temporary table for str match sheet 
CREATE TABLE bio_sample_temp
(
   id                     BIGINT         NOT NULL,
   reference_sample                VARCHAR(255),
   sample_descriptor  VARCHAR(255),
   model_display_name             VARCHAR(255),
   percentage_str_match         FLOAT(3),
   collection_date        VARCHAR(50),
   comments        VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;

# step 2: load the excel dataset from the str match sheet
LOAD DATA LOCAL INFILE '/Users/kevinwang/Projects/modeltracker/tempb.csv'
INTO TABLE bio_sample_temp
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';

# step 3: create the temporary table for biosample sheet
CREATE TABLE bio_sample_temp_c
(
   id                     BIGINT         NOT NULL,
   collection_date        VARCHAR(50),
   sample_descriptor  VARCHAR(255),
   tissue_origina                VARCHAR(255),
   tumor_site         VARCHAR(255),
   study              VARCHAR(255),
   study_number       VARCHAR(255),
   ppid             VARCHAR(255),
   specimen_barcode         VARCHAR(255),
   reference_str        VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;

# step 4: load the biosample sheet into the temp table
LOAD DATA LOCAL INFILE '/Users/kevinwang/Projects/modeltracker/tempc.csv'
INTO TABLE bio_sample_temp_c
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';


/*
SELECT bio_sample_temp.reference_sample, bio_sample_temp.sample_descriptor, bio_sample_temp.model_display_name, bio_sample_temp.percentage_str_match, bio_sample_temp.collection_date, bio_sample_temp_c.ppid
FROM bio_sample_temp 
INNER JOIN bio_sample_temp_c  ON bio_sample_temp.sample_descriptor = bio_sample_temp_c.sample_descriptor;
*/

# step 5: join the two temp tables to get the final tables with both str match and ppid info
# then insert into the bio_sample table
INSERT INTO bio_sample (id, reference_sample_descriptor, sample_descriptor, model_display_name, percentagestrmatch, protocol_paticipantid, recieving_date)
SELECT bio_sample_temp.id,  bio_sample_temp.reference_sample, bio_sample_temp.sample_descriptor, bio_sample_temp.model_display_name, bio_sample_temp.percentage_str_match, bio_sample_temp_c.ppid,bio_sample_temp.collection_date 
FROM bio_sample_temp 
INNER JOIN bio_sample_temp_c  ON bio_sample_temp.sample_descriptor = bio_sample_temp_c.sample_descriptor;

# step 6: final step is to drop the 2 temp tables which can be down using mysql client manually
DROP bio_sample_temp_c;
DROP bio_sample_temp;


/*
CREATE TABLE strdata
(
   id                 BIGINT         NOT NULL,
   panel              VARCHAR(255),
   sample_descriptor  VARCHAR(255),
   marker             VARCHAR(255),
   allele1            VARCHAR(255),
   allele2            VARCHAR(255),
   allele3            VARCHAR(255),
   allele4            VARCHAR(255),
   allele1n2          VARCHAR(255),
   collection_date               VARCHAR(255),
   PRIMARY KEY (id)
)
ENGINE=MyISAM;
*/

# load the str data from excel sheet into the strdata table
LOAD DATA LOCAL INFILE '/Users/kevinwang/Projects/modeltracker/str_output.csv'
INTO TABLE strdata
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';

# create the initial studies
INSERT INTO study (study_short_name,principle_investigator,study_long_name) 
VALUES
  ('PHLC','Kevin Test','PHLC'),
  ('LPNO','Kevin Test','LPNO'),
  ('LPTO','Kevin Test','LPTO'),
  ('NOTCH','Kevin Test','NOTCH'),
  ('SLPC','Kevin Test','SLPC'),
  ('MPM','Kevin Test','MPM'),
  ('MB','Kevin Test','MB'),
  ('TEST','test test','TEST');


# create the initial models
INSERT INTO model (model_identifier,study_short_name,barcode,model_display_name,protocol_participantid) 
VALUES
  (1,'TEST',NULL,'TEST001','G_12345'),
  (2,'TEST',NULL,'TEST002','G_12345'),
  (3,'TEST',NULL,'TEST003','G_12346');

