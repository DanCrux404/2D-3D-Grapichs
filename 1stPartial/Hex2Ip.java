public class Hex2Ip{
    public static void main(String[] args) {
        final String HEX = "-hex";
        final String IP = "-ip";

        Hex2Ip converter = new Hex2Ip();

        if(args.length > 0){
            if(args[0].equals(HEX)){
                System.out.println(args[1]);
                System.out.println("The value on ip is: ");
                converter.Hexa2Ip(args[1]);
                return;
            }

            if(args[0].equals(IP)){
                System.out.println(args[1]);
                System.out.println("The value on hexadecimal is: ");
                converter.Ip2Hexa(args[1]);
                return;
            }
        }else{
            System.out.println("No arguments were  provided");
            return;
        }
    }

    public void Hexa2Ip(String hex){
        char[] hexas = hex.toCharArray();
        int result = 0;

        for(int i = 0; i < hexas.length; i+=2)
        {
            //Let's say we have FF, starts at 0
            //0 X 16 = 0, then 
            //result += Character.digit('F', 16);
            //that is: 0 + 15 = 15, now result = 15
            //But the second if is missing and we repeat below but with result = 15
            //result *= 16; that is 15 × 16 = 240 and... result += 15;
            //so that sum is 240 + 15 = 255
            //On resume for each pair: value = X × 16 + Y
            result *= 16;
            //interpret a single numeric character as a hexa "16 base" digit
            //example: '0' -> 0, '5' -> 5, 'A'-> 10 ,'F' -> 15 
            //And... the magic? here is the best part... look the result *= 16 line...
            result += Character.digit(hexas[i], 16);
            result *= 16;
            result += Character.digit(hexas[i+1], 16);
            System.out.print(result + ".");
            result = 0;
        }
        System.out.println();
    }

    public void Ip2Hexa(String ip){
        char[] ips = ip.toCharArray();
        int result = 0;
        int convert = 0;
        String res = "";
        char[] letters = {'A', 'B', 'C', 'D', 'E', 'F'};

        for(int i = 0; i < ips.length; i++)
        {
            if(ips[i] == '.' || i == ips.length - 1)
            {
                // If we are on the last character, first
                // we add the digit to the result.
                // This is necessary because there is no '.' after
                // the last octet to trigger this part of the code.
                if(i == ips.length - 1)
                {
                    result *= 10;
                    result += Character.digit(ips[i], 10);
                }

                // Each IP octet can produce a maximum of two
                // hexadecimal digits, so we only need two positions.
                int[] pair = new int[2];
                int counter = 0;

                // To convert the decimal value to hexadecimal,
                // we repeatedly divide by 16.
                // The remainder (%) gives us each hexadecimal digit.
                //
                // Example with 255:
                //
                // 255 % 16 = 15
                // 255 / 16 = 15
                // 15 % 16 = 15
                // 15 / 16 = 0
                // So the hexadecimal digits are 15 and 15,
                // which will later become F and F.
                while(result > 0)
                {
                    convert = result % 16;
                    pair[counter] = convert;
                    counter++;
                    result /= 16;
                }

                // The remainders are stored backwards,
                // so we read the array from the last position
                // to the first one.
                // Example:
                // 255 gives [15, 15]
                // 15 -> F
                // 15 -> F
                // Result: FF
                for(int j = counter - 1; j >= 0; j--)
                {
                    String value = "";

                    // Values from 0 to 9 can be represented
                    // directly as hexadecimal digits.
                    // We add a leading zero because every IP octet
                    // must be represented by exactly two hex digits.
                    // Example:
                    // 5 -> 05
                    // 1 -> 01
                    if(pair[j] < 10)
                    {
                        value = "0" + pair[j];
                    }
                    else
                    {
                        // Values from 10 to 15 are represented
                        // by the hexadecimal letters A to F.
                        // 10 - 10 = 0 -> A
                        // 11 - 10 = 1 -> B
                        // ...
                        // 15 - 10 = 5 -> F
                        value = "" + letters[pair[j] - 10];

                        // If the octet produced only one hexadecimal
                        // digit, we add a leading zero.
                        //
                        // Example:
                        // 10 decimal -> A -> 0A
                        if(counter == 1)
                        {
                            value = "0" + value;
                        }
                    }

                    // Add the converted hexadecimal digit(s)
                    // to the final result.
                    res += value;
                }

                // Reset result so the next IP octet
                // can be converted independently.
                result = 0;
            }
            else
            {
                // Build the current decimal octet digit by digit.
                // Example with 255:
                // result = 0
                // 0 * 10 + 2 = 2
                // 2 * 10 + 5 = 25
                // 25 * 10 + 5 = 255
                result *= 10;
                result += Character.digit(ips[i], 10);
            }
        }

        System.out.println(res);
    }
}
