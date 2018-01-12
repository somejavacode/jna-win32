package process;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public class ProcessTool {

    public static void main(String[] args) throws Exception {

        int pid = start(
                "d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest",
                "d:\\dev\\github\\playground",
                "d:\\dev\\github\\playground\\out1",
                "d:\\dev\\github\\playground\\err1",
                false
        );

        System.out.println("pid=" + pid);

        Thread.sleep(10000);

        kill(pid);
    }

    private static void kill(int pid) {
        // this is a "kill", shutdown hook is not triggered
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms686714(v=vs.85).aspx
        WinNT.HANDLE h = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, pid);
        checkError(Kernel32.INSTANCE.TerminateProcess(h, 1), true);
    }

    private static void interrupt(int pid) {
        // maybe use GenerateConsoleCtrlEvent to send CTRL-C
    }

    // return false if process does not exist
    private static boolean check(int pid) {
        // try to collect as much info as possible
        return false;
    }

    /**
     * start new process
     *
     * @param command command with arguments
     * @param workDir working directory for command
     * @param outFile output file (i.e. "stdOut")
     * @param errFile optional error file (i.e. "stdErr"). if null error is sent to outFile.
     * @param append if true append to outFile and errFile
     * @return PID of process
     */
    private static int start(String command,
                             String workDir,
                             String outFile,
                             String errFile,
                             boolean append) {

        // https://github.com/java-native-access/jna/blob/master/contrib/platform/test/com/sun/jna/platform/win32/Kernel32Test.java
        // https://stackoverflow.com/questions/7018228/how-do-i-redirect-output-to-a-file-with-createprocess

        WinNT.HANDLE hFileOut = getHandle(outFile, append);

        WinNT.HANDLE hFileErr = errFile != null ? getHandle(errFile, append) : null;

        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms686331(v=vs.85).aspx
        WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
        startupInfo.hStdOutput = hFileOut;
        // error to out if no error defined
        startupInfo.hStdError = hFileErr != null ? hFileErr : hFileOut;
        // enable handles
        startupInfo.dwFlags = WinBase.STARTF_USESTDHANDLES;

        WinBase.PROCESS_INFORMATION.ByReference processInformation = new WinBase.PROCESS_INFORMATION.ByReference();

        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
        boolean result = Kernel32.INSTANCE.CreateProcess(
                null,
                command,
                // not working at this level. assume "cmd.exe" is interpreting the ">" etc.
                // "d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest > log.txt 2>&1",

                // need "cmd" to add "magic"?
                // working "BUT": kill does only kills cmd.exe. output still "working"!
//                        "c:\\windows\\system32\\cmd.exe /c d:\\dev\\bin\\jdk1.8.0_151_x64\\bin\\java.exe -cp target\\classes ShellTest > log.txt 2>&1",
                null,
                null,
                true,
                // new WinDef.DWORD(WinBase.CREATE_NEW_CONSOLE), // console pops up
                new WinDef.DWORD(WinBase.DETACHED_PROCESS), // working but "output" is invisible
                null,
                workDir,
                startupInfo,
                processInformation);

        checkError(result, true);

        return processInformation.dwProcessId.intValue();
    }

    private static WinNT.HANDLE getHandle(String file, boolean append) {
        // this is essential. without "inheritance" file have 0 bytes.
        WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;

        // https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx
        if (append) {
            return Kernel32.INSTANCE.CreateFile(
                    file,
                    WinNT.FILE_APPEND_DATA,
                    WinNT.FILE_SHARE_READ + WinNT.FILE_SHARE_WRITE,
                    sa,
                    WinNT.OPEN_ALWAYS,
                    WinNT.FILE_ATTRIBUTE_NORMAL,
                    null);
        }
        else {
            return Kernel32.INSTANCE.CreateFile(
                    file,
                    WinNT.FILE_WRITE_DATA + WinNT.FILE_READ_DATA,
                    WinNT.FILE_SHARE_READ,
                    sa,
                    WinNT.CREATE_ALWAYS,
                    WinNT.FILE_ATTRIBUTE_NORMAL,
                    null);
        }
    }

    private static void checkError(boolean result, boolean quit) {
        if (!result) {
            int error = Kernel32.INSTANCE.GetLastError();
            System.out.println("OS error #" + error + " " + Kernel32Util.formatMessageFromLastErrorCode(error));
            if (quit) {
                throw new RuntimeException("quit after error=" + error);
            }
        }
    }
}
