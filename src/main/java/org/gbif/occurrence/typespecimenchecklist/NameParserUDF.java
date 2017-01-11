package org.gbif.occurrence.typespecimenchecklist;

import org.gbif.api.exception.UnparsableException;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.api.service.checklistbank.NameParser;
import org.gbif.nameparser.GBIFNameParser;

import com.google.common.base.Strings;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses scientific name & authors into a standard representation or null if it fails
 */
@Description(
  name = "canonicalNameComplete",
  value = "_FUNC_(scientificName, authorship)")
public class NameParserUDF extends GenericUDF {
  private static final Logger LOG = LoggerFactory.getLogger(NameParserUDF.class);
  private static final NameParser PARSER = new GBIFNameParser();
  private static final int ARG_LENGTH = 2;
  private ObjectInspectorConverters.Converter[] converters;

  @Override
  public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
    if (arguments.length != ARG_LENGTH) {
      throw new UDFArgumentException("canonicalNameComplete takes "+ ARG_LENGTH +" arguments");
    }

    converters = new ObjectInspectorConverters.Converter[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      converters[i] = ObjectInspectorConverters.getConverter(arguments[i], PrimitiveObjectInspectorFactory.writableStringObjectInspector);
    }

    return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
  }

  public Object evaluate(GenericUDF.DeferredObject[] arguments) throws HiveException {
    assert arguments.length == ARG_LENGTH;

    String sciname = converters[0].convert(arguments[0].get()).toString();
    String author = converters[1].convert(arguments[1].get()).toString();

    try {
      ParsedName pn = PARSER.parse(sciname);

      // append author if its not part of the name yet
      if (!Strings.isNullOrEmpty(author) && !sciname.contains(author) &&
          (!pn.isAuthorsParsed() || Strings.isNullOrEmpty(pn.getAuthorship()))) {
        pn.setAuthorship(author.trim());
      }

      return pn.canonicalNameComplete();

    } catch (UnparsableException e) {
      LOG.info("Unparsable name {}", sciname);
      return null;
    }
  }

  @Override
  public String getDisplayString(String[] args) {
    assert args.length == 2;
    return "canonicalNameComplete(" + args[0] + ", " + args[1] + ')';
  }

}
