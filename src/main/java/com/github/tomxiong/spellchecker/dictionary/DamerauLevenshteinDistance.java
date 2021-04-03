package com.github.tomxiong.spellchecker.dictionary;

import io.github.mightguy.spellcheck.symspell.api.StringDistance;

public class DamerauLevenshteinDistance implements StringDistance  {

  @Override
  public double getDistance(String source, String target) {

    if (0 == source.length()) {
      return target.length();
    }

    if (0 == target.length()) {
      return source.length();
    }

    int[][] matrix = initMatrixXYTop(source.length(), target.length());

    char charX = ' ';
    for (int countX = 0; countX < source.length(); countX++) {
      char charXPrev = charX;
      charX = source.charAt(countX);
      char charY = ' ';
      for (int countY = 0; countY < target.length(); countY++) {
        char charYPrev = charY;
        charY = target.charAt(countY);
        int cost;
        if (charX == charY) {
          cost = 0;
        } else {
          cost = 1;
        }

        matrix[countX + 1][countY + 1] = Math
            .min(matrix[countX][countY + 1] + 1, Math.min(matrix[countX + 1][countY] + 1, matrix[countX][countY] + cost));

        if (countX > 0 && countY > 0 && charX == charYPrev && charXPrev == charY) {
          matrix[countX + 1][countY + 1] = Math.min(matrix[countX][countY], matrix[countX - 1][countY - 1] + cost);
        }
      }
    }
    return matrix[source.length()][target.length()];
  }

  @Override
  public double getDistance(String source, String target, double maxEditDistance) {
    double distance = getDistance(source, target);
    if (distance > maxEditDistance) {
      return -1;
    }
    return distance;

  }

  private int[][] initMatrixXYTop(int lenOfSource, int lenOfTarget) {
    int[][] matrix = new int[ lenOfSource + 1][lenOfTarget + 1];

    for (int count = 0; count < lenOfSource + 1; count++) {
      matrix[count][0] = count;
    }

    for (int count = 0; count < lenOfTarget + 1; count++) {
      matrix[0][count] = count;
    }
    return matrix;
  }
}
