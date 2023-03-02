package org.apized.auth.security;

public class CodeGenerator {
  public static String generateCode() {
    return generateCode(128);
  }

  public static String generateCode(int length) {
    char[] sample = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    char[] sequence = new char[length];
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = sample[(int) (Math.random() * sample.length)];
    }

    return new String(sequence);
  }
}
