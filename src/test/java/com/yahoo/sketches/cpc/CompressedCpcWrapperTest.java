/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.cpc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.PrintStream;

import org.testng.annotations.Test;

import com.yahoo.memory.Memory;
import com.yahoo.sketches.Family;
import com.yahoo.sketches.SketchesArgumentException;

/**
 * @author Lee Rhodes
 */
public class CompressedCpcWrapperTest {
  static PrintStream ps = System.out;

  @SuppressWarnings("unused")
  @Test
  public void check() {
    int lgK = 10;
    CpcSketch sk1 = new CpcSketch(lgK);
    CpcSketch sk2 = new CpcSketch(lgK);
    CpcSketch skD = new CpcSketch(lgK);
    double dEst = skD.getEstimate();
    double dlb = skD.getLowerBound(2);
    double dub = skD.getUpperBound(2);

    int n = 100000;
    for (int i = 0; i < n; i++) {
      sk1.update(i);
      sk2.update(i + n);
      skD.update(i);
      skD.update(i + n);
    }
    byte[] concatArr = skD.toCompressedByteArray();

    CpcUnion union = new CpcUnion(lgK);
    CpcSketch result = union.getResult();
    double uEst = result.getEstimate();
    double ulb = result.getLowerBound(2);
    double uub = result.getUpperBound(2);
    union.update(sk1);
    union.update(sk2);
    CpcSketch merged = union.getResult();
    byte[] mergedArr = merged.toCompressedByteArray();

    Memory concatMem = Memory.wrap(concatArr);
    CompressedCpcWrapper concatSk = new CompressedCpcWrapper(concatMem);
    assertEquals(concatSk.getLgK(), lgK);

    printf("              %12s %12s %12s\n", "Lb", "Est", "Ub");
    double ccEst = concatSk.getEstimate();
    double ccLb = concatSk.getLowerBound(2);
    double ccUb = concatSk.getUpperBound(2);
    printf("Concatenated: %12.0f %12.0f %12.0f\n", ccLb, ccEst, ccUb);

    //Memory mergedMem = Memory.wrap(mergedArr);
    CompressedCpcWrapper mergedSk = new CompressedCpcWrapper(mergedArr);
    double mEst = mergedSk.getEstimate();
    double mLb = mergedSk.getLowerBound(2);
    double mUb = mergedSk.getUpperBound(2);
    printf("Merged:       %12.0f %12.0f %12.0f\n", mLb, mEst, mUb);
    assertEquals(Family.CPC, CompressedCpcWrapper.getFamily());
  }

  @SuppressWarnings("unused")
  @Test
  public void checkIsCompressed() {
    CpcSketch sk = new CpcSketch(10);
    byte[] byteArr = sk.toCompressedByteArray();
    byteArr[5] &= (byte) -3;
    try {
      CompressedCpcWrapper wrapper = new CompressedCpcWrapper(Memory.wrap(byteArr));
      fail();
    } catch (SketchesArgumentException e) {}
  }

  /**
   * @param format the string to print
   * @param args the arguments
   */
  static void printf(String format, Object... args) {
    //ps.printf(format, args); //disable here
  }

  /**
   * @param s the string to print
   */
  static void println(String s) {
    //ps.println(s);  //disable here
  }

}