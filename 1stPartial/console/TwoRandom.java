import java.util.Random;

public class TwoRandom{
    public static void main(String args []){
        Random rand = new Random();

        int randomInt = rand.nextInt(100);
        int randomInt2 = rand.nextInt(100);

        System.out.println("First number: " + randomInt);
        System.out.println("Second number: " + randomInt2);

        if(randomInt > randomInt2){
            System.out.println(randomInt + " is bigger than " + randomInt2);
            return;
        }

        if(randomInt2 > randomInt){
            System.out.println(randomInt2 + " is bigger than " + randomInt);
            return;
        }

        System.out.println("Both numbers are the same :3");
    }
}
