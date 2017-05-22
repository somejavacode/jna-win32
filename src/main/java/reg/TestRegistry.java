package reg;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

import java.util.Date;

public class TestRegistry {

    public static void main(String[] args) {

        // https://superuser.com/questions/963910/how-to-find-the-build-version-of-windows-10
        final String versionKey = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";
        final WinReg.HKEY hklm = WinReg.HKEY_LOCAL_MACHINE;

        String name = getString(hklm, versionKey, "ProductName");
        String rel = getString(hklm, versionKey, "ReleaseId");
        String ver = getString(hklm, versionKey, "CurrentBuildNumber");
        // UBR (Update Build Revision)
        int ubr = getInt(hklm, versionKey, "UBR");
        System.out.println(name + " " + rel + " " + ver + "." + ubr);
        int maj = getInt(hklm, versionKey, "CurrentMajorVersionNumber");
        int min = getInt(hklm, versionKey, "CurrentMinorVersionNumber");
        System.out.println("major.minor=" + maj + "." + min);

        // https://msdn.microsoft.com/en-us/library/ms724284(v=vs.85).aspx
        // FILETIME structure
        // Contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
        long time = getLong(hklm, versionKey, "InstallTime");
        // this is broken (one day error), maybe time zone crap
        //  long shift = new Date(1970, 1, 1).getTime() - new Date(1601, 1, 1).getTime();
        long shift = 11644473600000L;  // copy from WinBase.java
        System.out.println("installed=" + new Date(time / 10000 - shift));
        // "super complicated" with FILETIME
        // System.out.println("installed " + new WinBase.FILETIME(new WinNT.LARGE_INTEGER(time)).toDate());

        // login user?
        String user = Advapi32Util.getUserName();
        System.out.println("user=" + user);

        // admin?
//        Advapi32Util.Account[] accounts = Advapi32Util.getCurrentUserGroups();
//        for (Advapi32Util.Account a : accounts) {
//            System.out.println("account: " + a.name + " " + a.accountType);
//        }
    }

    private static String getString(WinReg.HKEY hkey, String path, String key) {
        String ret = null;
        try {
            ret = Advapi32Util.registryGetStringValue(hkey, path, key);
        }
        catch (Win32Exception ex) {
            // com.sun.jna.platform.win32.Win32Exception: The system cannot find the file specified.
            // ignore exception. assume "key not found"
        }
        return ret;
    }

    private static int getInt(WinReg.HKEY hkey, String path, String key) {
        int ret = 0;
        try {
            ret = Advapi32Util.registryGetIntValue(hkey, path, key);
        }
        catch (Win32Exception ex) {
            // com.sun.jna.platform.win32.Win32Exception: The system cannot find the file specified.
            // ignore exception. assume "key not found"
        }
        return ret;
    }

    private static long getLong(WinReg.HKEY hkey, String path, String key) {
        long ret = 0;
        try {
            ret = Advapi32Util.registryGetLongValue(hkey, path, key);
        }
        catch (Win32Exception ex) {
            // com.sun.jna.platform.win32.Win32Exception: The system cannot find the file specified.
            // ignore exception. assume "key not found"
        }
        return ret;
    }
}
