package com.robc.intellibeat;


import com.robc.intellibeat.listeners.*;
import com.robc.intellibeat.sounds.Sound;
import com.robc.intellibeat.sounds.Sounds;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class ActionListeningSoundPlayer implements
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


  public ActionListeningSoundPlayer(Sounds sounds, Listener listener) {
    this.sounds = sounds;
    this.listener = listener;
    this.soundsByAction = editorSounds(sounds);
    this.soundsByRefactoring = refactoringSounds(sounds);
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
    sounds.marioSong.playInBackground();
    return this;
  }

  public void stop(boolean isIdeShutdown) {
    if (stopped) return;
    stopped = true;
    sounds.marioSong.stop();
    sounds.zeldaSong.stop();
    if (isIdeShutdown) {
//      sounds.gameover.play();
    }
  }

  public void stopAndWait() {
    if (stopped) return;
    stopped = true;
    sounds.marioSong.stop();
    sounds.zeldaSong.stop();
//    sounds.gameover.playAndWait();
  }

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
    try {
      if (!sequencer.isRunning()) {
        int index = this.count % this.sounds.sequences.size();
        System.out.println("playing");
        Sequence s = this.sounds.sequences.get(index);
        sequencer.setTickPosition(0);
        sequencer.setSequence(s);
        sequencer.start();
        this.count++;
        new Thread(() -> {
          try {
            Thread.sleep(s.getMicrosecondLength() / 1000);
            sequencer.stop();
          } catch (Exception e) {
            System.out.println("ERROR!");
            System.out.println(e);
          }
        }).start();
      } else {
        System.out.println("Not playing because is running");
      }
    } catch (Exception e) {
      System.out.println("ERROR!");
      System.out.println(e);
    }
