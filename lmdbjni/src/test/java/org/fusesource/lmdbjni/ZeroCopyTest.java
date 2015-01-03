package org.fusesource.lmdbjni;

import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class ZeroCopyTest {
  private File dir = new File("/tmp/test");

  private byte[] k1 = new byte[] {3, 3};
  private byte[] v1 = new byte[] {2, 2, 2, 2, 2};

  private byte[] k2 = new byte[] {1, 1, 1, 1, 1};
  private byte[] v2 = new byte[] {4};

  @Test
  public void testZeroCopy() throws Exception {
    deleteDir(dir);
    dir.mkdirs();

    Env env = new Env();
    env.open(dir.getAbsolutePath());
    Database db = env.openDatabase("test");
    db.put(k1, v1);
    db.put(k2, v2);

    Transaction tx = env.createTransaction();
    Cursor cursor = db.openCursor(tx);
    ByteBuffer buffer = ByteBuffer.allocateDirect(32);
    long address = ((DirectBuffer) buffer).address();
    JNI.mdb_cursor_get_address(cursor.pointer(), address, address + 2 * Unsafe.ADDRESS_SIZE, GetOp.FIRST.getValue());

    Val val = new Val(address);
    assertArrayEquals(val.getKey(), k2);
    assertArrayEquals(val.getVal(), v2);

    JNI.mdb_cursor_get_address(cursor.pointer(), address, address + 2 * Unsafe.ADDRESS_SIZE, GetOp.NEXT.getValue());
    val = new Val(address);
    assertArrayEquals(val.getKey(), k1);
    assertArrayEquals(val.getVal(), v1);
  }

  public static class Val {
    long address;

    public Val(long address) {
      this.address = address;
    }

    public byte[] getKey() {
      byte[] key = new byte[getKeySize()];
      long address = Unsafe.UNSAFE.getAddress(this.address + Unsafe.ADDRESS_SIZE * 1);
      Unsafe.getBytes(address, 0, key);
      return key;
    }

    public int getKeySize() {
      return (int) Unsafe.UNSAFE.getLong(address);
    }

    public byte[] getVal() {
      byte[] val = new byte[getValSize()];
      long address = Unsafe.UNSAFE.getAddress(this.address + Unsafe.ADDRESS_SIZE * 3);
      Unsafe.getBytes(address, 0, val);
      return val;
    }

    public int getValSize() {
      return (int) Unsafe.UNSAFE.getLong(address + Unsafe.ADDRESS_SIZE * 2);
    }
  }

  private static void deleteDir(File dir) {
    // delete one level.
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null)
        for (File file : files)
          if (file.isDirectory()) {
            deleteDir(file);
          } else {
            file.delete();
          }
    }
    dir.delete();
  }
}
