package loganalyze.additional;

/**
 * Helper-class to check the used operation system.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class OSValidator {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Check if the used os is windows.
     *
     * @return returns true if the os is windows
     */
    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    /**
     * Check if the used os is mac.
     *
     * @return returns true if the os is mac
     */
    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    /**
     * Check if the used os is unix.
     *
     * @return returns true if the os is unix
     */
    public static boolean isUnix() {
        return (OS.contains("nix")
                || OS.contains("nux")
                || OS.indexOf("aix") > 0);
    }

    /**
     * Check if the used os is solaris.
     *
     * @return returns true if the os is solaris
     */
    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }
}