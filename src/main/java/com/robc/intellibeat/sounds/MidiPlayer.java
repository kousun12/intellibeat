package com.robc.intellibeat.sounds;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.sound.midi.*;

public class MidiPlayer implements MetaEventListener {

  // Midi meta event
  public static final int END_OF_TRACK_MESSAGE = 47;

  private Sequencer sequencer;
  private boolean loop;
  private boolean paused;

  /**
   * Creates a new MidiPlayer object.
   */
  public MidiPlayer() {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequencer.addMetaEventListener(this);
    } catch (MidiUnavailableException ex) {
      sequencer = null;
    }
  }


  /**
   * Loads a sequence from the file system. Returns null if
   * an error occurs.
   */
  public Sequence getSequence(String filename) {
    try {
      return getSequence(new FileInputStream(filename));
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }


  /**
   * Loads a sequence from an input stream. Returns null if
   * an error occurs.
   */
  public Sequence getSequence(InputStream is) {
    try {
      if (!is.markSupported()) {
        is = new BufferedInputStream(is);
      }
      Sequence s = MidiSystem.getSequence(is);
      is.close();
      return s;
    } catch (InvalidMidiDataException ex) {
      ex.printStackTrace();
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }


  private ArrayList<Sequence> sequences = new ArrayList<>();
  private int sequenceIndex = 0;
  private Receiver receiver = null;

  /**
   * Plays a sequence, optionally looping. This method returns
   * immediately. The sequence is not played if it is invalid.
   */
  public void play(ArrayList<Sequence> sequences, boolean loop) {
//    MidiDevice device;
//    MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
//    for (int i = 0; i < infos.length; i++) {
//      try {
//        if (receiver == null) {
//          device = MidiSystem.getMidiDevice(infos[i]);
//          String name = device.getDeviceInfo().toString();
//          Pattern p = Pattern.compile("FluidSynth virtual port");
//          if (p.matcher(name).find()) {
//            if (!device.isOpen()) {
//              device.open();
//              System.out.println("Setting bg Receiver to : " + name);
//              receiver = device.getReceiver();
//              sequencer.getTransmitter().setReceiver(device.getReceiver());
//            }
//          }
//        }
//      } catch (Exception e) {
//        System.out.println("ERROR!");
//        System.out.println(e);
//      }
//    }
    this.sequences = sequences;
    Double d = (Math.random() * sequences.size());
    sequenceIndex = d.intValue();
    if (sequencer != null && sequences.size() > 0 && sequencer.isOpen()) {
      try {
        sequencer.setSequence(sequences.get(sequenceIndex));
        sequencer.start();
        this.loop = loop;
      } catch (InvalidMidiDataException ex) {
        ex.printStackTrace();
      }
    }
  }


  /**
   * This method is called by the sound system when a meta
   * event occurs. In this case, when the end-of-track meta
   * event is received, the sequence is restarted if
   * looping is on.
   */
  public void meta(MetaMessage event) {
    if (event.getType() == END_OF_TRACK_MESSAGE) {
      sequencer.stop();
      if (sequencer != null && sequencer.isOpen() && loop) {
        sequencer.setTickPosition(0);
        sequencer.start();
      } else if (!loop) {
        sequenceIndex = (sequenceIndex + 1) % sequences.size();
        try {
          sequencer.setSequence(sequences.get(sequenceIndex));
          sequencer.setTickPosition(0);
          sequencer.start();
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    }
  }


  /**
   * Stops the sequencer and resets its position to 0.
   */
  public void stop() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
      sequencer.setMicrosecondPosition(0);
    }
  }


  /**
   * Closes the sequencer.
   */
  public void close() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.close();
    }
  }


  /**
   * Gets the sequencer.
   */
  public Sequencer getSequencer() {
    return sequencer;
  }


  /**
   * Sets the paused state. Music may not immediately pause.
   */
  public void setPaused(boolean paused) {
    if (this.paused != paused && sequencer != null && sequencer.isOpen()) {
      this.paused = paused;
      if (paused) {
        sequencer.stop();
      } else {
        sequencer.start();
      }
    }
  }


  /**
   * Returns the paused state.
   */
  public boolean isPaused() {
    return paused;
  }

}
