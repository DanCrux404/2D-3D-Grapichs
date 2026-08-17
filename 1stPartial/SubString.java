public class SubString{
    public static void main(String[] args) {
         if (args.length > 0) {
            String words = args[0];

            char[] wordc = words.toCharArray();

            for(int i = wordc.length; i > 0; i --)
            {
                for(int j = 0; j < i; j++)
                {
                    System.out.print(wordc[j]);
                }
                System.out.println();
            }

            for(int i = wordc.length; i >= 0; i--)
            {
                for(int j = i; j < wordc.length; j++)
                {
                    System.out.print(wordc[j]);
                }
                System.out.println();
            }
        }else {
            System.out.println("No arguments were passed.");
        }

    }
}
