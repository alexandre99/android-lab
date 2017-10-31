package br.com.agenda.agenda;

import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by italo.teixeira on 25/10/2017.
 */

public class WebClient {

    public String post(String json) {
        String endereco = "https://www.caelum.com.br/mobile";
        return realizaConexao(json, endereco);
    }

    public String realizaConexao(String json, String endereco) {
        try {
            URL url = new URL(endereco);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            PrintStream output = new PrintStream(connection.getOutputStream());
            output.print(json);

            connection.connect();

            Scanner scanner = new Scanner(connection.getInputStream());
            String resposta = scanner.next();
            return resposta;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
