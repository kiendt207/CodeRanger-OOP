package coderanger.audio;

import java.awt.GraphicsEnvironment;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * Pure-Java 8-bit chiptune sound synthesizer for Sanbaka's combat actions.
 * Pre-renders PCM buffers at startup and plays them asynchronously off the Swing EDT.
 */
public class SoundEngine {
    private static final float SAMPLE_RATE = 22050f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final int STRIKE_PITCH_STEPS = 9;

    private final Map<SoundEffect, byte[]> soundBank = new EnumMap<>(SoundEffect.class);
    private final byte[][] strikePitchBank = new byte[STRIKE_PITCH_STEPS][];
    private final ThreadPoolExecutor audioPool;
    private volatile boolean muted;
    private volatile boolean audioAvailable;

    public SoundEngine() {
        this(GraphicsEnvironment.isHeadless());
    }

    public SoundEngine(boolean startMuted) {
        this.muted = startMuted;
        this.audioAvailable = probeAudioDevice();
        synthesizeAll();

        this.audioPool = new ThreadPoolExecutor(
                1,
                3,
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(6),
                runnable -> {
                    Thread thread = new Thread(runnable, "sanbaka-8bit-sfx");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    public void play(SoundEffect effect) {
        if (effect == null) {
            return;
        }
        if (effect == SoundEffect.STRIKE) {
            playStrike(0);
            return;
        }
        playBuffer(soundBank.get(effect));
    }

    public void playStrike(int streak) {
        int step = Math.max(0, Math.min(STRIKE_PITCH_STEPS - 1, streak / 4));
        playBuffer(strikePitchBank[step]);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean toggleMute() {
        this.muted = !this.muted;
        return this.muted;
    }

    public byte[] getPcmSample(SoundEffect effect) {
        byte[] data = soundBank.get(effect);
        return data == null ? new byte[0] : data.clone();
    }

    private void playBuffer(byte[] pcm) {
        if (muted || !audioAvailable || pcm == null || pcm.length == 0) {
            return;
        }
        audioPool.execute(() -> {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(FORMAT, Math.max(2048, pcm.length));
                line.start();
                line.write(pcm, 0, pcm.length);
                line.drain();
            } catch (Exception ignored) {
                audioAvailable = false;
            }
        });
    }

    private static boolean probeAudioDevice() {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    private void synthesizeAll() {
        // Ascending combo-scaled martial slash / code beam strikes for Sanbaka!
        double[] comboRoots = {520, 560, 600, 650, 700, 760, 820, 890, 980};
        for (int i = 0; i < STRIKE_PITCH_STEPS; i++) {
            double root = comboRoots[i];
            strikePitchBank[i] = concat(
                    sweep(root * 1.65, root * 0.72, 0.048, 0.34, Wave.PULSE_25, 0.35),
                    sweep(root * 1.15, root * 1.45, 0.022, 0.22, Wave.SQUARE, 0.0)
            );
        }
        soundBank.put(SoundEffect.STRIKE, strikePitchBank[0]);

        // Miss: dull low off-key clank
        soundBank.put(SoundEffect.MISS,
                sweep(195, 115, 0.085, 0.30, Wave.SQUARE, 0.20));

        // Enemy Finisher: crisp pixel slash explosion + bright 2-note pop
        soundBank.put(SoundEffect.ENEMY_KILL, concat(
                sweep(420, 140, 0.055, 0.36, Wave.SQUARE, 0.55),
                tone(660, 0.040, 0.28, Wave.PULSE_25),
                tone(990, 0.055, 0.28, Wave.PULSE_25)
        ));

        // Quick-Kill Shockwave Ultimate: charge-up + thunderous wave blast!
        soundBank.put(SoundEffect.SHOCKWAVE_ULT, concat(
                sweep(260, 1180, 0.075, 0.36, Wave.PULSE_25, 0.10),
                sweep(920, 95, 0.230, 0.42, Wave.SQUARE, 0.55)
        ));

        // Bonus Iced Tea Buff: classic 4-note RPG powerup arpeggio (C5-E5-G5-C6)
        soundBank.put(SoundEffect.BONUS_BUFF, concat(
                tone(523.25, 0.055, 0.30, Wave.PULSE_25),
                tone(659.25, 0.055, 0.30, Wave.PULSE_25),
                tone(783.99, 0.055, 0.30, Wave.PULSE_25),
                tone(1046.50, 0.110, 0.34, Wave.SQUARE)
        ));

        // Shield Break: crystalline glass/barrier shatter
        soundBank.put(SoundEffect.SHIELD_BREAK, concat(
                sweep(1250, 340, 0.130, 0.36, Wave.PULSE_25, 0.65),
                sweep(480, 210, 0.070, 0.26, Wave.TRIANGLE, 0.35)
        ));

        // Player Hurt: heavy low hit punch
        soundBank.put(SoundEffect.PLAYER_HURT, concat(
                sweep(220, 58, 0.180, 0.44, Wave.SQUARE, 0.55),
                sweep(110, 48, 0.090, 0.34, Wave.TRIANGLE, 0.25)
        ));

        // Boss Warning (Semester 8): 3 dramatic arcade siren blasts
        byte[] siren = sweep(440, 622, 0.120, 0.34, Wave.SQUARE, 0.05);
        byte[] gap = silence(0.035);
        soundBank.put(SoundEffect.BOSS_WARNING, concat(siren, gap, siren, gap,
                sweep(440, 880, 0.180, 0.38, Wave.SQUARE, 0.10)));

        // Boss Word Break: critical armor smash!
        soundBank.put(SoundEffect.BOSS_BREAK, concat(
                sweep(180, 72, 0.075, 0.42, Wave.SQUARE, 0.65),
                tone(587.33, 0.050, 0.32, Wave.PULSE_25),
                tone(880.00, 0.085, 0.34, Wave.SQUARE)
        ));

        // Boss Timeout Skill: ominous dark lightning slam
        soundBank.put(SoundEffect.BOSS_SKILL, concat(
                sweep(620, 85, 0.220, 0.42, Wave.SQUARE, 0.60),
                sweep(140, 55, 0.120, 0.35, Wave.TRIANGLE, 0.35)
        ));

        // Victory Fanfare: bright graduation melody!
        soundBank.put(SoundEffect.VICTORY, concat(
                tone(523.25, 0.09, 0.32, Wave.SQUARE),
                tone(659.25, 0.09, 0.32, Wave.SQUARE),
                tone(783.99, 0.09, 0.32, Wave.SQUARE),
                tone(1046.50, 0.16, 0.36, Wave.SQUARE),
                silence(0.03),
                tone(783.99, 0.08, 0.32, Wave.PULSE_25),
                tone(1046.50, 0.26, 0.38, Wave.SQUARE)
        ));

        // Game Over ("TOANG RỒI!"): playful retro descending wah-wah-waaah
        soundBank.put(SoundEffect.GAME_OVER, concat(
                tone(392.00, 0.13, 0.32, Wave.SQUARE),
                tone(369.99, 0.13, 0.32, Wave.SQUARE),
                tone(349.23, 0.13, 0.32, Wave.SQUARE),
                sweep(329.63, 210.0, 0.32, 0.35, Wave.SQUARE, 0.10)
        ));

        // UI Click: crisp 8-bit menu blip
        soundBank.put(SoundEffect.UI_CLICK,
                sweep(740, 1080, 0.032, 0.25, Wave.PULSE_25, 0.0));
    }

    private enum Wave { SQUARE, PULSE_25, TRIANGLE }

    private static byte[] tone(double freq, double seconds, double volume, Wave wave) {
        return sweep(freq, freq, seconds, volume, wave, 0.0);
    }

    private static byte[] sweep(
            double startFreq,
            double endFreq,
            double seconds,
            double volume,
            Wave wave,
            double noiseMix) {
        int samples = Math.max(1, (int) Math.round(seconds * SAMPLE_RATE));
        byte[] out = new byte[samples * 2];
        Random rng = new Random(2026L);
        double phase = 0.0;
        double heldNoise = 0.0;

        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double freq = startFreq + (endFreq - startFreq) * t;
            phase += freq / SAMPLE_RATE;
            double cycle = phase - Math.floor(phase);

            double osc = switch (wave) {
                case SQUARE -> cycle < 0.5 ? 1.0 : -1.0;
                case PULSE_25 -> cycle < 0.25 ? 1.0 : -0.45;
                case TRIANGLE -> cycle < 0.5 ? (cycle * 4.0 - 1.0) : (3.0 - cycle * 4.0);
            };

            if (i % 6 == 0) {
                heldNoise = rng.nextDouble() * 2.0 - 1.0;
            }
            double mixed = osc * (1.0 - noiseMix) + heldNoise * noiseMix;

            // Attack + exponential release envelope so clicks never pop
            double attack = Math.min(1.0, i / (SAMPLE_RATE * 0.003));
            double release = Math.pow(1.0 - t, 0.65);
            double sample = Math.max(-1.0, Math.min(1.0, mixed * volume * attack * release));
            short pcmVal = (short) Math.round(sample * Short.MAX_VALUE);
            out[i * 2] = (byte) (pcmVal & 0xFF);
            out[i * 2 + 1] = (byte) ((pcmVal >> 8) & 0xFF);
        }
        return out;
    }

    private static byte[] silence(double seconds) {
        int samples = Math.max(1, (int) Math.round(seconds * SAMPLE_RATE));
        return new byte[samples * 2];
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) {
            total += p.length;
        }
        byte[] merged = new byte[total];
        int offset = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, merged, offset, p.length);
            offset += p.length;
        }
        return merged;
    }
}
