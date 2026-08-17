import java.util.Arrays;
import java.util.stream.Stream;

public class Sort{
    public static void main(String[] args) {
        if (args.length > 0) {

            System.out.println("All arguments:");

            for (String arg : args) {
                System.out.println("- " + arg);
            }

            int[] intArray = Stream.of(args)
                       .mapToInt(Integer::parseInt)
                       .toArray();
            
            System.out.println("Sorted array:");
            Arrays.sort(intArray);
            System.out.println(Arrays.toString(intArray));

        } else {
            System.out.println("No arguments were passed.");
        }
    }
}
