package gui;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

public class TestMessageBox {

    // private final static int MB_YES_NO = 0x4;
    private final static int MB_YES_NO_CANCEL = 0x3;
    private final static int MB_ICON_EXCLAMATION = 0x30; // yellow triangle

    // this is a "stub" that will be mapped (as it is not already mapped in JNA)
    // https://msdn.microsoft.com/en-us/library/windows/desktop/ms645505(v=vs.85).aspx
    public interface user32 extends Library {
        int MessageBox(int something, String text, String caption, int flags);
    }

    public static void main(String[] args) throws Exception {
        user32 lib = Native.loadLibrary("user32", user32.class, W32APIOptions.UNICODE_OPTIONS);

        // blocking
        int win = lib.MessageBox(0, "MessageBox success ÖÄÖÄ!!!",  "Attention", MB_YES_NO_CANCEL | MB_ICON_EXCLAMATION);
        System.out.println("got window=" + win);  // 7 NO, 6 YES 2 CANCEL
    }
}
