<?xml version="1.0"?>
<eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.1 http://rs.gbif.org/schema/eml-gbif-profile/1.1/eml.xsd" packageId="" system="http://gbif.org" scope="system" xml:lang="en">
    <dataset>
        <title>GBIF Type Specimen Names</title>
        <creator>
            <individualName>
                <givenName>Markus</givenName>
                <surName>D&#xF6;ring</surName>
            </individualName>
            <organizationName>GBIF Secretariat</organizationName>
            <electronicMailAddress>mdoering@gbif.org</electronicMailAddress>
            <userId directory="http://orcid.org/">0000-0001-7757-1889</userId>
        </creator>
        <metadataProvider>
            <individualName>
                <givenName>Markus</givenName>
                <surName>D&#xF6;ring</surName>
            </individualName>
            <organizationName>GBIF Secretariat</organizationName>
            <electronicMailAddress>mdoering@gbif.org</electronicMailAddress>
            <userId directory="http://orcid.org/">0000-0001-7757-1889</userId>
        </metadataProvider>
        <pubDate>${.now?date?iso_utc}</pubDate>
        <language>en</language>
        <abstract>
            <para>A checklist of names extracted from all GBIF type specimen with parsable names.
                The list uses the verbatim, original data not interpreted by GBIF with the exception
                of the name itself which is parsed using the GBIF Name Parser.
                The detailed procedure can be found at https://github.com/gbif/type-specimen-checklist.</para>
        </abstract>
        <keywordSet>
            <keyword>type specimen</keyword>
            <keyword>nomenclature</keyword>
            <keywordThesaurus>GBIF</keywordThesaurus>
        </keywordSet>
        <intellectualRights>
            <para>
                To the extent possible under law, the publisher has waived all rights to these data and has dedicated them to the
                <ulink url="http://creativecommons.org/publicdomain/zero/1.0/legalcode">
                    <citetitle>Public Domain (CC0 1.0)</citetitle>
                </ulink>
                . Users may copy, modify, distribute and use the work, including for commercial purposes, without restriction.
            </para>
        </intellectualRights>
        <maintenance>
            <maintenanceUpdateFrequency>daily</maintenanceUpdateFrequency>
        </maintenance>
        <distribution scope="document">
            <online>
                <url function="information">https://github.com/gbif/type-specimen-checklist</url>
            </online>
        </distribution>
        <contact>
            <individualName>
                <givenName>Markus</givenName>
                <surName>D&#xF6;ring</surName>
            </individualName>
            <organizationName>GBIF Secretariat</organizationName>
            <electronicMailAddress>mdoering@gbif.org</electronicMailAddress>
            <userId directory="http://orcid.org/">0000-0001-7757-1889</userId>
        </contact>
    </dataset>
    <additionalMetadata>
        <metadata>
            <gbif>
                <dateStamp>${.now?iso_local}</dateStamp>
                <hierarchyLevel>dataset</hierarchyLevel>
            </gbif>
        </metadata>
    </additionalMetadata>
</eml:eml>
