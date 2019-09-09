package org.vitrivr.cineast.core.features.testing;

import java.awt.Polygon;

public class MergePolynom {

  public static Polygon addPolygon(Polygon[] p) {
    return null;

  }

  public static Polygon[] normalizePolygon(Polygon ref, Polygon p) {

    /**
     * std::int64_t min_d = std::numeric_limits<std::int64_t>::max();
     * 				size_t best_start = 0, best_order = 0;
     */

    int minD = 0;
    int bestStart = 0;
    int bestOrder = 0;

    for (int start = 0; start < 4; start++) {
      int i = start;
      int d;

      d = (int) (Math.sqrt(ref.xpoints[(i + 0) % 4] - p.xpoints[(i + 0) % 4]) +
          Math.sqrt(ref.ypoints[(i + 0) % 4] - p.ypoints[(i + 0) % 4]) +
          Math.sqrt(ref.xpoints[(i + 1) % 4] - p.xpoints[(i + 1) % 4]) +
          Math.sqrt(ref.ypoints[(i + 1) % 4] - p.ypoints[(i + 1) % 4]) +
          Math.sqrt(ref.xpoints[(i + 2) % 4] - p.xpoints[(i + 2) % 4]) +
          Math.sqrt(ref.ypoints[(i + 2) % 4] - p.ypoints[(i + 2) % 4]) +
          Math.sqrt(ref.xpoints[(i + 3) % 4] - p.xpoints[(i + 3) % 4]) +
          Math.sqrt(ref.ypoints[(i + 3) % 4] - p.ypoints[(i + 3) % 4]));

      if (d < minD) {
        minD = d;
        bestStart = start;
        bestOrder = 0;
      }

      d = (int) (Math.sqrt(ref.xpoints[(i + 0) % 4] - p.xpoints[(i + 3) % 4]) +
          Math.sqrt(ref.ypoints[(i + 0) % 4] - p.ypoints[(i + 3) % 4]) +
          Math.sqrt(ref.xpoints[(i + 1) % 4] - p.xpoints[(i + 2) % 4]) +
          Math.sqrt(ref.ypoints[(i + 1) % 4] - p.ypoints[(i + 2) % 4]) +
          Math.sqrt(ref.xpoints[(i + 2) % 4] - p.xpoints[(i + 1) % 4]) +
          Math.sqrt(ref.ypoints[(i + 2) % 4] - p.ypoints[(i + 1) % 4]) +
          Math.sqrt(ref.xpoints[(i + 3) % 4] - p.xpoints[(i + 0) % 4]) +
          Math.sqrt(ref.ypoints[(i + 3) % 4] - p.ypoints[(i + 0) % 4]));

      if (d < minD) {
        minD = d;
        bestStart = start;
        bestOrder = 1;
      }

    }

    Polygon[] r = new Polygon[4];

    if (bestOrder == 0) {
      for (int j = 0; j < 4; j++) {
        //r[j] = p[(j + bestStart) % 4];
      }
    } else {
      for (int j = 0; j < 4; j++) {
        //r[j] = p[(bestStart + 4 - j - 1) % 4];
      }
    }
    //TODO score

    return r;

  }

  public static Polygon[] getPoly() {
    Polygon[] poly = new Polygon[4];
    int [] xData = new int[4];
    int [] yData = new int[4];

    float score = 0;
    return null;
  }

  public static Polygon[] mergeQuadrangle(float data, int size, float threshold) {

    Polygon[] polys = {};
    for (int i = 0; i < size; i++) {
      float p = data + (i * 9);

      //TODO cInt

      //if (polys.length){

    }

    return polys;
  }

  public static void main(String[] args) {
    getPoly();
  }
}
