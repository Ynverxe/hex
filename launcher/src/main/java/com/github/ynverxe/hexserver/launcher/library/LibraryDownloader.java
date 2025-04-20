package com.github.ynverxe.hexserver.launcher.library;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.*;

public class LibraryDownloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(LibraryDownloader.class);

  private static final String MINESTOM_SOURCE_FILENAME = "minestom-source.json";

  private final Path serverDirPath;
  private final Path librariesDirPath;

  private final RepositorySystem system;
  private final DefaultRepositorySystemSession session;
  private final LocalRepositoryManager localRepository;
  private final List<URL> classpathUrls = new ArrayList<>();

  public LibraryDownloader(@NotNull Path serverDirPath) throws IOException, DependencyResolutionException {
    this.librariesDirPath = serverDirPath.resolve(".libs");
    this.serverDirPath = serverDirPath;

    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    this.system = locator.getService(RepositorySystem.class);
    this.session = MavenRepositorySystemUtils.newSession();

    this.localRepository = system.newLocalRepositoryManager(session, new LocalRepository(this.librariesDirPath.toString()));
    this.session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
    this.session.setLocalRepositoryManager(this.localRepository);
    this.session.setSystemProperties(System.getProperties());
    this.session.setTransferListener(new AbstractTransferListener() {
      @Override
      public void transferStarted(@NotNull TransferEvent event) {
        LOGGER.info("Downloading {}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
      }

      @Override
      public void transferFailed(TransferEvent event) {
        LOGGER.warn("Cannot download {}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
      }
    });

    InputStream stream = LibraryDownloader.class.getClassLoader().getResourceAsStream("core-all.jar");

    if (stream == null) {
      throw new IllegalStateException("Missing core-all.jar");
    }

    File tempCoreJar = createTempFile("temp-core", ".jar").toFile();
    tempCoreJar.deleteOnExit();

    OutputStream outputStream = newOutputStream(tempCoreJar.toPath());

    byte[] bytes = stream.readAllBytes();
    outputStream.write(bytes);

    stream.close();
    outputStream.close();

    this.classpathUrls.add(tempCoreJar.toURI().toURL()); // :core module overrides MinestomComponentLoggerProvider so the :core jar must be at start of classpath

    downloadDependencies();
  }

  public List<URL> urls() {
    return classpathUrls;
  }

  private void downloadDependencies() throws IOException, DependencyResolutionException {
    Path minestomSourcePath = this.serverDirPath.resolve(MINESTOM_SOURCE_FILENAME);

    BasicConfigurationNode node;
    GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
        .file(minestomSourcePath.toFile())
        .build();

    if (!exists(minestomSourcePath)) {
      InputStream stream = LibraryDownloader.class.getClassLoader().getResourceAsStream(MINESTOM_SOURCE_FILENAME);

      if (stream == null) {
        throw new IllegalStateException("Cannot find minestom-source.json");
      }

      copy(stream, minestomSourcePath);
      node = loader.load();
      stream.close();
    } else {
      node = loader.load();
    }

    MinestomSource minestomSource = node.get(MinestomSource.class);

    List<RemoteRepository> repositories = minestomSource.repositories()
        .stream()
        .map(url -> new RemoteRepository.Builder(url, "default", url).build())
        .toList();

    repositories = system.newResolutionRepositories(session, repositories);

    Artifact artifact = new DefaultArtifact(minestomSource.coordinates());

    CollectRequest collectRequest = new CollectRequest(new Dependency(artifact, JavaScopes.COMPILE), repositories);
    DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

    try {
      for (ArtifactResult artifactResult : system.resolveDependencies(session, dependencyRequest).getArtifactResults()) {
        LocalArtifactResult localArtifactResult = this.localRepository.find(session, artifactResult.getLocalArtifactResult().getRequest());
        if (localArtifactResult.getFile() == null) continue;

        appendToClasspath(localArtifactResult.getFile());
      }
    } catch (DependencyResolutionException e) {
      printDependencyTree(e.getResult().getRoot(), "");
      throw e;
    }
  }

  public static void printDependencyTree(DependencyNode node, String indent) {
    LOGGER.debug("{}{}", indent, formatNode(node));

    for (DependencyNode child : node.getChildren()) {
      printDependencyTree(child, indent + "  ");
    }
  }

  private static String formatNode(DependencyNode node) {
    if (node.getDependency() != null) {
      return node.getArtifact().toString();
    } else {
      return "(no dependency)";
    }
  }

  private void appendToClasspath(@NotNull File file) throws MalformedURLException {
    classpathUrls.add(file.toURI().toURL());
  }
}