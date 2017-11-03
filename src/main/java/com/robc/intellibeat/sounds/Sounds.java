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
  public final Sound oneUp;
  public final Sound oneDown;
  public final Sound coin;
  public final Sound bowserfalls;
  public final Sound breakblock;
  public final Sound fireball;
  public final Sound fireworks;
  public final Sound gameover;
  public final Sound jumpSmall;
  public final Sound jumpSuper;
  public final Sound kick;
  public final Sound stomp;
  public final Sound powerupAppears;
  public final Sound powerup;
  public final ArrayList<Sequence> sequences;
  public final ArrayList<Sequence> performances;

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
    try {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:/tmp/outputs/*.mid");
      Files.walkFileTree(Paths.get("/tmp/outputs/"), new SimpleFileVisitor<Path>() {
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

      PathMatcher matcherPerf = FileSystems.getDefault().getPathMatcher("glob:/tmp/performances/*.mid");
      Files.walkFileTree(Paths.get("/tmp/performances/"), new SimpleFileVisitor<Path>() {
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
    oneUp = load.fun(new Config("/smb_1-up.au"));
    oneDown = load.fun(new Config("/smb_pipe.au"));
    coin = load.fun(new Config("/smb_coin.au"));
    bowserfalls = load.fun(new Config("/smb_bowserfalls.au"));
    breakblock = load.fun(new Config("/smb_breakblock.au"));
    fireball = load.fun(new Config("/smb_fireball.au"));
    fireworks = load.fun(new Config("/smb_fireworks.au"));
    gameover = load.fun(new Config("/smb_gameover.au", true));
    jumpSmall = load.fun(new Config("/smb_jump-small.au"));
    jumpSuper = load.fun(new Config("/smb_jump-super.au"));
    kick = load.fun(new Config("/smb_kick.au"));
    stomp = load.fun(new Config("/smb_stomp.au"));
    powerup = load.fun(new Config("/smb_powerup.au"));
    powerupAppears = load.fun(new Config("/smb_powerup_appears.au"));
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