//    try {
//      if (!sequencer.isRunning()) {
//        int index = this.count % this.sounds.sequences.size();
//        Sequence s = this.sounds.sequences.get(index);
//        long tickLen = s.getMicrosecondLength() / s.getTickLength();
//        long micro = tickLen * tickNum;
//        if (micro >= s.getMicrosecondLength()) {
//          this.tickNum = 0;
//          this.count++;
//          index = this.count % this.sounds.sequences.size();
//          s = this.sounds.sequences.get(index);
//          sequencer.setTickPosition(0);
//        }
//        sequencer.setSequence(s);
//        System.out.println(s.getMicrosecondLength());
//        System.out.println(micro);
//        sequencer.start();
//        this.tickNum++;
//        System.out.println("playing");
//        new Thread(() -> {
//          try {
//            Thread.sleep(tickLen);
//            sequencer.stop();
//          } catch (Exception e) {
//
//          }
//        }).start();
//      } else {
//        System.out.println("Not playing because is running");
//      }
//    } catch (Exception e) {
//      System.out.println(e);
//    }
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
    System.out.println(actionId);
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
      sounds.coin.play();
      listener.unmappedRefactoring(refactoringId);
    }
  }

  @Override
  public void compilationSucceeded() {
    sounds.oneUp.play();
    if (compilationFailed) {
      compilationFailed = false;
      sounds.marioSong.playInBackground();
      sounds.zeldaSong.stop();
    }
  }

  @Override
  public void compilationFailed() {
    sounds.oneDown.play();
    if (!compilationFailed) {
      compilationFailed = true;
      sounds.zeldaSong.playInBackground();
      sounds.marioSong.stop();
    }
  }

  @Override
  public void onUnitTestSucceeded() {
    sounds.oneUp.play();
  }

  @Override
  public void onUnitTestFailed() {
    sounds.oneDown.play();
  }

  @Override
  public void onVcsCommit() {
    sounds.powerupAppears.play();
  }

  @Override
  public void onVcsUpdate() {
    sounds.powerup.play();
  }

  @Override
  public void onVcsPush() {
    sounds.powerup.play();
  }

  @Override
  public void onVcsPushFailed() {
    sounds.oneDown.play();
  }

  private static Map<String, Sound> refactoringSounds(Sounds sounds) {
    Map<String, Sound> result = new HashMap<String, Sound>();
    result.put("refactoring.rename", sounds.coin);
    result.put("refactoring.extractVariable", sounds.coin);
    result.put("refactoring.extract.method", sounds.coin);
    result.put("refactoring.inline.local.variable", sounds.coin);
    result.put("refactoring.safeDelete", sounds.coin);
    result.put("refactoring.introduceParameter", sounds.coin);
    return result;
  }

  private static Map<String, Sound> editorSounds(Sounds sounds) {
    Map<String, Sound> result = new HashMap<String, Sound>();

    result.put("EditorUp", sounds.kick);
    result.put("EditorDown", sounds.kick);
    result.put("EditorUpWithSelection", sounds.kick);
    result.put("EditorDownWithSelection", sounds.kick);
    result.put("EditorPreviousWord", sounds.kick);
    result.put("EditorNextWord", sounds.kick);
    result.put("EditorPreviousWordWithSelection", sounds.kick);
    result.put("EditorNextWordWithSelection", sounds.kick);
    result.put("EditorSelectWord", sounds.kick);
    result.put("EditorUnSelectWord", sounds.kick);
    result.put("$SelectAll", sounds.kick);
    result.put("EditorLineStart", sounds.jumpSmall);
    result.put("EditorLineEnd", sounds.jumpSmall);
    result.put("EditorLineStartWithSelection", sounds.jumpSmall);
    result.put("EditorLineEndWithSelection", sounds.jumpSmall);
    result.put("EditorPageUp", sounds.jumpSuper);
    result.put("EditorPageDown", sounds.jumpSuper);
    result.put("GotoPreviousError", sounds.jumpSuper);
    result.put("GotoNextError", sounds.jumpSuper);
    result.put("FindNext", sounds.jumpSuper);
    result.put("FindPrevious", sounds.jumpSuper);
    result.put("MethodUp", sounds.jumpSuper);
    result.put("MethodDown", sounds.jumpSuper);
    result.put("Back", sounds.jumpSuper);
    result.put("Forward", sounds.jumpSuper);
    result.put("GotoSuperMethod", sounds.jumpSuper);
    result.put("GotoDeclaration", sounds.jumpSuper);
    result.put("GotoImplementation", sounds.jumpSuper);
    result.put("EditSource", sounds.jumpSuper); // this is F4 navigate action

    result.put("EditorPaste", sounds.fireball);
    result.put("ReformatCode", sounds.fireball);
    result.put("EditorToggleCase", sounds.fireball);
    result.put("ExpandLiveTemplateByTab", sounds.fireball);
    result.put("EditorCompleteStatement", sounds.fireball);
    result.put("EditorChooseLookupItem", sounds.fireball);
    result.put("EditorChooseLookupItemReplace", sounds.fireball);
    result.put("HippieCompletion", sounds.fireball);
    result.put("HippieBackwardCompletion", sounds.fireball);
    result.put("MoveStatementUp", sounds.fireball);
    result.put("MoveStatementDown", sounds.fireball);
    result.put("EditorStartNewLineBefore", sounds.fireball);
    result.put("EditorStartNewLine", sounds.fireball);
    result.put("EditorDuplicate", sounds.fireball);
    result.put("EditorBackSpace", sounds.breakblock);
    result.put("EditorJoinLines", sounds.breakblock);
    result.put("EditorDelete", sounds.breakblock);
    result.put("EditorDeleteLine", sounds.breakblock);
    result.put("EditorDeleteToWordStart", sounds.breakblock);
    result.put("EditorDeleteToWordEnd", sounds.breakblock);
    result.put("CommentByLineComment", sounds.breakblock);
    result.put("CommentByBlockComment", sounds.breakblock);
    result.put("ToggleBookmark", sounds.stomp);
    result.put("ToggleBookmarkWithMnemonic", sounds.stomp);
    result.put("ToggleLineBreakpoint", sounds.stomp);
    result.put("HighlightUsagesInFile", sounds.stomp);

    result.put("NextTab", sounds.jumpSuper);
    result.put("PreviousTab", sounds.jumpSuper);
    result.put("CloseEditor", sounds.fireworks);
    result.put("CloseAllEditorsButActive", sounds.fireworks);
    result.put("$Undo", sounds.fireworks);
    result.put("$Redo", sounds.fireworks);
    result.put("ExpandAllRegions", sounds.stomp);
    result.put("CollapseAllRegions", sounds.stomp);
    result.put("ExpandRegion", sounds.stomp);
    result.put("CollapseRegion", sounds.stomp);
    result.put("CollapseSelection", sounds.stomp);
    result.put("PasteMultiple", sounds.stomp);
    result.put("FileStructurePopup", sounds.stomp);
    result.put("ShowBookmarks", sounds.stomp);
    result.put("ViewBreakpoints", sounds.stomp);
    result.put("QuickJavaDoc", sounds.stomp);
    result.put("ParameterInfo", sounds.stomp);
    result.put("ShowIntentionActions", sounds.stomp);
    result.put("EditorToggleColumnMode", sounds.stomp);
    result.put("SurroundWith", sounds.stomp);
    result.put("InsertLiveTemplate", sounds.stomp);
    result.put("SurroundWithLiveTemplate", sounds.stomp);
    result.put("NewElement", sounds.stomp);
    result.put("Generate", sounds.stomp);
    result.put("OverrideMethods", sounds.stomp);
    result.put("ImplementMethods", sounds.stomp);

    result.put("ChangeSignature", sounds.stomp);
    result.put("ExtractMethod", sounds.stomp);
    result.put("Inline", sounds.stomp);
    result.put("Move", sounds.stomp);

    result.put("Find", sounds.stomp);
    result.put("FindInPath", sounds.stomp);
    result.put("Replace", sounds.stomp);
    result.put("ReplaceInPath", sounds.stomp);

    result.put("ChangesView.Diff", sounds.stomp);
    result.put("CompareClipboardWithSelection", sounds.stomp);

    result.put("Switcher", sounds.stomp);
    result.put("RecentFiles", sounds.stomp);
    result.put("GotoClass", sounds.stomp);
    result.put("GotoFile", sounds.stomp);
    result.put("GotoSymbol", sounds.stomp);
    result.put("SearchEverywhere", sounds.stomp);
    result.put("GotoLine", sounds.stomp);
    result.put("ShowUsages", sounds.stomp);
    result.put("FindUsages", sounds.stomp);
    result.put("ShowNavBar", sounds.stomp);
    result.put("RunInspection", sounds.stomp);

    result.put("SelectIn", sounds.stomp);
    result.put("QuickChangeScheme", sounds.stomp);
    result.put("ActivateProjectToolWindow", sounds.stomp);
    result.put("ActivateStructureToolWindow", sounds.stomp);
    result.put("ActivateFindToolWindow", sounds.stomp);
    result.put("ActivateChangesToolWindow", sounds.stomp);
    result.put("ActivateRunToolWindow", sounds.stomp);
    result.put("ActivateDebugToolWindow", sounds.stomp);
    result.put("ActivateMessagesToolWindow", sounds.stomp);
    result.put("ActivateFavoritesToolWindow", sounds.stomp);
    result.put("AddToFavoritesPopup", sounds.stomp);
    result.put("TypeHierarchy", sounds.stomp);
    result.put("HideActiveWindow", sounds.stomp);
    result.put("Vcs.QuickListPopupAction", sounds.stomp);
    result.put("Vcs.ShowMessageHistory", sounds.stomp);

    result.put("ChooseRunConfiguration", sounds.stomp);
    result.put("ChooseDebugConfiguration", sounds.stomp);

    return result;
  }


  public interface Listener {
    void unmappedAction(String actionId);

    void unmappedRefactoring(String refactoringId);
  }
}
