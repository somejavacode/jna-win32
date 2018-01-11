package process;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public class ProcessTool {

    public static void main(String[] args) throws Exception {

        // https://github.com/java-native-access/jna/blob/master/contrib/platform/test/com/sun/jna/platform/win32/Kernel32Test.java
        // https://stackoverflow.com/questions/7018228/how-do-i-redirect-output-to-a-file-with-createprocess

        // this is essential. without "inheritance" file have 0 bytes.
        WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;

        // https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx
        WinNT.HANDLE hFileOut = Kernel32.INSTANCE.CreateFile("d:\\dev\\github\\playground\\out1",
                // append variant
                WinNT.FILE_APPEND_DATA, WinNT.FILE_SHARE_READ + WinNT.FILE_SHARE_WRITE,
                sa, WinNT.OPEN_ALWAYS, WinNT.FILE_ATTRIBUTE_NORMAL, null);

        WinNT.HANDLE hFileErr = Kernel32.INSTANCE.CreateFile("d:\\dev\\github\\playground\\err1",
                // create variant
                WinNT.FILE_WRITE_DATA + WinNT.FILE_READ_DATA, WinNT.FILE_SHARE_READ,
                sa, WinNT.CREATE_ALWAYS, WinNT.FILE_ATTRIBUTE_NORMAL, null);

        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms686331(v=vs.85).aspx
        WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
        startupInfo.hStdOutput = hFileOut;
        startupInfo.hStdError = hFileErr;
        startupInfo.dwFlags = WinBase.STARTF_USESTDHANDLES; // enable handles

        WinBase.PROCESS_INFORMATION.ByReference processInformation = new WinBase.PROCESS_INFORMATION.ByReference();

        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
        boolean result = Kernel32.INSTANCE.CreateProcess(
                        null,
                        "d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest",

                        // not working at this level. assume "cmd.exe" is interpreting the ">" etc.
//                        "d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest > log.txt 2>&1",

                          // need "cmd" to add "magic"?
                          // working "BUT": kill does only kills cmd.exe. output still "working"!
//                        "c:\\windows\\system32\\cmd.exe /c d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest > log.txt 2>&1",
                        null,
                        null,
                        true,
//                         new WinDef.DWORD(WinBase.CREATE_NEW_CONSOLE), // console pops up
                         new WinDef.DWORD(WinBase.DETACHED_PROCESS), // working but "output" is invisible
//                         new WinDef.DWORD(0), // not working?
                        null,
                        // the working directory
                        "d:\\dev\\github\\playground",
                        startupInfo,
                        processInformation);

        checkError(result, true);

        int pid = processInformation.dwProcessId.intValue();
        System.out.println("pid=" + pid);

        Thread.sleep(10000);

        // this is a "kill", shutdown hook is not triggered
        WinNT.HANDLE h = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, pid);
        checkError(Kernel32.INSTANCE.TerminateProcess(h, 1), false);
    }

    private static void checkError(boolean result, boolean quit) {
        if (!result) {
            int error = Kernel32.INSTANCE.GetLastError();
            System.out.println("OS error #" + error);
            System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));
            if (quit) {
                throw new RuntimeException("quit after error=" + error);
            }
        }
    }
}
