package jump61;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

//import ucb.util.CommandArgs;

/** The jump61 game.
 * @author Alex Freeman
 */
public class Main {

    /** Size of the buffer used for commands from the GUI. */
    static final int COMMAND_BUFFER_SIZE = 2048;

    /** Location of usage message resource. */
    static final String USAGE = "jump61/Usage.txt";

    /** Play jump61.  ARGS0 may consist of the single string
     *  '--display' to indicate that the game is played using a GUI. Prints
     *  a usage message if the arguments are wrong. */
    public static void main(String[] args0) {
        /* CommandArgs args =
            new CommandArgs("--display{0,1}", args0);

        if (!args.ok()) {
            usage();
            return;
        } */

        Game game;
        /*if (args.contains("--display")) {
            System.err.println("No graphical interface implemented");
            System.exit(1);
        } else {}*/ //When using UCB gui, put thefollowing code in here.
        Writer output = new OutputStreamWriter(System.out);
        game = new Game(new InputStreamReader(System.in),
                        output, output,
                        new OutputStreamWriter(System.err));
        System.exit(game.play());
    }

    /** Print the contents of the resource named NAME on OUT.
     *  NAME will typically be a file name based in one of the directories
     *  in the class path.  */
    static void printHelpResource(String name, PrintWriter out) {
        try {
            InputStream resource =
                Main.class.getClassLoader().getResourceAsStream(name);
            BufferedReader str =
                new BufferedReader(new InputStreamReader(resource));
            for (String s = str.readLine(); s != null; s = str.readLine())  {
                out.println(s);
            }
            str.close();
            out.flush();
        } catch (IOException excp) {
            out.printf("No help found.");
            out.flush();
        }
    }

    /** Print usage message. */
    private static void usage() {
        printHelpResource(USAGE, new PrintWriter(System.err));
    }

}

