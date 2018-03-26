# type-specimen-checklist
This project covers the Java classes and Oozie workflow and coordinator job needed to create a Darwin Core Checklist Archive for all distinct names 
that GBIF has type specimens for on a regular basis. 
Only names that are parsable with the GBIF Name Parser are included to exclude problematic names.

## How to run the Oozie workflow once
To run the workflow use the script [install-workflow](install-workflow.sh)  ```./install-workflow.sh dev gitOAuthToken``` which requires 2 command line parameters:
  
 - profile/environment name: properties file name as is stored in the GBIF configuration repository at the location https://github.com/gbif/gbif-configuration/tree/master/type-specimen-dwca-builder.
 - Github OAuth token: Github authentication token to access the private repository https://github.com/gbif/gbif-configuration/ where the configuration files are stored.

The configuration file used by this workflow requires the following settings:
  
```
# hdfs
namenode=hdfs://ha-nn
jobtracker=prodmaster3-vh.gbif.org:8032
# oozie
oozie.url=http://oozie.gbif.org:11000/oozie
oozie.use.system.libpath=true
oozie.libpath=hdfs://ha-nn/type-specimen-dwca-builder-uat/lib/
oozie.launcher.mapreduce.task.classpath.user.precedence=true
user.name=yarn
environment=uat
# in days
coordFrequency=7

# hive
hiveDB=prod_b
hiveMetastore=thrift://prodmaster1-vh.gbif.org:9083

# dwca
dwca.file=/occurrence-download/uat-downloads/gbif-type-specimen-checklist.zip
```

## Installing the Oozie coordinator job
The same workflow can be executed as an Oozie coordinator job running on a regular basis (see coordFrequency config property) using the same configs as for the workflow above.
To install the coordinator job, removing any previous coordinator job, 
use the script [install-coordinator.sh](install-coordinator.sh)  ```./install-coordinator.sh dev gitOAuthToken``` which again requires the 2 command line parameters described above.
