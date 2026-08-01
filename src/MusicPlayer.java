import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Phat nhac nen, lap lai lien tuc. Luu y: javax.sound.sampled (co san trong JDK,
 * khong can thu vien ngoai) chi doc truc tiep duoc file .wav. Neu ban co file .mp3,
 * can doi sang .wav truoc, hoac dung them thu vien ngoai (vd JLayer) de doc mp3.
 */
public class MusicPlayer {

    private Clip clip;

    /** Phat file wav va lap vo han. Neu khong doc duoc file, in loi ra console va bo qua (khong crash app). */
    public void playLoop(String path) {
        try {
            File file = new File(path);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[MusicPlayer] Khong phat duoc nhac (" + path + "): " + e.getMessage());
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}