package server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class MemoryChecker {

    /*
     |------------------ max ------------------------| allowed to be occupied from OS
     |------------------ committed -------|          | currently taken from OS
     |------------------ used --|                    | currently used from client
     */

    private static long getMaxHeapSize() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return memoryUsage.getMax();
    }

    private static long getCurrentHeapSize() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return memoryUsage.getCommitted();
    }

    private static long getUsedHeapSize() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return memoryUsage.getUsed();
    }

    public static void printHeapStatistic(String message) {
        System.out.println("------------------------------------------");
        System.out.println("INFORMATION ABOUT CURRENT HEAP SPACE");
        System.out.println("Current Information: " + message);
        System.out.println();
        String currentlyUsed = formatSize(getUsedHeapSize());
        System.out.println("Currently Used:                " + currentlyUsed);
        String totalSize = formatSize(getCurrentHeapSize());
        System.out.println("Current Taken Size:            " + totalSize);
        String maxSize = formatSize(getMaxHeapSize());
        System.out.println("Maximum Occupiable Size:       " + maxSize);
        System.out.println("------------------------------------------");
    }

    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }

        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double) size / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
