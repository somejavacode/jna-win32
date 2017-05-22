package reg;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.Date;

public class TestRegistry {

    public static void main(String[] args) {

        // https://superuser.com/questions/963910/how-to-find-the-build-version-of-windows-10
        String versionKey = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";

        String name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "ProductName");
        String rel = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "ReleaseId");
        String ver = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "CurrentBuildNumber");
        // UBR (Update Build Revision)
        int ubr = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "UBR");
        System.out.println(name + " " + rel + " " + ver + "." + ubr);
        int maj = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "CurrentMajorVersionNumber");
        int min = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "CurrentMinorVersionNumber");
        System.out.println("major.minor=" + maj + "." + min);

        // https://msdn.microsoft.com/en-us/library/ms724284(v=vs.85).aspx
        // FILETIME structure
        // Contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
        long time = Advapi32Util.registryGetLongValue(WinReg.HKEY_LOCAL_MACHINE, versionKey, "InstallTime");
        // this is broken (one day error), maybe time zone crap
        //  long shift = new Date(1970, 1, 1).getTime() - new Date(1601, 1, 1).getTime();
        long shift = 11644473600000L;  // copy from WinBase.java
        System.out.println("installed " + new Date(time / 10000 - shift));
        // "super complicated" with FILETIME
        // System.out.println("installed " + new WinBase.FILETIME(new WinNT.LARGE_INTEGER(time)).toDate());

        // login user?
        String user = Advapi32Util.getUserName();
        System.out.println("user " + user);

        // admin?
//        Advapi32Util.Account[] accounts = Advapi32Util.getCurrentUserGroups();
//        for (Advapi32Util.Account a : accounts) {
//            System.out.println("account: " + a.name + " " + a.accountType);
//        }
    }
}
