package com.github.ynverxe.hexserver.launcher.file;

import com.github.ynverxe.hexserver.launcher.exception.InvalidHashException;
import com.github.ynverxe.hexserver.launcher.util.ConnectionMaker;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RemoteResourceDownloader {

  private RemoteResourceDownloader() {
  }

  static void content(@NotNull RemoteResourceDefinition definition, Listener listener) throws InvalidHashException, IOException, NoSuchAlgorithmException {
    definition.evaluate();

    List<RemoteResourceDefinition.Signature> signatures = definition.signatures();
    InputStream stream = connectionStream(definition.url());

    if (signatures != null) {
      consumeAndValidateContent(signatures, stream, listener);
    } else {
      consumeContent(stream, listener);
    }
  }

  public static @NotNull InputStream connectionStream(@NotNull URL url) throws IOException {
    return ConnectionMaker.make(url).getInputStream();
  }
  
  private static @NotNull InputStream connectionStream(@NotNull String url) throws IOException {
    return connectionStream(URI.create(url).toURL());
  }
  
  private static void consumeAndValidateContent(List<RemoteResourceDefinition.Signature> signatures, InputStream stream, Listener listener)
      throws NoSuchAlgorithmException, IOException, InvalidHashException {
    try (listener) {
      Map<MessageDigest, String> hashValues = new HashMap<>();

      // create digests and compute expected hash values
      for (RemoteResourceDefinition.Signature signature : signatures) {
        MessageDigest digest = MessageDigest.getInstance(signature.algorithm());

        String expectedHashValue = resolveHashValue(signature);
        hashValues.put(digest, expectedHashValue);
      }

      // update digests
      int next;
      while ((next = stream.read()) != -1) {
        byte read = (byte) next;
        for (MessageDigest messageDigest : hashValues.keySet()) {
          messageDigest.update((byte) next);
        }
        listener.onRead(read);
      }

      for (Map.Entry<MessageDigest, String> entry : hashValues.entrySet()) {
        MessageDigest digest = entry.getKey();
        String expectedHash = entry.getValue();

        byte[] hashBytes = digest.digest();

        StringBuilder checksumBuilder = new StringBuilder();
        for (byte hashByte : hashBytes) {
          checksumBuilder.append(String.format("%02x", hashByte));
        }
        String fileHash = checksumBuilder.toString();

        if (!expectedHash.equalsIgnoreCase(fileHash)) {
          listener.onInvalidChecksum();
          throw new InvalidHashException("Checksums aren't the same");
        }
      }
    }
  }

  private static String resolveHashValue(RemoteResourceDefinition.Signature signature) throws IOException {
    String value = signature.value();
    if (signature.isRemoteValue()) {
      try (InputStream checksumInputStream = connectionStream(value)) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(checksumInputStream));
        value = reader.readLine();
        reader.close();
      }
    }

    return value;
  }

  static void consumeContent(InputStream stream, Listener listener) throws IOException {
    try (listener) {
      // update digests
      int next;
      while ((next = stream.read()) != -1) {
        byte read = (byte) next;
        listener.onRead(read);
      }
    }
  }

  interface Listener extends Closeable {
    void onRead(byte read) throws IOException;

    void onInvalidChecksum() throws IOException;
  }
}