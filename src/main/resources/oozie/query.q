USE ${hiveDB};

-- setup for our custom, combinable deflated compression
SET hive.exec.compress.output=true;
SET io.seqfile.compression.type=BLOCK;
SET mapred.output.compression.codec=org.gbif.hadoop.compress.d2.D2Codec;
SET io.compression.codecs=org.gbif.hadoop.compress.d2.D2Codec;
SET hive.groupby.orderby.position.alias=true;
CREATE TEMPORARY FUNCTION canonicalNameComplete AS 'org.gbif.occurrence.typespecimenchecklist.NameParserUDF';

-- in case this job is relaunched
DROP TABLE IF EXISTS type_specimen_ids;
DROP TABLE IF EXISTS type_specimen_taxa;

-- first create a table with just name and gbifid so we can distinct easily on the name
CREATE TABLE type_specimen_ids
AS SELECT canonicalNameComplete(v_scientificName, v_scientificNameAuthorship) AS nameComplete, min(gbifid) AS gbifid
  FROM occurrence_hdfs
  WHERE typeStatus != null AND v_scientificName RLIKE '^[A-Z][a-z]'
  GROUP BY 1;

-- create filtered type specimen table so it can be used in the multi-insert
CREATE TABLE type_specimen_taxa ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
  TBLPROPERTIES ("serialization.null.format"="")
  AS SELECT o.gbifid, o.datasetkey, s.nameComplete AS scientificName, o.v_taxonRank,
    o.v_kingdom, o.v_phylum, o.v_class, o.v_order, o.v_family, CONCAT("http://www.gbif.org/occurrence/", o.gbifid) AS link
  FROM occurrence_hdfs o JOIN type_specimen_ids s ON o.gbifid=s.gbifid
  WHERE s.nameComplete != NULL;




add JAR /tmp/tt.py;