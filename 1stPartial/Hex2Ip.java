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

        // An IPv4 address contains 4 octets.
        // Each octet is represented by two hexadecimal characters,
        // so the hexadecimal value must contain exactly 8 characters.
        if(hex.length() != 8)
        {
            System.out.println("Error");
            return;
        }

        for(int i = 0; i < hexas.length; i += 2)
        {
            // Let's say we have FF, starts at 0.
            //
            // 0 X 16 = 0, then:
            // result += Character.digit('F', 16);
            // that is: 0 + 15 = 15, now result = 15.
            //
            // But the second digit is missing, so we repeat below
            // but with result = 15.
            //
            // result *= 16;
            // that is 15 X 16 = 240.
            //
            // And then:
            // result += 15;
            //
            // So that sum is:
            // 240 + 15 = 255.
            //
            // In resume, for each pair:
            // value = X X 16 + Y

            // Interpret the first character as a hexadecimal digit.
            // Example:
            // '0' -> 0
            // '5' -> 5
            // 'A' -> 10
            // 'F' -> 15
            int first = Character.digit(hexas[i], 16);

            // Character.digit() returns -1 when the character
            // is not a valid hexadecimal digit.
            //
            // Example:
            // Character.digit('G', 16) -> -1
            if(first == -1)
            {
                System.out.println("Error");
                return;
            }

            // The first hexadecimal digit represents the
            // high part of the byte, so we multiply by 16.
            result *= 16;
            result += first;

            // Get the second hexadecimal digit.
            int second = Character.digit(hexas[i + 1], 16);

            // Validate the second hexadecimal character.
            if(second == -1)
            {
                System.out.println("Error");
                return;
            }

            // Add the second hexadecimal digit.
            result *= 16;
            result += second;

            // The pair is now converted from hexadecimal
            // to its decimal value.
            //
            // Example:
            // FF -> 255
            // 0A -> 10
            System.out.print(result + ".");

            // Reset result so the next hexadecimal pair
            // can be converted independently.
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
            // Validate that every character is either
            // a decimal digit or a '.' separator.
            //
            // Character.digit() returns -1 if the character
            // is not a valid decimal digit.
            if(ips[i] != '.' && Character.digit(ips[i], 10) == -1)
            {
                System.out.println("Error");
                return;
            }

            if(ips[i] == '.' || i == ips.length - 1)
            {
                // If we are on the last character, first
                // we add the digit to the result.
                //
                // This is necessary because there is no '.'
                // after the last octet to trigger this part.
                if(i == ips.length - 1)
                {
                    result *= 10;
                    result += Character.digit(ips[i], 10);
                }

                // An IP octet must be between 0 and 255.
                //
                // Example:
                // 255 -> valid
                // 256 -> Error
                if(result > 255)
                {
                    System.out.println("Error");
                    return;
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
                //
                // 15 % 16 = 15
                // 15 / 16 = 0
                //
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
                //
                // Example:
                // 255 gives [15, 15]
                //
                // 15 -> F
                // 15 -> F
                //
                // Result: FF
                for(int j = counter - 1; j >= 0; j--)
                {
                    String value = "";

                    // Values from 0 to 9 can be represented
                    // directly as hexadecimal digits.
                    //
                    // We add a leading zero because every IP
                    // octet must be represented by exactly
                    // two hexadecimal digits.
                    //
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
                        //
                        // 10 - 10 = 0 -> A
                        // 11 - 10 = 1 -> B
                        // 12 - 10 = 2 -> C
                        // 13 - 10 = 3 -> D
                        // 14 - 10 = 4 -> E
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
                //
                // Example with 255:
                //
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
