package com.robc.intellibeat;


import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.robc.intellibeat.listeners.*;
import com.robc.intellibeat.sounds.Sound;
import com.robc.intellibeat.sounds.Sounds;
import com.sun.nio.sctp.MessageInfo;
import javafx.util.Pair;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class ActionListeningSoundPlayer extends TypedHandlerDelegate implements
    Compilation.Listener, Refactoring.Listener, UnitTests.Listener, VcsActions.Listener, AllActions.Listener {

  private final Sounds sounds;
  private final Listener listener;
  private final Map<String, Sound> soundsByAction;
  private final Map<String, Sound> soundsByRefactoring;
  private boolean compilationFailed;
  private boolean stopped;
  private Sequencer sequencer;
  private int count = 0;
  private Receiver receiver = null;
  private int tickNum = 0;
  private ArrayList<ArrayList<MidiEvent>> events = new ArrayList<>();


  public ActionListeningSoundPlayer(Sounds sounds, Listener listener) {
    this.sounds = sounds;
    this.listener = listener;
    this.soundsByAction = editorSounds(sounds);
    this.soundsByRefactoring = refactoringSounds(sounds);
    for (Sequence s : sounds.sequences) {
      Track[] tracks = s.getTracks();
      if (tracks != null) {
        for (int i = 0; i < tracks.length; i++) {
          Track track = tracks[i];
          long curTick = track.get(0).getTick();
          ArrayList<MidiEvent> curList = new ArrayList<>();
          curList.add(track.get(0));
          for (int j = 0; j < track.size(); j++) {
            MidiEvent event = track.get(j);
            if (event.getTick() == curTick) {
              curList.add(event);
            } else {
              events.add(curList);
              curList = new ArrayList<>();
              curList.add(event);
              curTick = event.getTick();
            }
          }
        }
      }
    }
    try {
      this.sequencer = MidiSystem.getSequencer(false);
      if (this.sequencer == null) {
        // Error -- sequencer device is not supported.
        // Inform user and return...
      } else {
        // Acquire resources and make operational.
        sequencer.open();
        sequencer.setLoopCount(0);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public ActionListeningSoundPlayer init() {
    sounds.marioSong.playInBackground(drums);
    return this;
  }

  @Override
  public Result charTyped(char c, Project project, Editor editor, PsiFile file) {
    onAction(c + "");
    return Result.CONTINUE;
  }


  public void stop(boolean isIdeShutdown) {
    if (stopped) return;
    stopped = true;
    sounds.marioSong.stop();
    sounds.zeldaSong.stop();
    if (isIdeShutdown) {
    }
  }

  public void stopAndWait() {
    if (stopped) return;
    stopped = true;
    sounds.marioSong.stop();
    sounds.zeldaSong.stop();
  }


  boolean drums = false;
  public void playDrums(boolean play) {
    sounds.marioSong.playInBackground(drums);
    drums = play;
  }

  boolean isBlocked = false;
  int curIndex = 0;
  @Override
  public void onAction(String actionId) {
//		ExecuteShellComand obj = new ExecuteShellComand();
//
//		String domainName = "google.com";
//
//		//in mac oxs
//		String command = "ping -c 3 " + domainName;
//
//		//in windows
//		//String command = "ping -n 3 " + domainName;
//
//		String output = obj.executeCommand(command);
//
//		System.out.println(output);
    MidiDevice device = null;
    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
    for (int i = 0; i < infos.length; i++) {
      try {
        if (receiver == null) {
          device = MidiSystem.getMidiDevice(infos[i]);
          String name = device.getDeviceInfo().toString();
          Pattern p = Pattern.compile("FluidSynth virtual port");
          if (p.matcher(name).find()) {
            if (!device.isOpen()) {
              device.open();
              System.out.println("Setting Receiver to : " + name);
              receiver = device.getReceiver();
              sequencer.getTransmitter().setReceiver(device.getReceiver());
            }
          }
        }
      } catch (Exception e) {
        System.out.println("ERROR!");
        System.out.println(e);
      }
    }
//    int index = this.count % this.sounds.sequences.size();
//    Sequence s = this.sounds.sequences.get(index);
//    for (Track t : s.getTracks()) {
//      for (int it = 0; it < t.size(); it++) {
//        receiver.send(t.get(it).getMessage(), -1);
//      }
//    }
//    count++;
//    try {
//      if (!sequencer.isRunning()) {
//
//        int index = this.count % this.sounds.sequences.size();
//        Sequence s = this.sounds.sequences.get(index);
//        sequencer.setTickPosition(0);
//        sequencer.setSequence(s);
//        sequencer.start();
//        System.out.println("play " + s.getMicrosecondLength() / 1000 + " " + this.count);
//        this.count++;
//        new Thread(() -> {
//          try {
//            Thread.sleep(s.getMicrosecondLength() / 1000);
//            sequencer.stop();
//          } catch (Exception e) {
//            System.out.println("ERROR!");
//            System.out.println(e);
//          }
//        }).start();
//      } else {
//        System.out.println("Not playing because is running");
//      }
//    } catch (Exception e) {
//      System.out.println("ERROR!");
//      System.out.println(e);
//    }
    try {
      if (!isBlocked) {
        int index = this.count % this.sounds.sequences.size();
        Sequence s = this.sounds.sequences.get(index);
        long tickLen = s.getMicrosecondLength() / s.getTickLength();
        isBlocked = true;
        for (MidiEvent e: events.get(curIndex)) {
          receiver.send(e.getMessage(), -1);
        }
        curIndex++;
        new Thread(() -> {
          try {
            Thread.sleep(tickLen / 1000);
            isBlocked = false;
          } catch (Exception e) {
            System.out.println(e);
          }
        }).start();
      } else {
        System.out.println("Not playing because is running");
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    try {
      Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "pwd"});
      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = input.readLine()) != null) {
//        System.out.println(line);
      }
      input.close();
      p.waitFor();
    } catch (Exception e) {
      System.out.println(e.toString());
    }


    Sound sound = soundsByAction.get(actionId);
    if (sound != null) {
//      sound.play();
    } else {
      listener.unmappedAction(actionId);
    }
  }

  @Override
  public void onRefactoring(String refactoringId) {
    Sound sound = soundsByRefactoring.get(refactoringId);
    if (sound != null) {
      sound.play();
    } else {
      listener.unmappedRefactoring(refactoringId);
    }
  }

  @Override
  public void compilationSucceeded() {
    if (compilationFailed) {
      compilationFailed = false;
      sounds.zeldaSong.stop();
    }
  }

  @Override
  public void compilationFailed() {
    if (!compilationFailed) {
      compilationFailed = true;
      sounds.marioSong.stop();
    }
  }

  @Override
  public void onUnitTestSucceeded() {
  }

  @Override
  public void onUnitTestFailed() {
  }

  @Override
  public void onVcsCommit() {
  }

  @Override
  public void onVcsUpdate() {
  }

  @Override
  public void onVcsPush() {
  }

  @Override
  public void onVcsPushFailed() {
  }

  private static Map<String, Sound> refactoringSounds(Sounds sounds) {
    Map<String, Sound> result = new HashMap<String, Sound>();
    return result;
  }

  private static Map<String, Sound> editorSounds(Sounds sounds) {
    Map<String, Sound> result = new HashMap<String, Sound>();

    return result;
  }


  public interface Listener {
    void unmappedAction(String actionId);

    void unmappedRefactoring(String refactoringId);
  }
}
