package org.gbif.occurrence.typespecimenchecklist;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 *
 */
public class ConcatIntUDF extends UDF {
  public String evaluate(final String delimiter, List<Integer> ints){
    return ints.stream().map(u-> String.valueOf(u)).collect(Collectors.joining(delimiter));
  }
}
