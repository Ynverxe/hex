package com.github.ynverxe.hexserver.launcher.file;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@ConfigSerializable
class RemoteResourceDefinition {
  private @MonotonicNonNull String url;
  private @Nullable List<Signature> signatures;

  public @MonotonicNonNull String url() {
    return url;
  }

  public void evaluate() {
    Objects.requireNonNull(url, "url is null");
    if (signatures != null)
      for (Signature signature : signatures) {
        signature.evaluate();
      }
  }

  public @Nullable List<Signature> signatures() {
    return signatures;
  }

  @ConfigSerializable
  static class Signature {
    private @MonotonicNonNull String value;
    private @MonotonicNonNull String algorithm;

    public String value() {
      return value;
    }

    public String algorithm() {
      return algorithm;
    }

    public boolean isRemoteValue() {
      // check if value is an url
      try {
        URI.create(this.value).toURL();
        return true;
      } catch (MalformedURLException | IllegalArgumentException e) {
        return false;
      }
    }

    public void evaluate() {
      Objects.requireNonNull(this.value, "signature value is null");
      Objects.requireNonNull(this.algorithm, "signature algorithm is null");
    }
  }
}