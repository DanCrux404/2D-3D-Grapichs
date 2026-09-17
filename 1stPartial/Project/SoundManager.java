import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Synthesizes and plays mechanical ticking audio effects using Java Sound API.
 * Reuses a single pre-allocated Clip instance to prevent system resource leaks.
 */
public class SoundManager {

    private Clip tickClip;

    public SoundManager() {
        initSyntheticTickSound();
    }

    /**
     * Pre-allocates and opens a single audio clip in memory.
     */
    private void initSyntheticTickSound() {
        try {
            float sampleRate = 44100.0f;
            int durationMs = 12;
            int numSamples = (int) (sampleRate * (durationMs / 1000.0));
            byte[] tickAudioData = new byte[numSamples];

            for (int i = 0; i < numSamples; i++) {
                double angle = i / (sampleRate / 3000.0) * 2.0 * Math.PI;
                double decay = Math.exp(-i / (sampleRate * 0.002));
                tickAudioData[i] = (byte) (Math.sin(angle) * 127.0 * decay);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
            tickClip = AudioSystem.getClip();
            tickClip.open(format, tickAudioData, 0, tickAudioData.length);
        } catch (Exception e) {
            // Audio line failed to open; keep tickClip null to avoid application crash
            tickClip = null;
        }
    }

    /**
     * Rewinds and plays the pre-allocated tick audio sound asynchronously.
     */
    public void playTickSound() {
        if (tickClip == null) {
            return;
        }

        try {
            if (tickClip.isRunning()) {
                tickClip.stop();
            }
            tickClip.setFramePosition(0);
            tickClip.start();
        } catch (Exception ignored) {
            // Silently handle exceptions so the main animation thread never halts
        }
    }
}
