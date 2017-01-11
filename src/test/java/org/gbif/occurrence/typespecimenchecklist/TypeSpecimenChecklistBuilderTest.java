package org.gbif.occurrence.typespecimenchecklist;

import java.io.FileNotFoundException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

/**
 *
 */
public class TypeSpecimenChecklistBuilderTest {

  // the hive folder should not exist locally, expect not found
  @Test(expected = FileNotFoundException.class)
  public void build() throws Exception {
    Configuration conf = new org.apache.hadoop.conf.Configuration();
    FileSystem fs = LocalFileSystem.getLocal(conf);

    TypeSpecimenChecklistBuilder builder = new TypeSpecimenChecklistBuilder(fs, new Path("/tmp/dwca-test.zip"), "dev");
    builder.build();

  }

}