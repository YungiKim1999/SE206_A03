package wikispeak.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class representing a bash command.
 */
public class Command {

    private String _command;
    private String _stream = "Command did not execute";

    public Command(String command){
        _command = command;
    }

    /**
     * Executes the bash command. Returns an exit code and saves the output stream (if applicable)
     */
    public int execute(){

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", _command);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            saveStream(exitCode, process);
            return exitCode;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 2;
        }
    }

    public String getStream(){
        return _stream;
    }

    /**
     * Saves the appropriate stream of an executed command given its exitcode
     * @param exitCode
     * @param process
     * @throws IOException
     */
    private void saveStream(int exitCode, Process process) throws IOException {
        InputStream inputStream;
        BufferedReader reader;

        if(exitCode == 0){
            inputStream = process.getInputStream();
        }
        else{
            inputStream = process.getErrorStream();
        }

        reader = new BufferedReader(new InputStreamReader(inputStream));

        String streamLine;
        String streamFull = "";
        while((streamLine = reader.readLine()) != null) {
            streamFull = streamFull + streamLine + "\n";
        }

        _stream = streamFull;
    }

}
