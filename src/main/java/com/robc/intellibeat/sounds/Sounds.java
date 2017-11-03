package com.robc.intellibeat.sounds;

import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.Function;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class Sounds {
  public final ArrayList<Sequence> sequences;
  public final ArrayList<Sequence> performances;
  public final ArrayList<Sequence> drums;

  public final Sound marioSong;
  public final Sound zeldaSong;

  public static Sounds create(final boolean actionSoundsEnabled, final boolean backgroundMusicEnabled) {
    System.out.println("sounds create");
    return new Sounds(new Function<Config, Sound>() {
      @Override
      public Sound fun(Config config) {
        boolean enabled = (config.isBackgroundMusic && backgroundMusicEnabled) || (!config.isBackgroundMusic && actionSoundsEnabled);
        return enabled ?
            new Sound(loadBytes(config.filePath), config.filePath) :
            new SilentSound(loadBytes(config.filePath), config.filePath, SilentSound.Listener.none);
      }
    });
  }

  public static Sounds createSilent(final SilentSound.Listener listener) {
    return new Sounds(new Function<Config, Sound>() {
      @Override
      public Sound fun(Config config) {
        return new SilentSound(loadBytes(config.filePath), config.filePath, listener);
      }
    });
  }

  private Sounds(Function<Config, Sound> load) {
    sequences = new ArrayList<>();
    performances = new ArrayList<>();
    drums = new ArrayList<>();
    try {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:/Users/finshared/rnn/outputs/*.mid");
      Files.walkFileTree(Paths.get("/Users/finshared/rnn/outputs/"), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (matcher.matches(file)) {
            File f = new File(file.toAbsolutePath().toString());
            try {
              sequences.add(MidiSystem.getSequence(f));
            } catch (Exception e) {
              System.out.println(e);
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (Exception e) {

    }
    try {

      PathMatcher matcherPerf = FileSystems.getDefault().getPathMatcher("glob:/Users/finshared/rnn/performances/*.mid");
      Files.walkFileTree(Paths.get("/Users/finshared/rnn/performances/"), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (matcherPerf.matches(file)) {
            File f = new File(file.toAbsolutePath().toString());
            try {
              performances.add(MidiSystem.getSequence(f));
            } catch (Exception e) {
              System.out.println(e);
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (Exception e) {

    }
    try {

      PathMatcher matcherPerf = FileSystems.getDefault().getPathMatcher("glob:/Users/finshared/rnn/drums/*.mid");
      Files.walkFileTree(Paths.get("/Users/finshared/rnn/drums/"), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (matcherPerf.matches(file)) {
            File f = new File(file.toAbsolutePath().toString());
            try {
              drums.add(MidiSystem.getSequence(f));
            } catch (Exception e) {
              System.out.println(e);
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (Exception e) {

    }
    marioSong = load.fun(new Config("/mario_08.au", true));
    zeldaSong = load.fun(new Config("/zelda_04.au", true));
  }

  private static byte[] loadBytes(String fileName) {
    try {
      InputStream inputStream = Sounds.class.getResourceAsStream(fileName);
      if (inputStream == null) throw new RuntimeException("Cannot find " + fileName);
      return StreamUtil.loadFromStream(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class Config {
    public final String filePath;
    public final boolean isBackgroundMusic;

    public Config(String filePath) {
      this(filePath, false);
    }

    public Config(String filePath, boolean isBackgroundMusic) {
      this.filePath = filePath;
      this.isBackgroundMusic = isBackgroundMusic;
    }
  }
}

