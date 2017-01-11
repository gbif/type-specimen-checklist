package org.gbif.occurrence.typespecimenchecklist;

import org.gbif.hadoop.compress.d2.D2CombineInputStream;
import org.gbif.hadoop.compress.d2.zip.ModalZipOutputStream;
import org.gbif.hadoop.compress.d2.zip.ZipEntry;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A builder that will clear and build a new dataset index by paging over the given service.
 */
public class TypeSpecimenChecklistBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(TypeSpecimenChecklistBuilder.class);
  private final Configuration cfg;
  private final FileSystem fs;
  private final Path dwcaFile;
  private final Path hiveTable;

  public TypeSpecimenChecklistBuilder(FileSystem fs, Path dwcaFile, String hiveDB) {
    this.fs = fs;
    this.dwcaFile = dwcaFile;
    this.hiveTable = hivePath(hiveDB, "type_specimen_taxa");
    cfg = new Configuration();
    cfg.setTemplateLoader(new ClassTemplateLoader(TypeSpecimenChecklistBuilder.class, "/templates"));
  }

  private static Path hivePath(String db, String table) {
    return new Path("/user/hive/warehouse/"+db+".db/"+table);
  }

  /**
   * Bundles hive query results as taxon.tsv with eml and meta.xml
   */
  public void build() throws Exception {
    LOG.info("Building a new type specimen checklist DwC-A at {}", dwcaFile);
    try (
        FSDataOutputStream zipped = fs.create(dwcaFile, true);
        ModalZipOutputStream zos = new ModalZipOutputStream(new BufferedOutputStream(zipped));
    ) {
      addEmlEntry(zos);
      addResourceEntry(zos, "meta.xml");
      addTaxonEntry(zos);

    } catch (Exception ex) {
      LOG.error("Failed to zip archive", ex);
      throw Throwables.propagate(ex);
    }

    LOG.info("Finished building type specimen checklist DwC-A");
  }

  private void addTaxonEntry(ModalZipOutputStream zos) throws IOException {
    //Get all the files inside the directory and creates a list of InputStreams.
    D2CombineInputStream in =
        new D2CombineInputStream(Lists.transform(Lists.newArrayList(fs.listStatus(hiveTable)),
            new Function<FileStatus, InputStream>() {
              @Nullable
              @Override
              public InputStream apply(@Nullable FileStatus input) {
                try {
                  return fs.open(input.getPath());
                } catch (IOException ex) {
                  throw Throwables.propagate(ex);
                }
              }
            }));
    ZipEntry ze = new ZipEntry(Paths.get("taxon.tsv").toString());
    zos.putNextEntry(ze, ModalZipOutputStream.MODE.PRE_DEFLATED);
    ByteStreams.copy(in, zos);
    in.close(); // required to get the sizes
    ze.setSize(in.getUncompressedLength()); // important to set the sizes and CRC
    ze.setCompressedSize(in.getCompressedLength());
    ze.setCrc(in.getCrc32());
    zos.closeEntry();
  }

  private void addEmlEntry(ModalZipOutputStream zos) throws IOException {
    Template template = cfg.getTemplate("eml.ftl");
    Map<String, Object> data = ImmutableMap.<String, Object>of("license", "CC0");

    ZipEntry ze = new ZipEntry("eml.xml");
    zos.putNextEntry(ze, ModalZipOutputStream.MODE.DEFAULT);

    Writer writer = new OutputStreamWriter(zos);
    try {
      template.process(data, writer);

    } catch (TemplateException e) {
      LOG.error("Failed to write EML, Freemarker error", e);
      throw new IOException(e);

    } finally {
      zos.closeEntry();
    }
  }

  private void addResourceEntry(ModalZipOutputStream zos, String resourceName) throws IOException {
    ZipEntry ze = new ZipEntry(resourceName);
    zos.putNextEntry(ze, ModalZipOutputStream.MODE.DEFAULT);

    try (InputStream meta = getClass().getResourceAsStream(resourceName)) {
      ByteStreams.copy(meta, zos);
      zos.closeEntry();
    }
  }

  public static void run (Properties props) {
    try {
      // read properties and check args
      Path dwca = new Path(props.getProperty("dwca.file"));
      String db = props.getProperty("hiveDB");
      String namenode = props.getProperty("namenode");
      TypeSpecimenChecklistBuilder builder = new TypeSpecimenChecklistBuilder(getHdfs(namenode), dwca, db);
      builder.build();

    } catch (Exception e) {
      LOG.error("Failed to run TypeSpecimenChecklistBuilder", e);
      System.exit(1);
    }
  }

  private static FileSystem getHdfs(String nameNode) throws IOException {
    // filesystem configs
    org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
    conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, nameNode);
    return FileSystem.get(conf);
  }

  public static void main (String[] args) throws IOException {
    run(WorkflowUtils.loadProperties(args));
    System.exit(0);
  }
}
