package process;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public class ProcessTool {

    public static void main(String[] args) throws Exception {

        // https://github.com/java-native-access/jna/blob/master/contrib/platform/test/com/sun/jna/platform/win32/Kernel32Test.java

        //WinBase.PROCESS_INFORMATION processInformation = new WinBase.PROCESS_INFORMATION();
        WinBase.PROCESS_INFORMATION.ByReference processInformation = new WinBase.PROCESS_INFORMATION.ByReference();

//        WinNT.HANDLE hFileOut = Kernel32.INSTANCE.CreateFile("d:\\dev\\git\\playground\\out1", WinNT.GENERIC_READ, WinNT.FILE_SHARE_READ,
//                null, WinNT.OPEN_EXISTING, WinNT.FILE_ATTRIBUTE_NORMAL, null);
//
//        WinNT.HANDLE hFileErr = Kernel32.INSTANCE.CreateFile("d:\\dev\\git\\playground\\err1", WinNT.GENERIC_READ, WinNT.FILE_SHARE_READ,
//                null, WinNT.OPEN_EXISTING, WinNT.FILE_ATTRIBUTE_NORMAL, null);

        WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
//        startupInfo.hStdOutput = hFileOut;
//        startupInfo.hStdError = hFileErr;

        boolean result = Kernel32.INSTANCE.CreateProcess(
                        // "c:\\windows\\notepad.exe",
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
                         new WinDef.DWORD(WinBase.CREATE_NEW_CONSOLE), // console pops up
//                         new WinDef.DWORD(WinBase.DETACHED_PROCESS), // working but "output" is invisible
//                         new WinDef.DWORD(0), // not working?
                        null,
                        // working directory
                        "d:\\dev\\git\\playground",
                        startupInfo,
                        processInformation);

        checkError(result, true);

        int pid = processInformation.dwProcessId.intValue();
        System.out.println("pid=" + pid);

        Thread.sleep(5000);

        // this is a "kill", shutdown hook is not triggered?
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
