#!/usr/bin/env bash
#exit on any failure
set -e

ENV=$1
TOKEN=$2

echo "Getting latest type-specimen-dwca-builder properties file from github"
curl -s -H "Authorization: token $TOKEN" -H 'Accept: application/vnd.github.v3.raw' -O -L https://api.github.com/repos/gbif/gbif-configuration/contents/type-specimen-dwca-builder/$ENV.properties

#extract the oozie.url value from the properties file
oozie_url=`cat $ENV.properties| grep "oozie.url" | cut -d'=' -f2-`
namenode=`cat $ENV.properties| grep "namenode" | cut -d'=' -f2-`

echo "Assembling jar for $ENV"

#Oozie uses timezone UTC
mvn -U clean package -Duser.timezone=UTC -DskipTests assembly:single

#gets the oozie id of the current coordinator job if it exists
WID=$(oozie jobs -oozie ${oozie_url} -jobtype coordinator -filter name=TypeSpecimenDwcaBuilder-$ENV\;status=RUNNING\;status=PREP\;status=PREPSUSPENDED\;status=SUSPENDED\;status=PREPPAUSED\;status=PAUSED\;status=SUCCEEDED\;status=DONEWITHERROR\;status=FAILED |  awk 'NR==3' | awk '{print $1;}')
if [ -n "$WID" ]; then
  echo "Killing current coordinator job" $WID
  oozie job -oozie ${oozie_url} -kill $WID
fi

if hdfs dfs -test -d /type-specimen-dwca-builder-$ENV/; then
   echo "Removing content of current Oozie workflow directory"
   hdfs dfs -rm -f -r /type-specimen-dwca-builder-$ENV/*
else
   echo "Creating workflow directory"
   hdfs dfs -mkdir /type-specimen-dwca-builder-$ENV/
fi
echo "Copying new Oozie workflow to HDFS"
hdfs dfs -copyFromLocal target/oozie-workflow/* /type-specimen-dwca-builder-$ENV/
hdfs dfs -copyFromLocal $ENV.properties /type-specimen-dwca-builder-$ENV/lib/

echo "Running Oozie coordinator job"
oozie job --oozie ${oozie_url} -config $ENV.properties -DstartTime=`date -u "+%Y-%m-%dT03:00Z"` -Doozie.coord.application.path=${namenode}/type-specimen-dwca-builder-$ENV/ -run


